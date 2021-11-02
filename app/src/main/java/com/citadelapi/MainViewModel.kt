package com.citadelapi.product

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BridgeTokenResponse(
    @SerializedName("bridge_token")
    var bridgeToken: String,
) {

    class Deserializer : ResponseDeserializable<BridgeTokenResponse> {
        override fun deserialize(content: String): BridgeTokenResponse =
            Gson().fromJson(content, BridgeTokenResponse::class.java)
    }
}

sealed class ProductUIState {
    object BridgeTokenLoading : ProductUIState()

    data class BridgeTokenLoaded(val bridgeToken: String) : ProductUIState()
    object BridgeTokenError : ProductUIState()
}

data class SettingsUIState(
    val env: String = "",
    val clientId: String = "",
    val dev: String = "",
    val sandbox: String = "",
    val prod: String = ""
) {

}

@ExperimentalCoroutinesApi
class MainViewModel : ViewModel() {
    private lateinit var preferences: SharedPreferences
    private val _productUIState =
        MutableStateFlow<ProductUIState>(ProductUIState.BridgeTokenLoading)
    val productUIState: StateFlow<ProductUIState> = _productUIState

    private val _consoleState = MutableStateFlow<String>("")
    val consoleState: StateFlow<String> = _consoleState

    fun log(value: String) = viewModelScope.launch {
        _consoleState.value = "${_consoleState.value}\n${value}"
    }

    fun init(preferences: SharedPreferences) {
        this.preferences = preferences

        val env = preferences.getString("env", "sandbox")
        val sandboxKey = preferences.getString("sandbox", "")
        val developmentKey = preferences.getString("dev", "")
        val productionKey = preferences.getString("prod", "")
        val clientId = preferences.getString("client_id", "")

        _settingsUIState.value =
            settingsUIState.value.copy(
                env = env!!,
                sandbox = sandboxKey!!,
                dev = developmentKey!!,
                prod = productionKey!!,
                clientId = clientId!!,
            )
    }

    private val _settingsUIState =
        MutableStateFlow(SettingsUIState())
    val settingsUIState: StateFlow<SettingsUIState> = _settingsUIState

    fun changeEnv(env: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(env = env)
        val p = preferences.edit()
        p.putString("env", env)
        p.apply()
        fetchBridgeToken()
    }

    fun changeClientId(clientId: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(clientId = clientId)
        val p = preferences.edit()
        p.putString("client_id", clientId)
        p.apply()

        fetchBridgeToken()
    }

    fun changeDevKey(devKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(dev = devKey)
        val p = preferences.edit()
        p.putString("dev", devKey)
        p.apply()
    }

    fun changeProdKey(prodKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(prod = prodKey)
        val p = preferences.edit()
        p.putString("prod", prodKey)
        p.apply()
    }

    fun changeSandboxKey(sandboxKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(sandbox = sandboxKey)
        val p = preferences.edit()
        p.putString("sandbox", sandboxKey)
        p.apply()
    }

    fun fetchBridgeToken() {
        val env = settingsUIState.value.env
        val clientId = settingsUIState.value.clientId
        val sandboxKey = settingsUIState.value.sandbox
        val devKey = settingsUIState.value.dev
        val prodKey = settingsUIState.value.prod

        _productUIState.value = ProductUIState.BridgeTokenLoading

        "https://prod.citadelid.com/v1/bridge-tokens/"
            .httpPost()
            .header(
                mapOf(
                    "Content-Type" to "application/json",
                    "X-Access-Client-Id" to clientId,
                    "X-Access-Secret" to when (env) {
                        "dev" -> devKey
                        "prod" -> prodKey
                        else -> sandboxKey
                    }
                )
            )
            .body("""{"product_type": "income"}""")
            .responseObject<BridgeTokenResponse> { request, response, result ->
                val bridgeToken = result.component1()?.bridgeToken
                if (bridgeToken != null) {
                    _productUIState.value = ProductUIState.BridgeTokenLoaded(bridgeToken)
                } else {
                    _productUIState.value = ProductUIState.BridgeTokenError
                }
            }
    }
}