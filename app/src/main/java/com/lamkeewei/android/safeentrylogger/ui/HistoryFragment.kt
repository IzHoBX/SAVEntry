package com.lamkeewei.android.safeentrylogger.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.lamkeewei.android.safeentrylogger.CheckInOrOutActivity

import com.lamkeewei.android.safeentrylogger.databinding.FragmentHistoryBinding
import com.lamkeewei.android.safeentrylogger.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(HistoryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHistoryBinding
            .inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val longPressedHandler = VisitDataItemEventHandler { it ->
            val dataItem = it as DataItem.VisitDataItem
            val builder = AlertDialog.Builder(this.activity)
            builder
                .setTitle("Select Action")
                .setItems(DIALOG_OPTIONS) { _, which ->
                    when(which) {
                        0 -> viewModel.addToFavorite(dataItem.data.location)
                        1 -> {
                            val intent = Intent(this.activity, CheckInOrOutActivity::class.java)
                            intent.putExtra("url", dataItem.data.location.url)
                            intent.putExtra("action", "checkIn")
                            startActivity(intent)
                        }
                        2 -> viewModel.deleteVisit(dataItem.data.visit)
                    }
                }

            builder.create().show()
        }
        val handlers = mapOf(
            VisitDataItemHandlerType.CONTEXT_MENU to longPressedHandler
        )
        val listAdapter = DataItemAdapter(VisitListItemType.HISTORY, handlers)
        binding.recyclerViewHistory.adapter = listAdapter

        return binding.root
    }

    companion object {
        private const val TAG = "HistoryFragment"
        private val DIALOG_OPTIONS = arrayOf(
            "Add Location to Favorite", "Check In to Location", "Delete Visit")

        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}
