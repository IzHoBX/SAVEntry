package com.izho.saveentry

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.izho.saveentry.data.VisitWithLocation
import com.izho.saveentry.data.getAppDatabase
import com.izho.saveentry.settings.PreferenceUtils


class ExpressCheckoutTileService: TileService(), LifecycleOwner {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private var nextVisitToUse:VisitWithLocation? = null;
    private var hasFavorite = false
    private var alwaysUseScanner:Boolean = true
    private lateinit var onSettingsChangedListener:SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        val database = getAppDatabase(this, resetDb = false)
        database.dao.getAllActiveVisitWithLocation().observe(this, Observer { activeVisits ->
            if (activeVisits != null && !activeVisits.isEmpty()) {
                nextVisitToUse = activeVisits[0]
            } else {
                nextVisitToUse = null
            }
        })

        database.dao.getAllFavoriteLocations().observe(this, Observer { favourites ->
            hasFavorite = favourites.isNotEmpty()
        })
        alwaysUseScanner = PreferenceUtils.getStringPref(this,
            R.string.settings_tile_behavior, getString(R.string.tile_behavior_scanner)) == getString(R.string.tile_behavior_scanner)
        onSettingsChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == getString(R.string.settings_tile_behavior)) {
                alwaysUseScanner = PreferenceUtils.getStringPref(this,
                    R.string.settings_tile_behavior, getString(R.string.tile_behavior_scanner)) == getString(R.string.tile_behavior_scanner)
                updateTile()
            }
        }
        PreferenceUtils.registerListener(onSettingsChangedListener, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mDispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    fun updateTile() {
        val tile = qsTile;
        if (alwaysUseScanner) {
            tile.label = "Scan SafeEntry";
            tile.state = Tile.STATE_INACTIVE;
            tile.icon = Icon.createWithResource(this, R.drawable.ic_camera)
        } else {
            if (nextVisitToUse == null) {
                tile.label = "Check In";
                tile.state = Tile.STATE_INACTIVE;
                tile.icon = Icon.createWithResource(this, R.drawable.ic_login)
            } else {
                tile.label = "Check Out";
                tile.state = Tile.STATE_ACTIVE;
                tile.icon = Icon.createWithResource(this, R.drawable.ic_logout)
            }
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent:Intent
        if(alwaysUseScanner) {
            intent = Intent(this, LiveBarcodeScanningActivity::class.java)
        } else {//need to switch between check in or out
            if(nextVisitToUse != null) {
                intent = Intent(this, CheckInOrOutActivity::class.java)
                intent.putExtra("action", "checkOut")
                intent.putExtra("visitId", nextVisitToUse!!.visit.visitId)
                intent.putExtra("url", nextVisitToUse!!.location.url)
            } else {
                intent = Intent(this, LiveBarcodeScanningActivity::class.java)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        this.startActivity(intent)

        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)
    }

    override fun onDestroy() {
        PreferenceUtils.unregisterListener(onSettingsChangedListener, this)
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        mDispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onTileAdded() {
        Toast.makeText(this, "You can now check in or out faster - even without unlocking your phone", Toast.LENGTH_LONG).show()
        super.onTileAdded()
    }

    override fun getLifecycle(): Lifecycle = mDispatcher.lifecycle

}