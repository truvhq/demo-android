package com.truv

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi

object Tabs {
    const val PRODUCT = 0
    const val ORDER = 1
    const val CONSOLE = 2
    const val SETTINGS = 3
}

@ExperimentalCoroutinesApi
class PagerAgapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Tabs.ORDER -> OrderFragment()
            Tabs.CONSOLE -> ConsoleFragment()
            Tabs.SETTINGS -> SettingsFragment()
            else -> ProductFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}