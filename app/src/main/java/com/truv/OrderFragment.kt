package com.truv

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.truv.models.TruvBridgeViewConfig
import com.truv.models.TruvOrderEvent
import com.truv.ui.Title
import com.truv.webview.TruvBridgeView
import com.truv.webview.TruvOrderEventsListener
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class OrderFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val emptyTokenAlert = AlertDialog.Builder(context)
        emptyTokenAlert.setTitle("Invalid order bridge token")
        emptyTokenAlert.setMessage("Please enter a valid order bridge token to open the order page.")
        emptyTokenAlert.setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(primary = Color(0xFF0DAB4C))
                ) {
                    var orderToken by remember { mutableStateOf("") }
                    var widgetVisible by remember { mutableStateOf(false) }

                    if (widgetVisible) {
                        AndroidView(factory = { context ->
                            val serverUrls = viewModel.getServerUrls()
                            TruvBridgeView(context).apply {
                                setConfig(
                                    TruvBridgeViewConfig(
                                        apiUrl = serverUrls.apiUrl,
                                        cdnUrl = serverUrls.cdnUrl,
                                        orderUrl = serverUrls.orderUrl,
                                    )
                                )
                                addOrderEventListener(object : TruvOrderEventsListener {
                                    override fun onEvent(event: TruvOrderEvent) {
                                        viewModel.truvOrderEventListener.onEvent(event)
                                        if (event is TruvOrderEvent.Close) {
                                            requireActivity().runOnUiThread { widgetVisible = false }
                                        }
                                    }
                                })
                                loadOrderUrl(orderToken)
                            }
                        })
                    } else {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Title("Order")
                            OutlinedTextField(
                                value = orderToken,
                                onValueChange = { orderToken = it },
                                label = { Text("Order Bridge Token") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                singleLine = true,
                            )
                            Button(
                                onClick = {
                                    if (orderToken.isBlank()) {
                                        emptyTokenAlert.show()
                                    } else {
                                        widgetVisible = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "OPEN ORDER",
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
