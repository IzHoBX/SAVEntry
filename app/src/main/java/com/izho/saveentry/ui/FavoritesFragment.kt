package com.izho.saveentry.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.izho.saveentry.CheckInOrOutActivity
import com.izho.saveentry.data.Location

import com.izho.saveentry.databinding.FragmentFavoritesBinding
import com.izho.saveentry.viewmodel.FavoritesViewModel
import com.izho.saveentry.viewmodel.PlacesRelatedViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll

open abstract class PlacesRelatedFragment : Fragment() {
    fun presentEditPlaceNameDialog(location:Location, viewModel:PlacesRelatedViewModel) {
        val enterNameField = EditText(activity)
        enterNameField.setHint("Enter a new place name")
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Edit Place Name")
            .setPositiveButton("Done",
                DialogInterface.OnClickListener { dialog, id ->
                    if(enterNameField.text.toString().isNotEmpty()) {
                        viewModel.updateLocationUserDefinedName(location, enterNameField.text.toString())
                        Toast.makeText(activity, "Renamed Successfully", Toast.LENGTH_SHORT).show()
                        refreshBindingForLocation()
                    } else {
                        Toast.makeText(activity, "Please enter a new name", Toast.LENGTH_SHORT).show()
                        presentEditPlaceNameDialog(location, viewModel)
                    }
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
            .setNeutralButton("Reset to Original", DialogInterface.OnClickListener{dialog, id ->
                viewModel.updateLocationUserDefinedName(location, null)
                Toast.makeText(activity, "Reset to Original Name", Toast.LENGTH_SHORT).show()
               refreshBindingForLocation()
            })
            .setView(enterNameField)
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    abstract fun refreshBindingForLocation()
}

class FavoritesFragment : PlacesRelatedFragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(FavoritesViewModel::class.java)
    }

    private lateinit var binding:FragmentFavoritesBinding

    override fun refreshBindingForLocation() {
        binding.recyclerViewFavorites.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoritesBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val checkInHandler = VisitDataItemEventHandler {
            val favoriteDataItem = it as DataItem.FavoriteDataItem
            val intent = Intent(this.activity, CheckInOrOutActivity::class.java)
            intent.putExtra("url", favoriteDataItem.location.url)
            intent.putExtra("venueName", favoriteDataItem.location.venueName)
            intent.putExtra("locationId", favoriteDataItem.location.locationId)
            intent.putExtra("organization", favoriteDataItem.location.organization)
            intent.putExtra("action", "checkIn")
            startActivity(intent)
        }

        val longPressedHandler = VisitDataItemEventHandler { it ->
            val dataItem = it as DataItem.FavoriteDataItem
            val builder = AlertDialog.Builder(this.activity)
            builder
                .setTitle("Select Action")
                .setItems(DIALOG_OPTIONS) { _, which ->
                    when(which) {
                        0 -> viewModel.removeFromFavorite(dataItem.location)
                        1 -> presentEditPlaceNameDialog(dataItem.location, viewModel)
                    }
                }

            builder.create().show()
        }

        val handlers = mapOf(
            VisitDataItemHandlerType.CHECKIN to checkInHandler,
            VisitDataItemHandlerType.CONTEXT_MENU to longPressedHandler
        )
        val listAdapter = DataItemAdapter(VisitListItemType.FAVORITE, handlers)
        binding.recyclerViewFavorites.adapter = listAdapter

        return binding.root
    }

    companion object {
        private val DIALOG_OPTIONS = arrayOf("Remove from Favorite", "Rename Place")
        private const val TAG = "FavoritesFragment"
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }
}
