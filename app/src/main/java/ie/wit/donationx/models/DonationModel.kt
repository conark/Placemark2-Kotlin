package ie.wit.donationx.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@IgnoreExtraProperties
@Parcelize
data class DonationModel(
      var uid: String? = "",
      var paymentmethod: String = "N/A",
      var amount: Int = 0,
      var message: String = "Homer for President!",
      var upvotes: Int = 0,
      var profilepic: String = "",
      var latitude: Double = 0.0,
      var longitude: Double = 0.0,
      var email: String? = "joe@bloggs.com")
      : Parcelable
{
      @Exclude
      fun toMap(): Map<String, Any?> {
            return mapOf(
                  "uid" to uid,
                  "paymentmethod" to paymentmethod,
                  "amount" to amount,
                  "message" to message,
                  "upvotes" to upvotes,
                  "profilepic" to profilepic,
                  "latitude" to latitude,
                  "longitude" to longitude,
                  "email" to email
            )
      }
}


