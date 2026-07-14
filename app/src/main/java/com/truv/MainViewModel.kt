package com.truv

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import com.truv.models.TruvEventPayload
import com.truv.models.TruvSuccessPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import com.truv.api.TruvApiClient
import com.truv.models.TruvOrderEvent
import com.truv.webview.TruvEventsListener
import com.truv.webview.TruvOrderEventsListener

data class AccountState(
    @SerializedName("account_number") var accountNumber: String = "160026001",
    @SerializedName("routing_number") val routingNumber: String = "123456789",
    @SerializedName("bank_name") val bankName: String = "TD Bank",
    @SerializedName("bank_address") val bankAddress: String = "357 Kings Hwy N, Cherry Hill, NJ 08034, USA",
    @SerializedName("account_type") val accountType: String = "checking",
    @SerializedName("deposit_type") val depositType: String = "amount",
    @SerializedName("deposit_value") val depositValue: Int = 1
) {}

data class BridgeTokenRequest(
    @SerializedName("product_type") var productType: String,
    @SerializedName("company_mapping_id") var companyMapping: String?,
    @SerializedName("provider_id") var provider: String?,
    @SerializedName("account") var account: AccountState?,
) {}

sealed class BridgeTokenState() {
    object BridgeTokenIdle : BridgeTokenState()
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
) {}

data class SettingsUIState(
    val env: String = "",
    val server: String = "",
    val clientId: String = "",
    val dev: String = "",
    val sandbox: String = "",
    val prod: String = "",
) {

}

data class OrderUIState(
    val token: String = "",
    val widgetVisible: Boolean = false,
)

data class ServerUrls(
    val apiUrl: String,
    val cdnUrl: String,
    val orderUrl: String,
)

@ExperimentalCoroutinesApi
class MainViewModel : ViewModel() {
    private lateinit var preferences: SharedPreferences

    private val _activeTabState = MutableStateFlow<Int>(0)
    val activeTabState: StateFlow<Int> = _activeTabState

    fun setTab(tab: Int) = viewModelScope.launch {
        _activeTabState.value = tab
    }

    private val _bridgeTokenState =
        MutableStateFlow<BridgeTokenState>(BridgeTokenState.BridgeTokenIdle)
    val bridgeTokenState: StateFlow<BridgeTokenState> = _bridgeTokenState

    private val _bridgeErrorEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val bridgeErrorEvents: SharedFlow<Unit> = _bridgeErrorEvents

    private val _productUIState = MutableStateFlow(ProductUIState())
    val productUIState: StateFlow<ProductUIState> = _productUIState

    val truvBridgeEventListener = object : TruvEventsListener {

        override fun onSuccess(payload: TruvSuccessPayload) {
            log("bridge: onSuccess callback invoked")
        }

        override fun onEvent(event: TruvEventPayload) {
            log("bridge: onEvent callback invoked: ${event.eventType} ($event)")
        }

        override fun onClose() {
            log("bridge: onClose callback invoked")
            hideWidget()
        }

        override fun onLoad() {
            log("bridge: onLoad callback invoked")
        }

    }

    private val _orderUIState = MutableStateFlow(OrderUIState())
    val orderUIState: StateFlow<OrderUIState> = _orderUIState

    fun showOrderWidget(token: String) = viewModelScope.launch {
        _orderUIState.value = _orderUIState.value.copy(token = token, widgetVisible = true)
        log("Opening order with token $token")
    }

    fun hideOrderWidget() = viewModelScope.launch {
        _orderUIState.value = _orderUIState.value.copy(widgetVisible = false)
        log("Closing order")
    }

    val truvOrderEventListener = object : TruvOrderEventsListener {
        override fun onOrderEvent(event: TruvOrderEvent) {
            log("order onOrderEvent: $event")
            if (event is TruvOrderEvent.Close) {
                hideOrderWidget()
            }
        }

        override fun onBridgeEvent(event: TruvEventPayload) {
            log("order onBridgeEvent: $event")
        }
    }

