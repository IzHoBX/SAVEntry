package com.lamkeewei.android.safeentrylogger

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lamkeewei.android.safeentrylogger.data.Location
import com.lamkeewei.android.safeentrylogger.data.VisitWithLocation
import com.lamkeewei.android.safeentrylogger.ui.DataItemAdapter
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("visits")
fun RecyclerView.setVisits(visits: List<VisitWithLocation>?) {
    visits?.let { visitList ->
        val visitsAdapter = adapter as DataItemAdapter
        visitsAdapter.submitVisitList(visitList)
    }
}

@BindingAdapter("locations")
fun RecyclerView.setLocations(locations: List<Location>?) {
    locations?.let { it ->
        val visitsAdapter = adapter as DataItemAdapter
        visitsAdapter.submitLocationList(it)
    }
}

@BindingAdapter("titleCase")
fun TextView.setTitleCase(s: String?) {
    s?.let {
        val capitalized = s.split(" ")
            .joinToString(" ") { it.toLowerCase().capitalize() }
        text = capitalized
    }
}

@BindingAdapter("timeString")
fun TextView.setTimeString(epochTime: Long) {
    val fmt = SimpleDateFormat("hh:mm", Locale.getDefault())
    text = fmt.format(Date(epochTime))
}

@BindingAdapter("dateString")
fun TextView.setDateString(epochTime: Long) {
    val fmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    text = fmt.format(Date(epochTime))
}

@BindingAdapter("amPmString")
fun TextView.setAmPmString(epochTime: Long) {
    val fmt = SimpleDateFormat("a", Locale.getDefault())
    text = fmt.format(Date(epochTime))
}

@BindingAdapter("isVisible")
fun View.setIsVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}
