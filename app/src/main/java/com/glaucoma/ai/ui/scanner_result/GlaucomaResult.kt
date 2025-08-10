import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GlaucomaResult(
    val isGlaucoma: Boolean,
    val confidence: Int,
    val message: String
) : Parcelable