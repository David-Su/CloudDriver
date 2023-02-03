package cloud.model.net

data class CloudFile(
        val name: String? = null,
        val isDir: Boolean = false,
        val children: List<CloudFile>? = null,
        val size: Long? = null,
        val previewImg: String? = null
)