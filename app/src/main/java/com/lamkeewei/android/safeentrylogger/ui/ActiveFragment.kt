package com.lamkeewei.android.safeentrylogger.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.lamkeewei.android.safeentrylogger.CheckInOrOutActivity
import com.lamkeewei.android.safeentrylogger.ShowPassImageActivity

import com.lamkeewei.android.safeentrylogger.databinding.FragmentActiveBinding
import com.lamkeewei.android.safeentrylogger.viewmodel.ActiveViewModel

class ActiveFragment : Fragment() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(ActiveViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentActiveBinding.inflate(
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

        val handlers = mapOf(
            VisitDataItemHandlerType.DELETE to deleteHandler,
            VisitDataItemHandlerType.CHECKOUT to checkOutHandler,
            VisitDataItemHandlerType.SHOW_DETAILS to showPassImageHandler
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

        @JvmStatic
        fun newInstance() = ActiveFragment()
    }
}
