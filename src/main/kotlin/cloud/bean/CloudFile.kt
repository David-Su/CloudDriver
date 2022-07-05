package cloud.bean

data class CloudFile(
        val name: String? = null,
        val isDir: Boolean = false,
        val children: List<CloudFile>? = null,
        val size: Long?=null
)