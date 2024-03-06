package org.keizar.client

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import org.keizar.client.annotations.InternalClientApi
import org.keizar.client.services.BaseRoomService
import org.keizar.client.services.GameDataService
import org.keizar.client.services.RoomService
import org.keizar.client.services.RoomServiceImpl
import org.keizar.client.services.SeedBankService
import org.keizar.client.services.StreamingService
import org.keizar.client.services.StreamingServiceImpl
import org.keizar.client.services.UserService
import org.keizar.utils.communication.CommunicationModule
import org.keizar.utils.coroutines.childSupervisorScope
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A facade that provides access to the services provided by the server.
 */
interface Client {
    /**
     * Koin module that provides the services like [UserService].
     */
    val servicesModule: Module
}

/**
 * Creates a new [Client] instance.
 */
fun Client(
    config: ClientConfig,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
): Client = ClientImpl(config, parentCoroutineContext)

class ClientConfig(
    val baseUrl: String,
)

private class ClientImpl(
    private val config: ClientConfig,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : Client {
    /**
     * Koin module that provides the services like [UserService].
     */
    override val servicesModule: Module = module {
        single<RetrofitProvider> { RetrofitProvider(config.baseUrl, parentCoroutineContext) }
        single<UserService> { get<RetrofitProvider>().retrofit.create(UserService::class.java) }
        single<RoomService> {
            @OptIn(InternalClientApi::class)
            RoomServiceImpl(
                baseUrl = config.baseUrl,
                generated = get<RetrofitProvider>().retrofit.create(BaseRoomService::class.java)
            )
        }
        single<SeedBankService> { get<RetrofitProvider>().retrofit.create(SeedBankService::class.java) }
        single<StreamingService> { StreamingServiceImpl(config.baseUrl) }
        single<GameDataService> { get<RetrofitProvider>().retrofit.create(GameDataService::class.java) }
    }
}

internal class RetrofitProvider(
    baseUrl: String,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val koin: Koin? = null,
) : KoinComponent {
    override fun getKoin(): Koin = koin ?: super.getKoin()
    private val scope = parentCoroutineContext.childSupervisorScope()
    private val accessTokenProvider: AccessTokenProvider by inject()

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
            runBlocking { accessTokenProvider.getAccessToken() }?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()
        chain.proceed(request)
    }

    private val resultInterceptor: (Interceptor.Chain) -> Response = { chain ->
        val response = chain.proceed(chain.request())

        if (response.code() == 401) {
            scope.launch {
                logger.info { "Invalidating token because received 401 response from ${chain.request().url()}" }
                accessTokenProvider.invalidateToken()
            }
        }
        response
    }

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(resultInterceptor)
        .addInterceptor(authorizationInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        .build()
}