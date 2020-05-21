package mx.edu.ittepic.ladm_u4_practica3_arturolarios

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    object Constants{
        const val RECEIVE_SMS = 10
        const val SEND_SMS = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        grantPermission()
    }

    private fun grantPermission()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECEIVE_SMS), Constants.RECEIVE_SMS)
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), Constants.SEND_SMS)
        }
    }
}
