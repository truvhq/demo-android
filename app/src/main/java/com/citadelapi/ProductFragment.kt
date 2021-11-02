package com.citadelapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.citadelapi.product.MainViewModel
import com.citadelapi.product.ProductUIState
import kotlinx.coroutines.flow.collect
import org.json.JSONObject

val TAG = "PRODUCT TAB"

class ProductFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    lateinit var webview: WebView
    lateinit var webviewLayout: View
    lateinit var productLayout: View

    var bridgeToken = ""

    inner class WebAppInterface {

        @JavascriptInterface
        fun onSuccess(payload: String) {
            Log.d(TAG, "onSuccess invoked $payload")
            this@ProductFragment.hideWebview()
        }

        @JavascriptInterface
        fun onEvent(event: String) {
            Log.d(TAG, "onEvent invoked $event")

            viewModel.log(event)

            val json = JSONObject(event)
            val type = json.getString("event_type")

            when (type) {
                "CLOSE" -> this@ProductFragment.hideWebview()
            }
        }

        @JavascriptInterface
        fun onClose() {
            Log.d(TAG, "onClose invoked")
            this@ProductFragment.hideWebview()
        }

        @JavascriptInterface
        fun onLoad() {
            Log.d(TAG, "onLoad invoked")
        }

        @JavascriptInterface
        fun onError() {
            Log.d(TAG, "onError invoked")
        }
    }

    private fun showWebview() {
        val url = "https://cdn.citadelid.com/mobile.html?bridge_token=${bridgeToken}"
        println("loading widget with url $url")
        webview.loadUrl(url)

        webview.addJavascriptInterface(WebAppInterface(), "citadelInterface")

        Log.d(TAG, "Set Widget visible")

        webviewLayout.visibility = View.VISIBLE
        productLayout.visibility = View.INVISIBLE

        Log.d(TAG, "Widget is visible")
    }

    private fun hideWebview() {
        activity?.runOnUiThread {
            Log.d(TAG, "Set Widget invisible")

            productLayout.visibility = View.VISIBLE
            webviewLayout.visibility = View.INVISIBLE

            Log.d(TAG, "Widget is hidden")
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_product, container, false)
        webview = rootView.findViewById(R.id.webview) as WebView
        webviewLayout = rootView.findViewById(R.id.webviewLayout)
        productLayout = rootView.findViewById(R.id.productLayout)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val webSettings = webview.getSettings()

        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true

        val button = rootView.findViewById<Button>(R.id.openWidget)
        button.setOnClickListener {
            showWebview()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.productUIState.collect {
                when (it) {
                    ProductUIState.BridgeTokenLoading -> {
                        button.isEnabled = false
                    }
                    is ProductUIState.BridgeTokenLoaded -> {
                        bridgeToken = it.bridgeToken
                        button.isEnabled = true
                    }
                    ProductUIState.BridgeTokenError -> {
                        button.isEnabled = true
                    }
                }
            }
        }

        return rootView
    }
}