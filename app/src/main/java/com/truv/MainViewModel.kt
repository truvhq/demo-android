package com.truv

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import com.truv.models.TruvEventPayload
import com.truv.models.TruvSuccessPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.truv.api.TruvApiClient
import com.truv.webview.TruvEventsListener

data class AccountState(
    @SerializedName("account_number") var accountNumber: String = "160026001",
    @SerializedName("routing_number") val routingNumber: String = "123456789",
    @SerializedName("bank_name") val bankName: String = "TD Bank",
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

data class ServerUrls(
    val url: String,
    val cdnUrl: String
)

@ExperimentalCoroutinesApi
class MainViewModel : ViewModel() {
    private lateinit var preferences: SharedPreferences
    private lateinit var apiClient: TruvApiClient;

    private val _activeTabState = MutableStateFlow<Int>(0)
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
        }

        override fun onEvent(event: TruvEventPayload) {
            log("onEvent callback invoked: ${event.eventType} ($event)")
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

        val server = preferences.getString("server", "prod")
        val env = preferences.getString("env", "sandbox")
        val sandboxKey = preferences.getString("sandbox", "")
        val developmentKey = preferences.getString("dev", "")
        val productionKey = preferences.getString("prod", "")
        val clientId = preferences.getString("client_id", "")
        val userId = preferences.getString("user_id", "")

        _settingsUIState.value = settingsUIState.value.copy(
            server = server!!,
            env = env!!,
            sandbox = sandboxKey!!,
            dev = developmentKey!!,
            prod = productionKey!!,
            clientId = clientId!!,
        )

        fetchBridgeToken()
    }

    private val _settingsUIState = MutableStateFlow(SettingsUIState())
    val settingsUIState: StateFlow<SettingsUIState> = _settingsUIState

    fun changeServer(server: String) = viewModelScope.launch {
        _settingsUIState.value = settingsUIState.value.copy(server = server)
        val p = preferences.edit()
        p.putString("server", server)
        p.apply()
        fetchBridgeToken()
    }

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

        changeUserId("")

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

    fun changeUserId(userId: String) = viewModelScope.launch {
        val p = preferences.edit()
        p.putString("user_id", userId)
        p.apply()
    }

    private var bridgeTokenJob: Job? = null

    private fun fetchBridgeTokenThrottle() {
        bridgeTokenJob?.cancel()

        fetchBridgeToken()
    }
    
    public fun getServerUrls(): ServerUrls {
        val server = settingsUIState.value.server

        return when (server) {
            "dev" -> ServerUrls("https://dev.truv.com", "https://cdn-dev.truv.com")
            "stage" -> ServerUrls("https://stage.truv.com", "https://cdn-stage.truv.com")
            "prod" -> ServerUrls("https://prod.truv.com", "https://cdn.truv.com")
            "local" -> ServerUrls("https://dev.truv.com", "http://10.0.2.2:3700")
            else -> throw IllegalArgumentException("Invalid server: $server")
        }
    }

    private fun fetchBridgeToken() {
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

        val (serverUrl) = getServerUrls()

        if (secret == "") {
            Log.d("ViewModel", "stop fetching bridgetToken, secret is empty")
            return
        }

        apiClient = TruvApiClient(serverUrl, clientId, secret);

        _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoading
        val state = productUIState.value

        bridgeTokenJob = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                apiClient.createUser({ userId ->
                    changeUserId(userId)
                    createBridgeToken(userId, state)
                    log("User created with id: $userId")
                }, {
                    log("User creation error: $it")
                })
            }


        }
    }

    private fun createBridgeToken(userId: String, state: ProductUIState) {
        apiClient.createBridgeToken(userId, BridgeTokenRequest(
            productUIState.value.productType,
            state.companyMapping,
            state.provider,
            if (state.productType === "deposit_switch" || state.productType === "pll") state.accountState else null
        ), {
            _bridgeTokenState.value = BridgeTokenState.BridgeTokenLoaded(it)
            log("Fetched bridge token: $it")
        }, {
            log("Bridge token error: $it")
            _bridgeTokenState.value = BridgeTokenState.BridgeTokenError
        })
    }

}