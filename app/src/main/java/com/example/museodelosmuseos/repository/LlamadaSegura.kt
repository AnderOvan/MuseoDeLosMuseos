package com.example.museodelosmuseos.repository

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

// Ejecuta una llamada a la red y devuelve Result:
//  - Result.success(dato) si el servidor respondió bien
//  - Result.failure(error) si no hay conexión, el servidor falló o el JSON no calza
// Así el ViewModel decide qué mostrar sin que la app se cierre.
suspend fun <T> llamadaSegura(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e // la pantalla se cerró: no es un error, se deja pasar
    } catch (e: Exception) {
        Result.failure(e)
    }

// Los DELETE no devuelven cuerpo: si el código no es 2xx se lanza el error HTTP
fun Response<Unit>.exigirExito() {
    if (!isSuccessful) throw HttpException(this)
}