package com.citadelapi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
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
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.citadelapi.product.BridgeTokenState
import com.citadelapi.product.MainViewModel
import com.citadelapi.ui.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import org.json.JSONObject

val TAG = "PRODUCT TAB"

@ExperimentalCoroutinesApi
class ProductFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    inner class WebAppInterface {

        @JavascriptInterface
        fun onSuccess(payload: String) {
            Log.d(TAG, "onSuccess invoked $payload")
            viewModel.log("onSuccess callback invoked")
            viewModel.hideWidget()
        }

        @JavascriptInterface
        fun onEvent(event: String) {
            Log.d(TAG, "onEvent invoked $event")

            viewModel.log(event)

            val json = JSONObject(event)
            val type = json.getString("event_type")

            when (type) {
                "CLOSE" -> viewModel.hideWidget()
            }
        }

        @JavascriptInterface
        fun onClose() {
            Log.d(TAG, "onClose invoked")
            viewModel.log("onClose callback invoked")
            viewModel.hideWidget()
        }

        @JavascriptInterface
        fun onLoad() {
            Log.d(TAG, "onLoad invoked")
            viewModel.log("onLoad callback invoked")
        }

        @JavascriptInterface
        fun onError() {
            Log.d(TAG, "onError invoked")
            viewModel.log("onError callback invoked")
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val alert = AlertDialog.Builder(context)
        alert.setTitle("Can’t open Citadel Bridge")
        alert.setMessage("Add a key or change the environment in the settings to run Citadel Bridge.")
        alert.setNeutralButton("Open settings") { _, _ ->
            viewModel.setTab(2)
        }

        var cachedWebview: WebView? = null;

        lifecycleScope.launchWhenStarted {
            viewModel.productUIState.collect {
                if (!it.widgetVisible) {
                    cachedWebview = null
                }
            }
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
                            cachedWebview ?: WebView(it).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val i = Intent(Intent.ACTION_VIEW)
                                        i.data = request?.url
                                        startActivity(i)
                                        return true
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        Toast.makeText(getActivity(), "Your Internet Connection May not be active", Toast.LENGTH_LONG).show();
                                        viewModel.hideWidget()
                                    }
                                }
                                settings.javaScriptEnabled = true
                                settings.allowContentAccess = true
                                settings.domStorageEnabled = true
                                addJavascriptInterface(WebAppInterface(), "citadelInterface")
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                cachedWebview = this
                            }
                        }, update = {
                            val state = bridgeTokenState.value
                            if (state is BridgeTokenState.BridgeTokenLoaded && it.url?.contains(
                                    state.bridgeToken
                                ) != true
                            ) {
                                Log.d(
                                    TAG,
                                    "update webview, url: ${it.url} token: ${state.bridgeToken}"
                                )
                                it.loadUrl("https://cdn.citadelid.com/mobile.html?bridge_token=${state.bridgeToken}")
                            }
                        })
                    } else {
                        val scrollState = rememberScrollState(0)
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(8.dp).verticalScroll(scrollState)
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
                                    "OPEN CITADEL BRIDGE",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}