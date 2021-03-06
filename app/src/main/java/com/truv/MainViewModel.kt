package com.truv

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.truv.models.TruvEventPayload
import com.truv.models.TruvSuccessPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BridgeTokenResponse(
    @SerializedName("bridge_token")
    var bridgeToken: String,
) {

    class Deserializer : ResponseDeserializable<BridgeTokenResponse> {
        override fun deserialize(content: String): BridgeTokenResponse =
            Gson().fromJson(content, BridgeTokenResponse::class.java)
    }
}

data class AccountState(
    @SerializedName("account_number")
    var accountNumber: String = "160026001",
    @SerializedName("routing_number")
    val routingNumber: String = "123456789",
    @SerializedName("bank_name")
    val bankName: String = "TD Bank",
    @SerializedName("account_type")
    val accountType: String = "checking",
    @SerializedName("deposit_type")
    val depositType: String = "amount",
    @SerializedName("deposit_value")
    val depositValue: Int = 1
) {}

data class BridgeTokenRequest(
    @SerializedName("product_type")
    var productType: String,
    @SerializedName("company_mapping_id")
    var companyMapping: String?,
    @SerializedName("provider_id")
    var provider: String?,
    @SerializedName("account")
    var account: AccountState?,
) {}

sealed class BridgeTokenState() {
    object BridgeTokenLoading : BridgeTokenState()
    data class BridgeTokenLoaded(val bridgeToken: String) : BridgeTokenState()
    object BridgeTokenError : BridgeTokenState()
}

data class ProductUIState(
    val productType: String = "income",
    val widgetVisible: Boolean = false,
    val companyMapping: String? = null,
    val provider: String? = null,
    val accountState: AccountState = AccountState(),
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

    private val _activeTabState =
        MutableStateFlow<Int>(0)
    val activeTabState: StateFlow<Int> = _activeTabState

    fun setTab(tab: Int) = viewModelScope.launch {
        _activeTabState.value = tab
    }

    private val _bridgeTokenState =
        MutableStateFlow<BridgeTokenState>(BridgeTokenState.BridgeTokenLoading)
    val bridgeTokenState: StateFlow<BridgeTokenState> = _bridgeTokenState

    private val _productUIState = MutableStateFlow(ProductUIState())
    val productUIState: StateFlow<ProductUIState> = _productUIState

    val truvBridgeEventListener = object : TruvEventsListener {

        override fun onSuccess(payload: TruvSuccessPayload) {
            log("onSuccess callback invoked")
            hideWidget()
        }

        override fun onEvent(event: TruvEventPayload.EventType) {
            if (event == TruvEventPayload.EventType.CLOSE) {
                hideWidget()
            }
        }

        override fun onClose() {
            log("onClose callback invoked")
            hideWidget()
        }

        override fun onLoad() {
            log("onLoad callback invoked")
        }

        override fun onError() {
            log("onError callback invoked")
        }

    }

    fun changeProduct(productType: String) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(productType = productType)

        fetchBridgeToken()
    }

    fun showWidget() = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(widgetVisible = true)
        log("Opening widget with bridge token ${bridgeTokenState.value}")
    }

    fun hideWidget() = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(widgetVisible = false)
        log("Closing widget")
    }

    fun changeCompanyMapping(mapping: String?) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(companyMapping = mapping)

        fetchBridgeToken()
    }

    fun changeProvider(provider: String?) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(provider = provider)

        fetchBridgeToken()
    }

    fun changeAccountState(accountState: AccountState) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(accountState = accountState)

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

        fetchBridgeTokenThrottle()
    }

    fun changeDevKey(devKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(dev = devKey)
        val p = preferences.edit()
        p.putString("dev", devKey)
        p.apply()

        fetchBridgeTokenThrottle()
    }

    fun changeProdKey(prodKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(prod = prodKey)
        val p = preferences.edit()
        p.putString("prod", prodKey)
        p.apply()

        fetchBridgeTokenThrottle()
    }

    fun changeSandboxKey(sandboxKey: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(sandbox = sandboxKey)
        val p = preferences.edit()
        p.putString("sandbox", sandboxKey)
        p.apply()

        fetchBridgeTokenThrottle()
    }

    private var bridgeTokenJob: Job? = null

    private fun fetchBridgeTokenThrottle() {
        bridgeTokenJob?.cancel()
        bridgeTokenJob = viewModelScope.launch {
            delay(1000)
            fetchBridgeToken()
        }
    }

    fun fetchBridgeToken() {
        Log.d("ViewModel", "execute fetchBridgeToken")

        val env = settingsUIState.value.env
        val clientId = settingsUIState.value.clientId
        val sandboxKey = settingsUIState.value.sandbox
        val devKey = settingsUIState.value.dev
        val prodKey = settingsUIState.value.prod

        val secret = when (env) {
            "dev" -> devKey
            "prod" -> prodKey
            else -> sandboxKey
        }

        if (secret == "") {
            Log.d("ViewModel", "stop fetching bridgetToken, secret is empty")
            return
        }

        _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoading
        val state = productUIState.value
        val gson = Gson()
        val body = gson.toJson(
            BridgeTokenRequest(
                productUIState.value.productType,
                state.companyMapping,
                state.provider,
                if (state.productType === "deposit_switch" || state.productType === "pll") state.accountState else null
            )
        )

        log("Fetching bridge token with data: $body")

        "https://prod.citadelid.com/v1/bridge-tokens/"
            .httpPost()
            .header(
                mapOf(
                    "Content-Type" to "application/json",
                    "X-Access-Client-Id" to clientId,
                    "X-Access-Secret" to secret
                )
            )
            .body(body)
            .responseObject<BridgeTokenResponse> { _, _, result ->
                val bridgeToken = result.component1()?.bridgeToken
                if (bridgeToken != null) {
                    _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoaded(bridgeToken)
                    log("Fetched bridge token $bridgeToken")
                } else {
                    log("Bridge token error ${result.component2()?.message}")
                    log("Bridge token error body ${result.component2()?.errorData?.toString(Charsets.UTF_8)}")
                    _bridgeTokenState.value = BridgeTokenState.BridgeTokenError
                }
            }
    }
}