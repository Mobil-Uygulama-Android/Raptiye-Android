package tr.edu.bilimankara20307006.taskflow.data.repository

/**
 * Network Result
 * API çağrılarının sonuçlarını temsil eder
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

/**
 * API yanıtını NetworkResult'a dönüştürür
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        
        if (response.isSuccessful) {
            response.body()?.let {
                NetworkResult.Success(it)
            } ?: NetworkResult.Error("Boş yanıt alındı")
        } else {
            NetworkResult.Error(
                message = response.message() ?: "Bilinmeyen hata",
                code = response.code()
            )
        }
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Bağlantı hatası"
        )
    }
}
