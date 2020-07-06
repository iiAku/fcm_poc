package com.iiaku.push


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    var MY_PERMISSIONS_REQUEST_CALL_PHONE = 1
    val TAG = "LOGING"

    val filter = IntentFilter("custom-event-name")
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            Log.d("receiver", "Got message: $message")
            val yourIntent = Intent(context, MainActivity::class.java)
            yourIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0,
                yourIntent, 0
            )
            try {
                pendingIntent.send(applicationContext, 0, yourIntent)
            } catch (e: Exception) {
                Log.e(TAG, Arrays.toString(e.stackTrace))
            }

            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            //callPhone()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w( "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.d(TAG, "token: $token")
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
        Log.d(TAG, "Registered receiver successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Log.d(TAG, "unregistered receiver successfully")
    }

    fun callPhone(phoneNumber: String) {
        val call = Intent(Intent.ACTION_CALL)
        call.setData(Uri.parse("tel:$phoneNumber"))

        val permissionToGrant = android.Manifest.permission.CALL_PHONE
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permissionToGrant) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionToGrant)) {
                Log.d("LOGING","Asking permission to user")
            } else {
                //ActivityCompat.requestPermissions(this, arrayOf(permissionToGrant), MY_PERMISSIONS_REQUEST_CALL_PHONE)
                startActivity(call)
            }
        } else {
            startActivity(call)
        }
    }
}
