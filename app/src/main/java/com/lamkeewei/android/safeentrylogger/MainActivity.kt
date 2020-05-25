package com.lamkeewei.android.safeentrylogger

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.lamkeewei.android.safeentrylogger.ui.ActiveFragment
import com.lamkeewei.android.safeentrylogger.ui.FavoritesFragment
import com.lamkeewei.android.safeentrylogger.ui.HistoryFragment

private val TAB_LABELS = arrayOf("Active", "History", "Favorites")
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pageAdapter = PageAdapter(this)
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = pageAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = TAB_LABELS[position]
        }.attach()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { _ ->
            startActivity(Intent(this, LiveBarcodeScanningActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.allPermissionsGranted(this)) {
            Utils.requestRuntimePermissions(this)
        }
    }

    class PageAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ActiveFragment.newInstance()
                1 -> HistoryFragment.newInstance()
                2 -> FavoritesFragment.newInstance()
                else -> throw ClassNotFoundException("Unknown type of fragment")
            }
        }

    }
}