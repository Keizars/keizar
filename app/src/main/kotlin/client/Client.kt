package org.keizar.android.client

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.utils.communication.CommunicationModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * A client for the Keizar server.
 */
class Client(
    baseUrl: String,
) : KoinComponent {
    private val sessionManager: SessionManager by inject()

    private val logger = logger("Client")
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = CommunicationModule
    }

    private val loggingInterceptor: (Interceptor.Chain) -> Response = { chain ->
        val request = chain.request()
        chain.proceed(request).also { response ->
            logger.info { "${request.method()} ${request.url()}: ${response.code()} ${response.message()}" }
        }
    }
    private val authorizationInterceptor: (Interceptor.Chain) -> Response = { chain ->
        val request = chain.request().newBuilder().apply {
            runBlocking { sessionManager.token.first() }?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()
        chain.proceed(request)
    }
    private val resultInterceptor: (Interceptor.Chain) -> Response = { chain ->
        val response = chain.proceed(chain.request())
//        val body = response.body()
//        if (response.isSuccessful && body != null) {
//            Response.Builder().apply {
//                code(response.code())
//                message(response.message())
//                body(ResponseBody.create(body.contentType(), Result.success(body)))
//            }.build()
//        } else {
//            Response.Builder().apply {
//                code(response.code())
//                message(response.message())
//                body(ResponseBody.create(response.head, Result.success(response.body())))
//            }.build()
//
//            Response.success(
//                Result.failure(
//                    HttpException(response)
//                )
//            )
//        }

        if (response.code() == 401) {
            sessionManager.launchInBackground { invalidateToken() }
        }
        response
    }

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(resultInterceptor)
        .addInterceptor(authorizationInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
//        .addConverterFactory(object : Converter.Factory() {
//            private val jsonConverter = json.asConverterFactory(contentType)
//            override fun responseBodyConverter(
//                type: Type,
//                annotations: Array<Annotation>,
//                retrofit: Retrofit
//            ): Converter<ResponseBody, *> = Converter<ResponseBody, Any> { value ->
//                jsonConverter.responseBodyConverter(
//                    type,
//                    annotations,
//                    retrofit,
//                )?.convert(value)
//            }
//
//        })
        .build()

    /**
     * Koin module that provides the services like [UserService].
     */
    val module: Module = module {
        single<UserService> { retrofit.create(UserService::class.java) }
        single<RoomService> { retrofit.create(RoomService::class.java) }
        single<SeedBankService> { retrofit.create(SeedBankService::class.java) }
    }
}

