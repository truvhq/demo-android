package com.citadelapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.citadelapi.product.MainViewModel
import com.citadelapi.ui.Dropdown
import com.citadelapi.ui.DropdownData
import com.citadelapi.ui.Title
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SettingsFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(primary = Color(0xFF0DAB4C))
                ) {
                    SettingsPage(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
fun SettingsPage(
    viewModel: MainViewModel
) {
    val state = viewModel.settingsUIState.collectAsState()
    Column(Modifier.padding(horizontal = 20.dp)) {
        Title("Settings")
        Dropdown(
            value = state.value.env,
            onChange = { viewModel.changeEnv(it) },
            options = arrayOf(
                DropdownData("sandbox", "Sandbox"),
                DropdownData("dev", "Development"),
                DropdownData("prod", "Production"),
            ),
            label = "Environment"
        )
        OutlinedTextField(
            value = state.value.clientId,
            onValueChange = { viewModel.changeClientId(it) },
            label = { Text("Client ID") },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = state.value.sandbox,
            onValueChange = { viewModel.changeSandboxKey(it) },
            label = { Text("Sandbox") },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = state.value.dev,
            onValueChange = { viewModel.changeDevKey(it) },
            label = { Text("Development") },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = state.value.prod,
            onValueChange = { viewModel.changeProdKey(it) },
            label = { Text("Production") },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
