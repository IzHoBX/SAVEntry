package com.izho.saveentry

import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class ExpressCheckoutTileService: TileService() {

    override fun onClick() {
        super.onClick()
        val sharedPref = this.getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        with (sharedPref) {
            val action = getString("action", null) ?: return
            val visitId = getLong("visitId", -1)
            if (visitId == -1L) {
                return
            }
            val url = getString("url", null) ?: return
            val notificationId = getInt("notificationId", -1)
            if(notificationId == -1) {
                return
            }

            val intent = Intent(this@ExpressCheckoutTileService, CheckInOrOutActivity::class.java)
            intent.putExtra("action", "checkOut")
            intent.putExtra("visitId", visitId)
            intent.putExtra("url", url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            this@ExpressCheckoutTileService.startActivity(intent)
            Log.v("start", "activity")
        }
    }
}