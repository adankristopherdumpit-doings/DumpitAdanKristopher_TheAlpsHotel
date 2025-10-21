package ph.edu.comteq.dumpit_alpshotel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import ph.edu.comteq.dumpit_alpshotel.HotelDetails
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import ph.edu.comteq.dumpit_alpshotel.ui.theme.Dumpit_alpshotelTheme
import kotlin.jvm.java
import kotlin.math.floor
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val json = assets.open("hotel_details.1000.json")
        val reader = InputStreamReader(json)
        val hotelDetails = Gson().fromJson(reader, HotelDetails::class.java)
        reader.close()

        setContent {
            Dumpit_alpshotelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Homepage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Homepage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hotels by remember { mutableStateOf(emptyList<Hotel>()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredHotels = hotels.filter { it.hotel_name.contains(searchQuery, ignoreCase = true)
    }
    //load json data
    LaunchedEffect(Unit) {
        val json = context.assets.open("hotels.json")
            .bufferedReader()
            .use { it.readText() }
        val gson = Gson()
        val hotelArray = gson.fromJson(json,
            Array<Hotel>::class.java)
        hotels = hotelArray.toList()
    }
    //Main Container
    Column (
        modifier = modifier
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            //Left Side
            Text(
                text = "Alps Hotel",
                modifier = Modifier.padding(start = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.france_national_flag),
                contentDescription = "Logo",
                modifier = androidx.compose.ui.Modifier.size(width = 40.dp, height = 24.dp)
            )
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .width(40.dp)
                    .clickable {
                        val intent = Intent(context, AccountPage::class.java)
                        context.startActivity(intent)
                    }
            )
        }
        //Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {newValue -> searchQuery = newValue},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {Text("Search...")},
            singleLine = true,
        )
        //Hotel List
        LazyColumn (
            modifier = Modifier.fillMaxSize()
        ){
            items(filteredHotels) {hotel ->
                HotelCard(hotel)

//                Text(
//                    text = hotel.hotel_name,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(16.dp)
//                )
            }
        }
    }
}
@Composable
fun HotelCard(hotel: Hotel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                val intent = Intent(context, HotelRatings::class.java).apply {
                    // Pass data from the 'hotel' object
                    putExtra("HOTEL_ID", hotel.hotel_id)
                    putExtra("HOTEL_NAME", hotel.hotel_name)

                    // FIX: Use the 'hotel' object to get the image path.
                    putExtra("HOTEL_IMAGE_PATH", hotel.hotel_cover_image)
                }
                context.startActivity(intent)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${hotel.hotel_cover_image}")
                    .crossfade(true)
                    .build(),
                contentDescription = hotel.hotel_name,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = hotel.hotel_name,
                    // A smaller font size might fit better here
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        hotel.hotel_rating.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val starCount = floor(hotel.hotel_rating).toInt()
                    //star
                    repeat(starCount) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${hotel.hotel_to_ski_distance} km to ski lift",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomepagePreview() {
    Dumpit_alpshotelTheme {
        Homepage()
    }
}

