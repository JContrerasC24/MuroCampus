package com.tecsup.murocampus.data

sealed class ErrorRepositorio(message: String, cause: Throwable) : Exception(message, cause) {
    class SinConexion(cause: Throwable) : ErrorRepositorio("No hay conexión. Comprueba internet y vuelve a intentar.", cause)
    class Http(val codigo: Int, cause: Throwable) : ErrorRepositorio("El servidor respondió con error HTTP $codigo.", cause)
    class RespuestaInesperada(cause: Throwable) : ErrorRepositorio("Respuesta inesperada del servidor.", cause)
    class Desconocido(cause: Throwable) : ErrorRepositorio("Ocurrió un error inesperado.", cause)
}
