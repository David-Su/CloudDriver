package cloud.bean

class CloudFile {
    var name: String? = null
    var isDir = false
    var children: List<CloudFile>? = null
}