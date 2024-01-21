package h.lillie.weartube.presentation

import android.content.Context
import android.util.DisplayMetrics

class Converter {
    fun dpToPx(context: Context, dp: Float) : Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}