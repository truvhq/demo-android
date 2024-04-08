package com.truv

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

@kotlinx.coroutines.ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        tabLayout = findViewById(R.id.tabLayout)
        pager = findViewById(R.id.pager)
        pager.isUserInputEnabled = false

        val adapter = PagerAgapter(supportFragmentManager, lifecycle)

        val sharedPreference =  getSharedPreferences("TRUV_SETTINGS", Context.MODE_PRIVATE)
        viewModel.init(sharedPreference)

        pager.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("Product").setIcon(R.drawable.ic_product))
        tabLayout.addTab(tabLayout.newTab().setText("Console").setIcon(R.drawable.ic_console))
        tabLayout.addTab(tabLayout.newTab().setText("Settings").setIcon(R.drawable.ic_settings))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        lifecycleScope.launchWhenStarted {
            viewModel.activeTabState.collect {
                pager.currentItem = it
                tabLayout.selectTab(tabLayout.getTabAt(it))
            }
        }
    }
}