    fun changeProduct(productType: String) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(productType = productType)
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
    }

    fun changeProvider(provider: String?) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(provider = provider)
    }

    fun changeAccountState(accountState: AccountState) = viewModelScope.launch {
        _productUIState.value = productUIState.value.copy(accountState = accountState)
    }

    private val _consoleState = MutableStateFlow<String>("")
    val consoleState: StateFlow<String> = _consoleState

    fun log(value: String) = viewModelScope.launch {
        _consoleState.value = "${_consoleState.value}\n${value}"
    }

    fun init(preferences: SharedPreferences) {
        this.preferences = preferences

        val server = preferences.getString("server", "prod")
        val env = preferences.getString("env", "sandbox")
        val sandboxKey = preferences.getString("sandbox", "")
        val developmentKey = preferences.getString("dev", "")
        val productionKey = preferences.getString("prod", "")
        val clientId = preferences.getString("client_id", "")

        _settingsUIState.value = settingsUIState.value.copy(
            server = server!!,
            env = env!!,
            sandbox = sandboxKey!!,
            dev = developmentKey!!,
            prod = productionKey!!,
            clientId = clientId!!,
        )
    }

    private val _settingsUIState = MutableStateFlow(SettingsUIState())
    val settingsUIState: StateFlow<SettingsUIState> = _settingsUIState

    fun changeServer(server: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(server = server)
        val p = preferences.edit()
        p.putString("server", server)
        p.apply()
    }

    fun changeEnv(env: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(env = env)
        val p = preferences.edit()
        p.putString("env", env)
        p.apply()
    }

    fun changeClientId(clientId: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(clientId = clientId)
        val p = preferences.edit()
        p.putString("client_id", clientId)
        p.apply()

        changeUserId("")
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

    fun changeUserId(userId: String) = viewModelScope.launch {
        val p = preferences.edit()
        p.putString("user_id", userId)
        p.apply()
    }

    public fun getServerUrls(): ServerUrls {
        val server = settingsUIState.value.server

        return when (server) {
            "dev" -> ServerUrls("https://dev.truv.com", "https://cdn-dev.truv.com", "https://my-dev.truv.com")
            "stage" -> ServerUrls("https://stage.truv.com", "https://cdn-stage.truv.com", "https://my-stage.truv.com")
            "prod" -> ServerUrls("https://prod.truv.com", "https://cdn.truv.com", "https://my.truv.com")
            "local" -> ServerUrls("https://dev.truv.com", "http://10.0.2.2:3700", "http://10.0.2.2:3701")
            else -> throw IllegalArgumentException("Invalid server: $server")
        }
    }

    fun openBridge() {
        val settings = settingsUIState.value
        val secret = when (settings.env) {
            "dev" -> settings.dev
            "prod" -> settings.prod
            else -> settings.sandbox
        }

        if (secret.isEmpty()) {
            Log.d("ViewModel", "can't open bridge, secret is empty")
            showBridgeError()
            return
        }

        val apiClient = TruvApiClient(getServerUrls().apiUrl, settings.clientId, secret, ::log)
        val state = productUIState.value
        _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoading

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val userId = preferences.getString("user_id", "")
                if (userId.isNullOrEmpty()) {
                    apiClient.createUser({ newUserId ->
                        changeUserId(newUserId)
                        log("User created with id: $newUserId")
                        createBridgeToken(apiClient, newUserId, state)
                    }, { statusCode, body ->
                        log(requestErrorMessage("User creation", statusCode, body))
                        _bridgeTokenState.value = BridgeTokenState.BridgeTokenError
                        showBridgeError()
                    })
                } else {
                    createBridgeToken(apiClient, userId, state)
                }
            }
        }
    }

    private fun createBridgeToken(apiClient: TruvApiClient, userId: String, state: ProductUIState) {
        apiClient.createBridgeToken(userId, BridgeTokenRequest(
            state.productType,
            state.companyMapping,
            state.provider,
            if (state.productType == "deposit_switch" || state.productType == "pll") state.accountState else null
        ), { token ->
            _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoaded(token)
            log("Fetched bridge token: $token")
            showWidget()
        }, { statusCode, body ->
            log(requestErrorMessage("Bridge token", statusCode, body))
            _bridgeTokenState.value = BridgeTokenState.BridgeTokenError
            showBridgeError()
        })
    }

    private fun requestErrorMessage(action: String, statusCode: Int, body: String): String {
        val status = if (statusCode > 0) " (HTTP $statusCode)" else ""
        return "$action error$status: $body"
    }

    private fun showBridgeError() {
        _bridgeErrorEvents.tryEmit(Unit)
    }

}