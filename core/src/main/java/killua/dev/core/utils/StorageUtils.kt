package killua.dev.core.utils

import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi

object StorageUtils {
//    fun checkPermission(context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ActivityCompat.checkSelfPermission(context, Manifest.permission.MANAGE_MEDIA) == PackageManager.PERMISSION_GRANTED
//        } else {
//            ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        }
//
//    fun requestPermission(context: Context) {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
//            ActivityCompat.requestPermissions(context.getActivity(), arrayOf(Manifest.permission.MANAGE_MEDIA), 1)
//        }else{
//            ActivityCompat.requestPermissions(context.getActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
//        }
//    }
}