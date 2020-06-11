package com.izho.saveentry

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.izho.saveentry.data.getAppDatabase
import com.izho.saveentry.ui.ActiveFragment
import com.izho.saveentry.ui.FavoritesFragment
import com.izho.saveentry.ui.HistoryFragment

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

        setSelectedTab()

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

        setSelectedTab()
    }

    fun setSelectedTab() {
        val tabs: TabLayout = findViewById(R.id.tabs)
        val database = getAppDatabase(this, resetDb = false)
        val activeVisits = database.dao.getAllActiveVisitWithLocation().observe(this, Observer { activeVisits ->
            if (activeVisits == null || activeVisits.isEmpty()) {
                tabs.selectTab(tabs.getTabAt(TAB_LABELS.indexOf("Favorites")))
            }
        })
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