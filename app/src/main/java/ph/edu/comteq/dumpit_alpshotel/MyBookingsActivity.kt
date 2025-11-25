package ph.edu.comteq.dumpit_alpshotel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.comteq.dumpit_alpshotel.ui.theme.Dumpit_alpshotelTheme

class MyBookingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Dumpit_alpshotelTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("My Bookings") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // Navigate to MainActivity (Homepage)
                                    val intent = Intent(this@MyBookingsActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    MyBookingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        newBooking = intent.getSerializableExtra("NEW_BOOKING") as? Booking
                    )
                }
            }
        }
    }
}


@Composable
fun MyBookingsScreen(modifier: Modifier = Modifier, newBooking: Booking?) {
    val context = LocalContext.current
    var bookings by remember { mutableStateOf(emptyList<Booking>()) }

    // Load bookings from persistent storage when screen starts
    LaunchedEffect(Unit) {
        bookings = BookingManager.getBookings(context)
    }

    // Add new booking if provided and save it to persistent storage
    LaunchedEffect(newBooking) {
        if (newBooking != null) {
            // Save to persistent storage
            BookingManager.saveBooking(context, newBooking)
            // Refresh the list from storage
            bookings = BookingManager.getBookings(context)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "List of my bookings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bookings yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(bookings) { index, booking ->
                    BookingItem(booking = booking, sequence = index + 1)
                }
            }
        }
    }
}

// Your existing BookingItem composable remains the same...
@Composable
fun BookingItem(booking: Booking, sequence: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sequence number
            Text(
                text = "#$sequence",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Name
            Text(
                text = "Name: ${booking.firstName} ${booking.lastName}",
                fontWeight = FontWeight.SemiBold
            )

            // Dates
            Text(text = "Dates: ${booking.checkInDate} to ${booking.checkOutDate}")

            // Occupants
            Text(text = "Adults: ${booking.adults}, Children: ${booking.children}, Rooms: ${booking.rooms}")

            // Booking type and payment
            val purposeText = when (booking.travelPurpose) {
                "sightseeing" -> "Sightseeing"
                "business" -> "Business"
                else -> booking.travelPurpose
            }

            val paymentText = when (booking.paymentMethod) {
                "cash" -> "Cash"
                "credit_card" -> "Credit Card"
                "e_pay" -> "E-Pay"
                else -> booking.paymentMethod
            }

            Text(text = "Type: $purposeText, Payment: $paymentText")

            // Price
            Text(
                text = "Price: ₱${booking.totalPrice}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}