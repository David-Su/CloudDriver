package cloud.model.net

data class RenameFile(
        val paths: List<String> = listOf(),
        val newPaths: List<String> = listOf(),
)