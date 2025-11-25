package ph.edu.comteq.dumpit_alpshotel
import java.io.Serializable
// The main data class representing the entire JSON object
data class HotelDetails(
    val hotel_id: Int,
    val hotel_name: String,
    val guest_reviews: GuestReviews,
    val rooms: List<Room>
)

// Represents the "guest_reviews" object in the JSON
data class GuestReviews(
    val ratings_categories: List<Map<String, Double>>,
    val reviews_objects: List<Review>
)

// Represents a single review object inside "reviews_objects"
data class Review(
    val username: String,
    val country: String,
    val review_text: String
)

// Represents a single room object inside the "rooms" list
data class Room(
    val room_id: Int,
    val room_type: String,
    val room_bed_type: String,
    val room_total_number_of_guests: Int,
    val room_features: List<String>,
    val room_price_for_one_night: Int
)

data class Booking(
    val firstName: String,
    val lastName: String,
    val hotelName: String,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val adults: Int,
    val children: Int,
    val rooms: Int,
    val travelPurpose: String,
    val paymentMethod: String,
    val totalPrice: Int
) : Serializable