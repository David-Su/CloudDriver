package cloud.model.net

data class UploadTask(
        val path: String,
        var progress: Double,
        var speed: Long //字节
)
