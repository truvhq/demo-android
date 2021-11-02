package com.citadelapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.citadelapi.product.MainViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect


@ExperimentalCoroutinesApi
class ConsoleFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_console, container, false)
        val console = view.findViewById<TextView>(R.id.console)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        lifecycleScope.launchWhenStarted {
            viewModel.consoleState.collect {
                console.text = it
            }
        }

        return view
    }
}