package za.pixello.hub

import android.content.Context
import androidx.core.content.edit

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("hub", Context.MODE_PRIVATE)

    var dynamicColor: Boolean
        get() = sp.getBoolean("dynamic_color", true)
        set(value) = sp.edit { putBoolean("dynamic_color", value) }
}
