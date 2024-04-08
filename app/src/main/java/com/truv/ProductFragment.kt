package com.truv

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.truv.ui.*
import com.truv.webview.TruvBridgeView
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class ProductFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        var bridgeView: TruvBridgeView? = null
        val alert = AlertDialog.Builder(context)
        alert.setTitle("Can’t open Truv Bridge")
        alert.setMessage("Add a key or change the environment in the settings to run Truv Bridge.")
        alert.setNeutralButton("Open settings") { _, _ ->
            viewModel.setTab(2)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.productUIState.collect {
                if (!it.widgetVisible) {
                    bridgeView = null
                }
            }
        }

        var activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
           bridgeView?.onActivityResultListener(it)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(primary = Color(0xFF0DAB4C))
                ) {
                    val productState = viewModel.productUIState.collectAsState()
                    val bridgeTokenState = viewModel.bridgeTokenState.collectAsState()

                    if (productState.value.widgetVisible) {
                        AndroidView(factory = {
                            bridgeView ?: TruvBridgeView(it).apply {
                                addEventListener(viewModel.truvBridgeEventListener)
                                addActivityForResultLauncher(activityResultLauncher)
                                bridgeView = this
                            }
                        }, update = {
                            val state = bridgeTokenState.value
                            if (state is BridgeTokenState.BridgeTokenLoaded
                                && !it.hasBridgeToken(state.bridgeToken)
                            ) {
                                Log.d(
                                    TAG,
                                    "update webview, url: ${it.currentUrl} token: ${state.bridgeToken}"
                                )
                                it.loadBridgeTokenUrl(bridgeToken = state.bridgeToken)
                            }
                        })
                    } else {
                        val scrollState = rememberScrollState(0)
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Column {
                                Title("Product")
                                Dropdown(
                                    value = productState.value.productType,
                                    onChange = { viewModel.changeProduct(it) },
                                    options = arrayOf(
                                        DropdownData("income", "Income"),
                                        DropdownData("employment", "Employment"),
                                        DropdownData("deposit_switch", "Direct Deposit Switch"),
                                        DropdownData("pll", "Paycheck Linked Loan"),
                                        DropdownData("admin", "Employee Directory"),
                                        DropdownData("admin", "Payroll History"),
                                    ),
                                    label = "Product"
                                )
                                AdditionalSettings(viewModel = viewModel)
                            }
                            Button(
                                onClick = { if (bridgeTokenState.value is BridgeTokenState.BridgeTokenLoaded) viewModel.showWidget() else alert.show() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "OPEN TRUV BRIDGE",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "PRODUCT TAB"
    }

}