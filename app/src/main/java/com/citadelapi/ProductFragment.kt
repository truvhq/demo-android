package com.citadelapi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.citadelapi.product.BridgeTokenState
import com.citadelapi.product.MainViewModel
import com.citadelapi.ui.AdditionalSettings
import com.citadelapi.ui.Product
import com.citadelapi.ui.Title
import kotlinx.coroutines.ExperimentalCoroutinesApi
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


    @ExperimentalUnitApi
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                val productState = viewModel.productUIState.collectAsState()
                val bridgeTokenState = viewModel.bridgeTokenState.collectAsState()

                if (productState.value.widgetVisible) {
                    AndroidView(factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            settings.allowContentAccess = true
                            settings.domStorageEnabled = true
                            addJavascriptInterface(WebAppInterface(), "citadelInterface")
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                        }
                    }, update = {
                        val state = bridgeTokenState.value
                        if (state is BridgeTokenState.BridgeTokenLoaded) {
                            it.loadUrl("https://cdn.citadelid.com/mobile.html?bridge_token=${state.bridgeToken}")
                        }
                    })
                } else {
                    MaterialTheme(
                        colors = MaterialTheme.colors.copy(primary = Color(0xFF0DAB4C))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Column {
                                Title("Product")
                                Product(
                                    product = productState.value.productType,
                                    onChange = { viewModel.changeProduct(it) }
                                )
                                AdditionalSettings(viewModel = viewModel)
                            }
                            Button(
                                onClick = { viewModel.showWidget() },
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