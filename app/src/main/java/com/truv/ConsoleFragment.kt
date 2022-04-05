package com.truv

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.*
import com.truv.product.MainViewModel
import androidx.lifecycle.ViewModelProvider
import com.truv.ui.Title
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class ConsoleFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.consoleState.collectAsState()
                val scrollState = rememberScrollState(Int.MAX_VALUE)

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Title("Console")
                    Text(state.value, lineHeight = 30.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}