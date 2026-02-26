package com.truv

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PagerAgapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> OrderFragment()
            2 -> ConsoleFragment()
            3 -> SettingsFragment()
            else -> ProductFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}