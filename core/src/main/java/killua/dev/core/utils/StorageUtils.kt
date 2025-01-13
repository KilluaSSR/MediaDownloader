package killua.dev.core.utils

import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import android.Manifest

object StorageUtils {
    fun checkPermission(context: Context) = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        ActivityCompat.checkSelfPermission(context, Manifest.permission.MANAGE_MEDIA)}else{
        ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    fun requestPermission() {
        // Request permission
    }
}