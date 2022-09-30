package cloud.bean

data class UploadTask(
        val path: String,
        var progress: Double,
        var speed: Long //字节
)
