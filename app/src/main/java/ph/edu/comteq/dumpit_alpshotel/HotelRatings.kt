package ph.edu.comteq.dumpit_alpshotel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.google.gson.Gson
import ph.edu.comteq.dumpit_alpshotel.ui.theme.Dumpit_alpshotelTheme

class HotelRatings : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hotelId = intent.getIntExtra("HOTEL_ID", -1)
        val hotelName = intent.getStringExtra("HOTEL_NAME") ?: "Hotel Details"
        val hotelImagePath = intent.getStringExtra("HOTEL_IMAGE_PATH")

        setContent {
            Dumpit_alpshotelTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(hotelName) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    HotelRatingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        hotelId = hotelId,
                        hotelName = hotelName,
                        hotelImagePath = hotelImagePath
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelRatingsScreen(
    modifier: Modifier = Modifier,
    hotelId: Int,
    hotelName: String,
    hotelImagePath: String?
) {
    val context = LocalContext.current
    var hotelDetails by remember { mutableStateOf<HotelDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // FIXED: Load specific hotel details file
    LaunchedEffect(hotelId) {
        try {
            val fileName = "hotels_details.$hotelId.json"
            println("DEBUG: Looking for file: $fileName")

            // List all files in assets to see what's available
            val files = context.assets.list("")
            println("DEBUG: Available files in assets: ${files?.joinToString()}")

            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            hotelDetails = Gson().fromJson(jsonString, HotelDetails::class.java)
            println("DEBUG: Successfully loaded hotel details for ID: $hotelId")
        } catch (e: Exception) {
            println("DEBUG: Error loading hotel details: ${e.message}")
            e.printStackTrace()
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (hotelDetails == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sorry, details for this hotel could not be found.")
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = "file:///android_asset/$hotelImagePath",
                contentDescription = hotelDetails!!.hotel_name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            TabbedContent(hotelDetails!!, hotelName, hotelImagePath ?: "")
        }
    }
}

@Composable
fun TabbedContent(hotelDetails: HotelDetails, hotelName: String, hotelImagePath: String) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Guest Reviews", "Room Selection")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> GuestReviewsTab(hotelDetails)
            1 -> RoomsTab(hotelDetails, hotelName, hotelImagePath)
        }
    }
}

@Composable
fun GuestReviewsTab(hotelDetails: HotelDetails) {
    val guestReviews = hotelDetails.guest_reviews
    Column(Modifier.padding(16.dp)) {
        Text("Overall Ratings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        guestReviews.ratings_categories.forEach { category ->
            val (key, value) = category.entries.first()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(key, fontWeight = FontWeight.SemiBold)
                Text(value.toString())
            }
            Spacer(Modifier.height(4.dp))
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("What Guests Are Saying", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        guestReviews.reviews_objects.forEach { review ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${review.username} from ${review.country}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(review.review_text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun RoomsTab(hotelDetails: HotelDetails, hotelName: String, hotelImagePath: String) {
    val context = LocalContext.current
    Column(Modifier.padding(16.dp)) {
        hotelDetails.rooms.forEach { room ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        val intent = Intent(context, BookingConfirmActivity::class.java).apply {
                            putExtra("HOTEL_NAME", hotelName)
                            putExtra("HOTEL_IMAGE_PATH", hotelImagePath)
                            putExtra("ROOM_TYPE", room.room_type)
                            putExtra("ROOM_PRICE", room.room_price_for_one_night)
                            putExtra("MAX_GUESTS", room.room_total_number_of_guests)
                        }
                        context.startActivity(intent)
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(room.room_type, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Price: ₱${room.room_price_for_one_night} / night")
                    Text("Beds: ${room.room_bed_type}")
                    Text("Max Guests: ${room.room_total_number_of_guests}")
                    Spacer(Modifier.height(4.dp))
                    Text("Features: ${room.room_features.joinToString(", ")}", lineHeight = 20.sp)
                }
            }
        }
    }
}

private fun createDummyHotelDetails(hotelId: Int): HotelDetails {
    return HotelDetails(
        hotel_id = hotelId,
        hotel_name = "Hotel $hotelId",
        guest_reviews = GuestReviews(
            ratings_categories = listOf(
                mapOf("Cleanliness" to 8.0),
                mapOf("Comfort" to 7.5),
                mapOf("Location" to 8.5),
                mapOf("Value for money" to 7.8)
            ),
            reviews_objects = listOf(
                Review("Guest", "Various", "Great hotel with excellent service and amenities."),
                Review("Traveler", "International", "Comfortable stay with beautiful views.")
            )
        ),
        rooms = listOf(
            Room(
                room_id = hotelId * 1000 + 1,
                room_type = "Standard Room",
                room_bed_type = "1 double bed",
                room_total_number_of_guests = 2,
                room_features = listOf("Free WiFi", "TV", "Private bathroom"),
                room_price_for_one_night = 100
            ),
            Room(
                room_id = hotelId * 1000 + 2,
                room_type = "Deluxe Room",
                room_bed_type = "1 large double bed",
                room_total_number_of_guests = 2,
                room_features = listOf("Free WiFi", "TV", "Private bathroom", "Balcony"),
                room_price_for_one_night = 150
            )
        )
    )
}