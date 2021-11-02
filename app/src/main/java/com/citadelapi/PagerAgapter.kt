package com.citadelapi

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAgapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ConsoleFragment()
            2 -> SettingsFragment()
            else -> ProductFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}