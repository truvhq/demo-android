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

data class BridgeTokenRequest(
    @SerializedName("product_type")
    var productType: String,
    @SerializedName("company_mapping_id")
    var companyMapping: String?,
    @SerializedName("provider_id")
    var provider: String?,
) {}

sealed class BridgeTokenState() {
    object BridgeTokenLoading : BridgeTokenState()
    data class BridgeTokenLoaded(val bridgeToken: String) : BridgeTokenState()
    object BridgeTokenError : BridgeTokenState()
}

data class ProductUIState(
    val productType: String = "income",
    val widgetVisible: Boolean = false,
    val companyMapping: String = "",
    val provider: String = "",
    val routingNumber: String = "123456789",
    val accountNumber: String = "160026001",
    val bankName: String = "TD Bank",
    val accountType: String = "checking"
) {
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
    private val _bridgeTokenState =
        MutableStateFlow<BridgeTokenState>(BridgeTokenState.BridgeTokenLoading)
    val bridgeTokenState: StateFlow<BridgeTokenState> = _bridgeTokenState

    private val _productUIState = MutableStateFlow<ProductUIState>(ProductUIState())
    val productUIState: StateFlow<ProductUIState> = _productUIState

    fun changeProduct(productType: String) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(productType = productType)

        fetchBridgeToken();
    }

    fun showWidget() = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(widgetVisible = true)
        log("Opening widget with bridge token ${bridgeTokenState.value}")
    }

    fun hideWidget() = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(widgetVisible = false)
        log("Closing widget")
    }

    fun changeCompanyMapping(mapping: String) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(companyMapping = mapping)

        fetchBridgeToken()
    }

    fun changeProvider(provider: String) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(provider = provider)

        fetchBridgeToken()
    }

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

        fetchBridgeToken()
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

        _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoading
        val gson = Gson()
        val body = gson.toJson(
            BridgeTokenRequest(
                productUIState.value.productType,
                if (productUIState.value.companyMapping == "") null else productUIState.value.companyMapping,
                if (productUIState.value.provider == "") null else productUIState.value.provider,
            )
        )

        log("Fetching bridge token with data: $body")

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
            .body(body)
            .responseObject<BridgeTokenResponse> { _, _, result ->
                val bridgeToken = result.component1()?.bridgeToken
                if (bridgeToken != null) {
                    _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoaded(bridgeToken)
                    log("Fetched bridge token ${bridgeToken}")
                } else {
                    log("Bridge token error ${result.component2()?.message}")
                    _bridgeTokenState.value = BridgeTokenState.BridgeTokenError
                }
            }
    }
}