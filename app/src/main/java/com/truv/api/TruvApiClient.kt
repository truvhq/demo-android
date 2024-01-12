package com.truv.api

import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.httpPost
import com.truv.BridgeTokenState
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.truv.BridgeTokenRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.UUID

data class BridgeTokenResponse(
    @SerializedName("bridge_token") var bridgeToken: String,
) {

    class Deserializer : ResponseDeserializable<BridgeTokenResponse> {
        override fun deserialize(content: String): BridgeTokenResponse =
            Gson().fromJson(content, BridgeTokenResponse::class.java)
    }
}

data class CreateUserResponse(
    @SerializedName("id") var id: String,
) {

    class Deserializer : ResponseDeserializable<CreateUserResponse> {
        override fun deserialize(content: String): CreateUserResponse =
            Gson().fromJson(content, CreateUserResponse::class.java)
    }
}

class TruvApiClient {
    private val baseUrl: String;
    private val clientId: String;
    private val clientSecret: String;

    constructor(baseUrl: String, clientId: String, clientSecret: String) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    };

    fun createUser(
        onSuccess: (id: String) -> Unit, onFailure: (message: String?) -> Unit
    ) = runBlocking {
        val gson = Gson()
        "$baseUrl/v1/users/".httpPost().header(
            mapOf(
                "Content-Type" to "application/json",
                "X-Access-Client-Id" to clientId,
                "X-Access-Secret" to clientSecret
            )
        ).body(gson.toJson(mapOf("external_user_id" to UUID.randomUUID().toString())))
            .awaitObjectResult(CreateUserResponse.Deserializer())
            .fold(
                { response -> onSuccess(response.id) },
                { error -> onFailure(error.response.responseMessage) },
            )
    }

    fun createBridgeToken(
        userId: String,
        request: BridgeTokenRequest,
        onSuccess: (token: String) -> Unit,
        onFailure: (message: String?) -> Unit
    ) = runBlocking {
        val gson = Gson()
        "$baseUrl/v1/users/$userId/tokens/".httpPost().header(
            mapOf(
                "Content-Type" to "application/json",
                "X-Access-Client-Id" to clientId,
                "X-Access-Secret" to clientSecret
            )
        ).body(gson.toJson(request)).awaitObjectResult(BridgeTokenResponse.Deserializer()).fold(
            { response -> onSuccess(response.bridgeToken) },
            { error -> onFailure(error.response.responseMessage) },
        )

    }
}