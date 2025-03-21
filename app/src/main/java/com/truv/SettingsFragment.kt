package com.truv

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.truv.ui.Dropdown
import com.truv.ui.DropdownData
import com.truv.ui.PasswordField
import com.truv.ui.Title
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
    val scrollState = rememberScrollState(0)
    Column(Modifier.padding(horizontal = 20.dp).verticalScroll(scrollState)) {
        Title("Settings")
        Dropdown(
            value = state.value.server,
            onChange = { viewModel.changeServer(it) },
            options = arrayOf(
                DropdownData("dev", "Development"),
                DropdownData("stage", "Stage"),
                DropdownData("prod", "Production"),
                DropdownData("local", "Local (check README.md in citadel_frontend repo)"),
            ),
            label = "Server"
        )
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
            singleLine = true,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Access keys",
            fontSize = 16.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.padding(top = 24.dp)
        )
        PasswordField(
            value = state.value.sandbox,
            onValueChange = { viewModel.changeSandboxKey(it) },
            label = { Text("Sandbox") },
        )
        PasswordField(
            value = state.value.dev,
            onValueChange = { viewModel.changeDevKey(it) },
            label = { Text("Development") },
        )
        PasswordField(
            value = state.value.prod,
            onValueChange = { viewModel.changeProdKey(it) },
            label = { Text("Production") },
        )
    }
}
