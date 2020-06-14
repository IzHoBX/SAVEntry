package com.izho.saveentry.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter import androidx.recyclerview.widget.RecyclerView
import com.izho.saveentry.data.Location
import com.izho.saveentry.data.VisitWithLocation
import com.izho.saveentry.databinding.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

sealed class DataItem {
    abstract val id: Long

    data class VisitDataItem(
        val data: VisitWithLocation,
        override val id: Long = data.visit.visitId
    ) : DataItem()

    data class FavoriteDataItem(
        val location: Location,
        override val id: Long
    ) : DataItem()

    class DateHeader(val time: Long) : DataItem() {
        override val id: Long = time
    }
}


enum class VisitListItemType(val typeId: Int) {
    HEADER(0),
    ACTIVE(1),
    HISTORY(2),
    FAVORITE(3),
    LOCATION(4)
}

enum class VisitDataItemHandlerType { DELETE, CHECKOUT, CHECKIN, CONTEXT_MENU, SHOW_DETAILS, CHOOSE }

class VisitDataItemEventHandler(private val handler: (DataItem) -> Unit) {
    fun onClick(visitDataItem: DataItem.VisitDataItem) = handler(visitDataItem)
    fun onClick(favoriteDataItem: DataItem.FavoriteDataItem) = handler(favoriteDataItem)
}

class HeaderViewHolder private constructor(private val binding: HeaderItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(time: Long) {
        binding.time = time
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup) : HeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = HeaderItemBinding
                .inflate(layoutInflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }
}

class ActiveVisitViewHolder private constructor(private val binding: ActiveVisitItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DataItem.VisitDataItem, handlers: Map<VisitDataItemHandlerType, VisitDataItemEventHandler>) {
        binding.dataItem = item

        handlers[VisitDataItemHandlerType.SHOW_DETAILS]?.let { handler ->
            binding.root.setOnClickListener {
                handler.onClick(item)
            }
        }

        handlers[VisitDataItemHandlerType.DELETE]?.let {
            binding.deleteHandler = it
        }

        handlers[VisitDataItemHandlerType.CHECKOUT]?.let {
            binding.checkOutHandler = it
        }

        binding.executePendingBindings()
    }

    companion object {
        private const val TAG = "ActiveVisitViewHolder"

        fun from(parent: ViewGroup) : ActiveVisitViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ActiveVisitItemBinding
                .inflate(layoutInflater, parent, false)
            return ActiveVisitViewHolder(binding)
        }
    }
}

class HistoryVisitViewHolder private constructor(private val binding: HistoryVisitItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DataItem.VisitDataItem, handlers: Map<VisitDataItemHandlerType, VisitDataItemEventHandler>) {
        binding.dataItem = item

        handlers[VisitDataItemHandlerType.CONTEXT_MENU]?.let { handler ->
            binding.root.setOnClickListener { _ ->
                handler.onClick(item)
            }
        }

        binding.executePendingBindings()
    }

    companion object {
        private const val TAG = "HistoryVisitViewHolder"

        fun from(parent: ViewGroup) : HistoryVisitViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = HistoryVisitItemBinding
                .inflate(layoutInflater, parent, false)
            return HistoryVisitViewHolder(binding)
        }
    }
}

class FavoriteLocationViewHolder private constructor(private val binding: FavoriteLocationItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DataItem.FavoriteDataItem,
             handlers: Map<VisitDataItemHandlerType, VisitDataItemEventHandler>) {
        binding.dataItem = item
        handlers[VisitDataItemHandlerType.CHECKIN]?.let {
            binding.checkInHandler = it
        }

        handlers[VisitDataItemHandlerType.CONTEXT_MENU]?.let { handler ->
            binding.root.setOnLongClickListener { _ ->
                handler.onClick(item)
                true
            }
        }

        binding.executePendingBindings()
    }

    companion object {
        private const val TAG = "FavoriteLocationViewHolder"

        fun from(parent: ViewGroup) : FavoriteLocationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = FavoriteLocationItemBinding
                .inflate(layoutInflater, parent, false)
            return FavoriteLocationViewHolder(binding)
        }
    }
}

class LocationViewHolder private constructor(private val binding: LocationItemBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DataItem.FavoriteDataItem,
             handlers: Map<VisitDataItemHandlerType, VisitDataItemEventHandler>) {
        binding.dataItem = item
        handlers[VisitDataItemHandlerType.CHOOSE]?.let { handler ->
            binding.root.setOnClickListener() { _ ->
                handler.onClick(item)
                true
            }
        }

        binding.executePendingBindings()
    }

    companion object {
        private const val TAG = "LocationViewHolder"

        fun from(parent: ViewGroup) : LocationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = LocationItemBinding
                .inflate(layoutInflater, parent, false)
            return LocationViewHolder(binding)
        }
    }
}

class DataItemAdapter(
    private val itemType: VisitListItemType,
    private val handlers: Map<VisitDataItemHandlerType, VisitDataItemEventHandler>
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    private class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    fun submitLocationList(locations: List<Location>) {
        adapterScope.launch {
            val items = locations.mapIndexed{ idx, l ->
                DataItem.FavoriteDataItem(l, idx.toLong())
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun submitVisitList(visits: List<VisitWithLocation>) {
        adapterScope.launch {
            val items: List<DataItem>
            when(itemType) {
                VisitListItemType.ACTIVE -> {
                    items = visits.map { DataItem.VisitDataItem(it) }
                }

                VisitListItemType.HISTORY -> {
                    var prevDay = Long.MAX_VALUE
                    items = visits.fold(mutableListOf<DataItem>()) { list, v ->
                        val currentDay = Calendar.getInstance().apply {
                            time = Date(v.visit.checkInAt)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        if (currentDay < prevDay) {
                            list += DataItem.DateHeader(currentDay)
                            prevDay = currentDay
                        }

                        list += DataItem.VisitDataItem(v)
                        list
                    }
                }

                else -> throw ClassNotFoundException("Unknown item type")
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> HeaderViewHolder.from(parent)
            1 -> ActiveVisitViewHolder.from(parent)
            2 -> HistoryVisitViewHolder.from(parent)
            3 -> FavoriteLocationViewHolder.from(parent)
            4 -> LocationViewHolder.from(parent)
            else -> throw ClassNotFoundException("Unknown item type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = getItem(position)
        when(holder) {
            is ActiveVisitViewHolder -> {
                holder.bind(data as DataItem.VisitDataItem, handlers)
            }
            is HistoryVisitViewHolder -> {
                holder.bind(data as DataItem.VisitDataItem, handlers)
            }
            is HeaderViewHolder -> {
                holder.bind((data as DataItem.DateHeader).time)
            }
            is FavoriteLocationViewHolder -> {
                holder.bind((data as DataItem.FavoriteDataItem), handlers)
            }
            is LocationViewHolder -> {
                holder.bind((data as DataItem.FavoriteDataItem), handlers)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is DataItem.DateHeader -> VisitListItemType.HEADER.typeId
            else -> itemType.typeId
        }
    }
}

