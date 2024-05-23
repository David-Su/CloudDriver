package cloud.model.net

data class Response<T>(val code: String, val message: String, val result: T? = null)