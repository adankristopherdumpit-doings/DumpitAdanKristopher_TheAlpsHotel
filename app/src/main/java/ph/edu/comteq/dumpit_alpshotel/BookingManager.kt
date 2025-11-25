package ph.edu.comteq.dumpit_alpshotel

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object BookingManager {
    private const val PREFS_NAME = "bookings_prefs"
    private const val BOOKINGS_KEY = "saved_bookings"

    fun saveBooking(context: Context, booking: Booking) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()

        // Get current bookings and add the new one
        val currentBookings = getBookings(context).toMutableList()
        currentBookings.add(booking)

        // Save updated list back to SharedPreferences
        val bookingsJson = gson.toJson(currentBookings)
        prefs.edit().putString(BOOKINGS_KEY, bookingsJson).apply()
    }

    fun getBookings(context: Context): List<Booking> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bookingsJson = prefs.getString(BOOKINGS_KEY, "[]") ?: "[]"
        val gson = Gson()

        return try {
            val type = object : TypeToken<List<Booking>>() {}.type
            gson.fromJson<List<Booking>>(bookingsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearAllBookings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(BOOKINGS_KEY).apply()
    }
}