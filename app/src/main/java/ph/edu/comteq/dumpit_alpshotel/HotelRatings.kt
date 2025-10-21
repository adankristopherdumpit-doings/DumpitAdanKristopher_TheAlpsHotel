package ph.edu.comteq.dumpit_alpshotel

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import ph.edu.comteq.dumpit_alpshotel.ui.theme.Dumpit_alpshotelTheme
import ph.edu.comteq.dumpit_alpshotel.HotelDetails

// 1. THIS IS NOW A FULL ACTIVITY CLASS
class HotelRatings : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. RECEIVE DATA FROM THE INTENT
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
                                IconButton(onClick = { finish() }) { // Back button
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    // 3. LAUNCH THE SCREEN AND PASS THE RECEIVED DATA
                    HotelRatingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        hotelId = hotelId,
                        hotelImagePath = hotelImagePath
                    )
                }
            }
        }
    }
}

// 4. THIS FUNCTION NOW LOADS DATA BASED ON THE HOTEL ID
@Composable
fun HotelRatingsScreen(modifier: Modifier = Modifier, hotelId: Int, hotelImagePath: String?) {
    val context = LocalContext.current
    var hotelDetails by remember { mutableStateOf<HotelDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // This block runs once to load the JSON data
    LaunchedEffect(hotelId) {
        val jsonString = context.assets.open("hotel_details.json").bufferedReader().use { it.readText() }
        val allDetails = Gson().fromJson(jsonString, HotelDetails::class.java)

        // Find the correct hotel (assuming hotel_id matches)
        if (allDetails.hotel_id == hotelId) {
            hotelDetails = allDetails
        }
        isLoading = false
    }

    // Show a loading indicator while data is being fetched
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return // Stop here while loading
    }

    // Show an error if the hotel wasn't found
    if (hotelDetails == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sorry, details for this hotel could not be found.")
        }
        return // Stop here if no details
    }

    // Main UI for the hotel details
    LazyColumn(modifier = modifier.fillMaxSize()) {

        // Display the main hotel image
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

        // Display the tabs and their content
        item {
            TabbedContent(hotelDetails!!)
        }
    }
}

// 5. NEW COMPOSABLE TO MANAGE TABS AND THEIR STATE
@Composable
fun TabbedContent(hotelDetails: HotelDetails) {
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

        // The content of the selected tab is shown
        when (selectedTabIndex) {
            0 -> GuestReviewsTab(hotelDetails) // Corrected: Tab for reviews
            1 -> RoomsTab(hotelDetails)        // Corrected: Tab for rooms
        }
    }
}

// 6. RENAMED and CLEANED UP: This is the content for the "Guest Reviews" tab
@Composable
fun GuestReviewsTab(hotelDetails: HotelDetails) {
    val guestReviews = hotelDetails.guest_reviews
    Column(Modifier.padding(16.dp)) {
        Text("Overall Ratings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Display category ratings
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

        // Display individual reviews
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

// 7. RENAMED and CLEANED UP: This is the content for the "Room Selection" tab
@Composable
fun RoomsTab(hotelDetails: HotelDetails) {
    Column(Modifier.padding(16.dp)) {
        hotelDetails.rooms.forEach { room ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)) {
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
