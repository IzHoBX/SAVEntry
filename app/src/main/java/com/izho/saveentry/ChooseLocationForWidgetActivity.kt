package com.izho.saveentry

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.izho.saveentry.databinding.LocationDataBindingBinding
import com.izho.saveentry.databinding.LocationItemBinding
import com.izho.saveentry.ui.*
import com.izho.saveentry.viewmodel.ChooseLocationForWidgetViewModel
import com.izho.saveentry.viewmodel.FavoritesViewModel

class ChooseLocationForWidgetActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(ChooseLocationForWidgetViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_location_for_widget)
        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            title = "Choose a location"
        }
        setSupportActionBar(toolbar)

        val binding = LocationDataBindingBinding.bind(findViewById(R.id.dataBinding))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val chooseHandler = VisitDataItemEventHandler {
            val iconPosition = intent.getIntExtra("position", -1)
            if (iconPosition != -1) {
                val sp = getSharedPreferences(CHECK_IN_FROM_WIDGET_ACTION, Context.MODE_PRIVATE)
                val location = (it as DataItem.FavoriteDataItem).location
                sp.edit().putString("url"+iconPosition.toString(), location.url).apply()
                sp.edit().putString("venueName"+iconPosition.toString(), location.venueName).apply()
                sp.edit().putString("locationId"+iconPosition.toString(), location.locationId).apply()
                sp.edit().putString("organization"+iconPosition.toString(), location.organization).apply()
                Toast.makeText(this, "You can now check in to ${location.venueName} from Home Screen", Toast.LENGTH_LONG).show()
                val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                var intent = Intent(this, HomeWidgetProvider::class.java)
                intent.setAction(UPDATE_WIDGET)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                sendBroadcast(intent)
                intent = Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }

        val handlers = mapOf(
            VisitDataItemHandlerType.CHOOSE to chooseHandler
        )
        val listAdapter = DataItemAdapter(VisitListItemType.LOCATION, handlers)
        findViewById<RecyclerView>(R.id.recycler_view_all_Locations).adapter = listAdapter
    }
}