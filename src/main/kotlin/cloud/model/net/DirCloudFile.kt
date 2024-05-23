package cloud.model.net

data class DirCloudFile(
    val name: String? = null,
    val children: List<DirCloudFileChild>? = null,
    val size: Long? = null,
) {
    data class DirCloudFileChild(
        val name: String? = null,
        val isDir: Boolean = false,
        val previewImg: String? = null,
        val size: Long? = null,
    )
}
