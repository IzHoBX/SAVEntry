package com.izho.saveentry

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher

/**
 * Implementation of App Widget functionality.
 */

const val CHECK_IN_FROM_WIDGET_ACTION = "check_in_from_widget_action"
const val UPDATE_WIDGET = "update widget"
const val WIDGET_ICON_POSITION = "widget icon position"

class HomeWidgetProvider : AppWidgetProvider(), LifecycleOwner {
    private val mDispatcher = ServiceLifecycleDispatcher(this)

    init {
        mDispatcher.onServicePreSuperOnStart()
        mDispatcher.onServicePreSuperOnCreate()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // update each of the app widgets with the remote adapter
        appWidgetIds.forEach { appWidgetId ->

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            val intent = Intent(context, HomeWidgetService::class.java).apply {
                // Add the app widget ID to the intent extras.
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            // Instantiate the RemoteViews object for the app widget layout.
            val rv = RemoteViews(context.packageName, R.layout.home_widget).apply {
                // Set up the RemoteViews object to use a RemoteViews adapter.
                // This adapter connects
                // to a RemoteViewsService  through the specified intent.
                // This is how you populate the data.
                setRemoteAdapter(R.id.grid_view, intent)
            }

            //set individual intent for buttons
            val toastPendingIntent: PendingIntent = Intent(
                context,
                HomeWidgetProvider::class.java
            ).run {
                // Set the action for the intent.
                // When the user touches a particular view, it will have the effect of
                // broadcasting TOAST_ACTION.
                action = CHECK_IN_FROM_WIDGET_ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))

                PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            rv.setPendingIntentTemplate(R.id.grid_view, toastPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == CHECK_IN_FROM_WIDGET_ACTION) {
            val iconPosition= intent.getIntExtra(WIDGET_ICON_POSITION, -1)
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            when (iconPosition) {
                0 -> {
                    val intent = Intent(context, LiveBarcodeScanningActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                    context?.startActivity(intent)
                }
                else -> {
                    if(context != null) {
                        val sp = context?.getSharedPreferences(CHECK_IN_FROM_WIDGET_ACTION, Context.MODE_PRIVATE)
                        val url = sp.getString("url"+iconPosition.toString(), "")
                        val venueName = sp.getString("venueName"+iconPosition.toString(), "")
                        val locationId = sp.getString("locationId"+iconPosition.toString(), "")
                        val organization = sp.getString("organization"+iconPosition.toString(), "")

                        if(url != "" && venueName != "" && locationId != "" && organization != "") {
                            val intent = Intent(context, CheckInOrOutActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                            intent.putExtra("url", url)
                            intent.putExtra("venueName", venueName)
                            intent.putExtra("locationId", locationId)
                            intent.putExtra("organization", organization)
                            intent.putExtra("action", "checkIn")
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(context, ChooseLocationForWidgetActivity::class.java)
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                    or Intent.FLAG_ACTIVITY_NO_HISTORY)
                            intent.putExtra("position", iconPosition)
                            context.startActivity(intent)
                            Toast.makeText(context, "Please choose a location for express check in", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } else if (intent.action == UPDATE_WIDGET) {
            Log.v("Received ", "broadcast")
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if(context != null)
                //onUpdate(context, AppWidgetManager.getInstance(context), IntArray(1, {index -> id}))
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(id, R.id.grid_view)
        }
        super.onReceive(context, intent)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context != null) {
            for (i in 1..3) {
                val sp = context.getSharedPreferences(CHECK_IN_FROM_WIDGET_ACTION, Context.MODE_PRIVATE)
                sp.edit().putString("venueName"+i.toString(), "").apply()
            }
        }

        super.onDeleted(context, appWidgetIds)
    }

    override fun getLifecycle(): Lifecycle = mDispatcher.lifecycle
}

class HomeWidgetService : RemoteViewsService() {

    class LocationRemoteViewsFactory(
        private val context: Context,
        intent: Intent
    ) : RemoteViewsFactory {

        override fun onCreate() {

        }


        override fun getLoadingView(): RemoteViews {
            return RemoteViews(context.packageName, R.layout.header_item)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            if(position == 0) {
                return RemoteViews(context.packageName, R.layout.widget_location_icon).apply {

                    //unique action for each icon
                    val fillInIntent = Intent().apply {
                        putExtra(WIDGET_ICON_POSITION, position)
                    }
                    // Make it possible to distinguish the individual on-click
                    // action of a given item
                    setOnClickFillInIntent(R.id.widget_location_icon, fillInIntent)
                }
            } else {
                return RemoteViews(context.packageName, R.layout.widget_location_icon2).apply {
                    val sp = context?.getSharedPreferences(CHECK_IN_FROM_WIDGET_ACTION, Context.MODE_PRIVATE)
                    val venueName = sp.getString("venueName"+position.toString(), "")
                    if (venueName != "") {
                        setTextViewText(R.id.location_name, venueName)
                    } else {
                        setTextViewText(R.id.location_name, "+")
                    }

                    //unique action for each icon
                    val fillInIntent = Intent().apply {
                        putExtra(WIDGET_ICON_POSITION, position)
                    }
                    setOnClickFillInIntent(R.id.location_name, fillInIntent)
                }
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getViewTypeCount(): Int {
            return count
        }

        override fun onDestroy() {
        }

    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return LocationRemoteViewsFactory(this.applicationContext, intent)
    }
}