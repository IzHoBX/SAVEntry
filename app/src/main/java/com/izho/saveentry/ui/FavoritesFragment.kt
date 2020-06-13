package com.izho.saveentry.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.izho.saveentry.CheckInOrOutActivity

import com.izho.saveentry.databinding.FragmentFavoritesBinding
import com.izho.saveentry.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(FavoritesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentFavoritesBinding.inflate(layoutInflater, container, false)
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
        private val DIALOG_OPTIONS = arrayOf("Remove from Favorite")
        private const val TAG = "FavoritesFragment"
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }
}
