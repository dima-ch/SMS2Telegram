package life.hnj.sms2telegram

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking
import life.hnj.sms2telegram.smshandler.SMSHandleForegroundService

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    val REQUEST_CODE = 101
    var PERMISSIONS = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS
    )

    private fun checkMultiplePermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions(activity: Activity,permissions: Array<String> ) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                } else {
                    Toast.makeText(
                        applicationContext,
                        "SMS2Telegram needs permission",
                        Toast.LENGTH_LONG
                    ).show()
                    this.finishAffinity()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.action_bar))

        val sync2TgEnabledKey = sync2TelegramKey(resources)
        val sync2TgEnabled = getBooleanVal(applicationContext, sync2TgEnabledKey)

        val toggle = findViewById<SwitchCompat>(R.id.enable_telegram_sync)
        val serviceIntent = Intent(
            applicationContext, SMSHandleForegroundService::class.java
        )
        if (sync2TgEnabled) {
            if(!checkMultiplePermissions(PERMISSIONS)){
                requestPermissions(this, PERMISSIONS)
            }
            startSMSService(serviceIntent)
        }
        toggle.isChecked = sync2TgEnabled
        toggle.setOnCheckedChangeListener { _, isChecked ->
            runBlocking { setSync2TgEnabled(sync2TgEnabledKey, isChecked) }
            if (isChecked) {
                if(!checkMultiplePermissions(PERMISSIONS)){
                    requestPermissions(this, PERMISSIONS)
                }
                startSMSService(serviceIntent)
            } else {
                applicationContext.stopService(serviceIntent)
                Toast.makeText(applicationContext, "The service stopped", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startSMSService(serviceIntent: Intent) {
        val alreadyStarted = applicationContext.startService(serviceIntent)
        if (alreadyStarted != null) {
            Log.d(TAG, "The service is already started")
            Toast.makeText(applicationContext, "The service is already started", Toast.LENGTH_SHORT)
                .show()
        } else {
            Log.d(TAG, "Background service started")
            Toast.makeText(applicationContext, "The service started", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun setSync2TgEnabled(
        sync2TgEnabledKey: Preferences.Key<Boolean>,
        value: Boolean
    ) {
        applicationContext.dataStore.edit { settings ->
            settings[sync2TgEnabledKey] = value
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        toolbar.inflateMenu(R.menu.actionbar_menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    action = Intent.CATEGORY_PREFERENCE
                }
                startActivity(intent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}