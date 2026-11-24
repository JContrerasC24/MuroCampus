package com.tecsup.murocampus.data

import com.tecsup.murocampus.data.remote.PostApi
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.NuevoPost
import com.tecsup.murocampus.model.Post
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PostRepositoryRemote(private val api: PostApi) : PostRepository {
    override suspend fun listar() = ejecutar { api.listar() }
    override suspend fun detalle(id: Int) = ejecutar { api.detalle(id) }
    override suspend fun comentarios(id: Int) = ejecutar { api.comentarios(id) }
    override suspend fun porUsuario(userId: Int) = ejecutar { api.porUsuario(userId) }
    override suspend fun crear(nuevo: NuevoPost) = ejecutar { api.crear(nuevo) }

    private suspend fun <T> ejecutar(request: suspend () -> T): Result<T> = try {
        Result.success(request())
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
