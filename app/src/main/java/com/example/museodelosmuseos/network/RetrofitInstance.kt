package com.example.museodelosmuseos.network


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton (object): una sola instancia de Retrofit por servidor en toda la app.
// Cada servidor tiene su propia URL base, por eso la API externa y los
// microservicios usan instancias separadas y no se interfieren.
object RetrofitInstance {

    // ---------- Direcciones ----------
    // API externa (internet, HTTPS)
    private const val URL_API_ARTE = "https://api.artic.edu/api/v1/"

    // Backend propio (Spring Boot, HTTP local, puerto 8080).
    // Emulador: el PC se ve como "10.0.2.2" (se detecta solo).
    // Celular físico: "localhost" funciona gracias a "adb reverse tcp:8080 tcp:8080".
    // Backend propio (Spring Boot, HTTP local, puerto 8080).
    // Emulador y celular físico: "localhost" funciona gracias a "adb reverse tcp:8080 tcp:8080".
    private const val URL_BACKEND = "http://localhost:8080/"

    // ---------- Cliente HTTP compartido ----------
    // Muestra en Logcat cada petición y su respuesta (útil para la defensa)
    private val registro = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(registro)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // La API externa pide (como cortesía) identificar la app que la consume
    private val clienteArte: OkHttpClient = cliente.newBuilder()
        .addInterceptor { cadena ->
            val peticion = cadena.request().newBuilder()
                .header("AIC-User-Agent", "MuseoDeLosMuseos (proyecto academico Duoc UC)")
                .build()
            cadena.proceed(peticion)
        }
        .build()

    // Arma un Retrofit para una URL base: JSON <-> data class con Gson
    private fun crearRetrofit(urlBase: String, clienteHttp: OkHttpClient = cliente): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Un solo Retrofit para el backend: los 3 servicios comparten la URL base
    private val backend: Retrofit by lazy { crearRetrofit(URL_BACKEND) }

    // ---------- Servicios ("by lazy": se crean la primera vez que se usan) ----------
    val arteApi: ArteApiService by lazy {
        crearRetrofit(URL_API_ARTE, clienteArte).create(ArteApiService::class.java)
    }

    val museoApi: MuseoApiService by lazy {
        backend.create(MuseoApiService::class.java)
    }

    val productoApi: ProductoApiService by lazy {
        backend.create(ProductoApiService::class.java)
    }

    val pedidoApi: PedidoApiService by lazy {
        backend.create(PedidoApiService::class.java)
    }

    val usuarioApi: UsuarioApiService by lazy {
        backend.create(UsuarioApiService::class.java)
    }
}