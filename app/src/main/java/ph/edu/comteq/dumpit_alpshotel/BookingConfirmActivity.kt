package ph.edu.comteq.dumpit_alpshotel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.comteq.dumpit_alpshotel.ui.theme.Dumpit_alpshotelTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
class BookingConfirmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Dumpit_alpshotelTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Booking Confirm") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // Navigate to MainActivity (Homepage)
                                    val intent = Intent(this@BookingConfirmActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    startActivity(intent)
                                    finish()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    BookingConfirmScreen(
                        modifier = Modifier.padding(innerPadding),
                        hotelName = intent.getStringExtra("HOTEL_NAME") ?: "",
                        roomType = intent.getStringExtra("ROOM_TYPE") ?: "",
                        roomPrice = intent.getIntExtra("ROOM_PRICE", 0),
                        maxGuests = intent.getIntExtra("MAX_GUESTS", 1)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(
    modifier: Modifier = Modifier,
    hotelName: String,
    roomType: String,
    roomPrice: Int,
    maxGuests: Int
) {
    val context = LocalContext.current

    // Form state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var adults by remember { mutableStateOf("1") }
    var children by remember { mutableStateOf("0") }
    var travelPurpose by remember { mutableStateOf("sightseeing") }
    var paymentMethod by remember { mutableStateOf("cash") }

    // Date picker states
    var showCheckInDatePicker by remember { mutableStateOf(false) }
    var showCheckOutDatePicker by remember { mutableStateOf(false) }
    val checkInDatePickerState = rememberDatePickerState()
    val checkOutDatePickerState = rememberDatePickerState()

    // Confirmation dialog state
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingBooking by remember { mutableStateOf<Booking?>(null) }

    // Derived state
    val totalGuests = (adults.toIntOrNull() ?: 0) + (children.toIntOrNull() ?: 0)
    val roomsNeeded = remember(totalGuests, maxGuests) {
        if (totalGuests == 0) 1 else Math.ceil(totalGuests.toDouble() / maxGuests).toInt()
    }

    val totalPrice = remember(roomsNeeded, roomPrice, travelPurpose, checkInDate, checkOutDate) {
        calculateTotalPrice(roomsNeeded, roomPrice, travelPurpose, checkInDate, checkOutDate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "You are going to reserve:",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hotel Info
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hotel: $hotelName", fontWeight = FontWeight.Bold)
                Text("Room: $roomType")
                Text("Price per room: ₱$roomPrice")
            }
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Personal Information
            Text("Personal Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dates
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = checkInDate,
                    onValueChange = { },
                    label = { Text("Check-in Date") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    placeholder = { Text("Select Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCheckInDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    singleLine = true
                )
                OutlinedTextField(
                    value = checkOutDate,
                    onValueChange = { },
                    label = { Text("Check-out Date") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    placeholder = { Text("Select Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCheckOutDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Occupants
            Text("Occupants", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = adults,
                    onValueChange = { adults = it },
                    label = { Text("Adults") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = children,
                    onValueChange = { children = it },
                    label = { Text("Children") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Auto-calculated rooms
            Text(
                text = "Rooms needed: $roomsNeeded",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Travel Purpose
            Text("Travel for business?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = travelPurpose == "sightseeing",
                        onClick = { travelPurpose = "sightseeing" }
                    )
                    Text("For sightseeing", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = travelPurpose == "business",
                        onClick = { travelPurpose = "business" }
                    )
                    Text("+ ₱150 For business with a meeting room", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method
            Text("Which way to pay?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Column {
                listOf("Cash", "Credit card", "E-Pay").forEach { method ->
                    val methodKey = method.toLowerCase().replace(" ", "_")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMethod == methodKey,
                            onClick = { paymentMethod = methodKey }
                        )
                        Text(method, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Total Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₱", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₱$totalPrice",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (validateForm(firstName, lastName, checkInDate, checkOutDate, adults, children)) {
                            // Create booking but don't save yet - show confirmation first
                            pendingBooking = Booking(
                                firstName = firstName,
                                lastName = lastName,
                                hotelName = hotelName,
                                roomType = roomType,
                                checkInDate = checkInDate,
                                checkOutDate = checkOutDate,
                                adults = adults.toIntOrNull() ?: 0,
                                children = children.toIntOrNull() ?: 0,
                                rooms = roomsNeeded,
                                travelPurpose = travelPurpose,
                                paymentMethod = paymentMethod,
                                totalPrice = totalPrice
                            )
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Book Now")
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                // When user clicks outside the dialog or back button
                showConfirmationDialog = false
            },
            title = {
                Text(
                    text = "Confirm Booking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Please confirm your booking details:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hotel: $hotelName")
                    Text("Room: $roomType")
                    Text("Name: $firstName $lastName")
                    Text("Dates: $checkInDate to $checkOutDate")
                    Text("Guests: $adults adults, $children children")
                    Text("Rooms: $roomsNeeded")
                    Text("Total: ₱$totalPrice")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This action cannot be undone.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // User confirmed - save the booking and navigate
                        pendingBooking?.let { booking ->
                            BookingManager.saveBooking(context, booking)
                        }
                        showConfirmationDialog = false

                        // Navigate to MyBookings
                        val intent = Intent(context, MyBookingsActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Confirm Booking", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // User cancelled - just close the dialog
                        showConfirmationDialog = false
                        pendingBooking = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Check-in Date Picker Dialog
    if (showCheckInDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCheckInDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        checkInDatePickerState.selectedDateMillis?.let { dateMillis ->
                            checkInDate = formatSelectedDate(dateMillis)
                        }
                        showCheckInDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = checkInDatePickerState)
        }
    }

    // Check-out Date Picker Dialog
    if (showCheckOutDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCheckOutDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        checkOutDatePickerState.selectedDateMillis?.let { dateMillis ->
                            checkOutDate = formatSelectedDate(dateMillis)
                        }
                        showCheckOutDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = checkOutDatePickerState)
        }
    }
}

// Helper functions
fun formatSelectedDate(dateMillis: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateMillis
    val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    return dateFormat.format(calendar.time)
}

fun calculateTotalPrice(
    rooms: Int,
    roomPrice: Int,
    travelPurpose: String,
    checkIn: String,
    checkOut: String
): Int {
    val basePrice = rooms * roomPrice
    val businessFee = if (travelPurpose == "business") 150 else 0

    // Calculate nights from dates
    val nights = calculateNights(checkIn, checkOut)

    return (basePrice + businessFee) * nights
}

fun calculateNights(checkIn: String, checkOut: String): Int {
    if (checkIn.isEmpty() || checkOut.isEmpty()) return 1

    return try {
        val format = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
        val inDate = format.parse(checkIn)
        val outDate = format.parse(checkOut)

        if (inDate != null && outDate != null) {
            val diff = outDate.time - inDate.time
            val days = diff / (1000 * 60 * 60 * 24)
            maxOf(days.toInt(), 1)
        } else {
            1
        }
    } catch (e: Exception) {
        1
    }
}

fun validateForm(
    firstName: String,
    lastName: String,
    checkIn: String,
    checkOut: String,
    adults: String,
    children: String
): Boolean {
    // Basic validation - in real app, show error messages to user
    return firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            checkIn.isNotBlank() &&
            checkOut.isNotBlank() &&
            adults.toIntOrNull() != null &&
            children.toIntOrNull() != null
}