package com.izho.saveentry.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.izho.saveentry.CheckInOrOutActivity
import com.izho.saveentry.ShowPassImageActivity

import com.izho.saveentry.databinding.FragmentActiveBinding
import com.izho.saveentry.viewmodel.ActiveViewModel

class ActiveFragment : PlacesRelatedFragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(ActiveViewModel::class.java)
    }

    private lateinit var binding: FragmentActiveBinding

    override fun refreshBindingForLocation() {
        binding.recyclerViewActive.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveBinding.inflate(
            layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val deleteHandler = VisitDataItemEventHandler { it ->
            val dataItem = it as DataItem.VisitDataItem
            // Clear the associate notification
            with(NotificationManagerCompat.from(requireContext())) {
                cancel(dataItem.data.visit.notificationId)
            }

            viewModel.deleteActiveVisit(dataItem.data.visit)
        }

        val checkOutHandler = VisitDataItemEventHandler {
            val dataItem = it as DataItem.VisitDataItem
            val intent = Intent(activity, CheckInOrOutActivity::class.java)
            intent.putExtra("action", "checkOut")
            intent.putExtra("visitId", dataItem.data.visit.visitId)
            intent.putExtra("url", dataItem.data.location.url)
            startActivity(intent)
        }

        val showPassImageHandler = VisitDataItemEventHandler {
            val dataItem = it as DataItem.VisitDataItem
            val intent = Intent(activity, ShowPassImageActivity::class.java)
            intent.putExtra("passImagePath", dataItem.data.visit.passImagePath)
            startActivity(intent)
        }

        val longPressedHandler = VisitDataItemEventHandler { it ->
            val dataItem = it as DataItem.VisitDataItem
            val builder = AlertDialog.Builder(this.activity)
            builder
                .setTitle("Select Action")
                .setItems(DIALOG_OPTIONS) { _, which ->
                    when(which) {
                        0 -> presentEditPlaceNameDialog(dataItem.data.location, viewModel)
                    }
                }

            builder.create().show()
        }

        val handlers = mapOf(
            VisitDataItemHandlerType.DELETE to deleteHandler,
            VisitDataItemHandlerType.CHECKOUT to checkOutHandler,
            VisitDataItemHandlerType.SHOW_DETAILS to showPassImageHandler,
            VisitDataItemHandlerType.CONTEXT_MENU to longPressedHandler
        )

        val listAdapter = DataItemAdapter(VisitListItemType.ACTIVE, handlers)
        binding.recyclerViewActive.adapter = listAdapter

        listAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.recyclerViewActive.scrollToPosition(0)
            }
        })

        return binding.root
    }

    companion object {
        private const val TAG = "ActiveFragment"
        private val DIALOG_OPTIONS = arrayOf("Rename Place")

        @JvmStatic
        fun newInstance() = ActiveFragment()
    }
}
