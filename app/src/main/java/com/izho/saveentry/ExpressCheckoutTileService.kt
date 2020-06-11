package com.izho.saveentry

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.izho.saveentry.data.VisitWithLocation
import com.izho.saveentry.data.getAppDatabase


class ExpressCheckoutTileService: TileService(), LifecycleOwner {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private var nextVisitToUse:VisitWithLocation? = null;


    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        val database = getAppDatabase(this, resetDb = false)
        val activeVisits = database.dao.getAllActiveVisitWithLocation().observe(this, Observer { activeVisits ->
            if (activeVisits != null && !activeVisits.isEmpty()) {
                nextVisitToUse = activeVisits[0]
            } else {
                nextVisitToUse = null
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mDispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile;
        if (nextVisitToUse == null) {
            tile.label = "Check In";
            tile.state = Tile.STATE_INACTIVE;
            tile.icon = Icon.createWithResource(this, R.drawable.ic_login)
        } else {
            tile.label = "Check Out";
            tile.state = Tile.STATE_ACTIVE;
            tile.icon = Icon.createWithResource(this, R.drawable.ic_logout)
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent:Intent
        if(nextVisitToUse != null) {
            intent = Intent(this, CheckInOrOutActivity::class.java)
            intent.putExtra("action", "checkOut")
            intent.putExtra("visitId", nextVisitToUse!!.visit.visitId)
            intent.putExtra("url", nextVisitToUse!!.location.url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        } else {
            intent = Intent(this, LiveBarcodeScanningActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        }
        this.startActivity(intent)
        Log.v("start", "activity")
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        mDispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun getLifecycle(): Lifecycle = mDispatcher.lifecycle

}