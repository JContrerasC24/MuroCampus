package com.tecsup.murocampus.data

import com.tecsup.murocampus.data.remote.UsuarioApi
import com.tecsup.murocampus.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class UsuarioRepositoryRemote(private val api: UsuarioApi) : UsuarioRepository {
    override suspend fun listar(): Result<List<Usuario>> = try {
        Result.success(api.listar())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (http: HttpException) {
        Result.failure(ErrorRepositorio.Http(http.code(), http))
    } catch (serializacion: SerializationException) {
        Result.failure(ErrorRepositorio.RespuestaInesperada(serializacion))
    } catch (red: IOException) {
        Result.failure(ErrorRepositorio.SinConexion(red))
    } catch (error: Exception) {
        Result.failure(ErrorRepositorio.Desconocido(error))
    }
}
