package cloud.util

import cloud.config.Cons
import cloud.manager.logger

object CloudFileUtil {

    fun getWholePath(pathsStr: String): String {
        return getWholePath(pathsStr.split(",".toRegex()))
    }

    fun getWholePath(paths: List<String>): String {
        return paths
                .let {
                    if (it.getOrNull(0) == Cons.Path.USER_DIR_STUB) {
                        it.toMutableList().also { it.removeAt(0) }
                    } else it
                }.let {
                    logger.info("getWholePath:${it}")
                    FileUtil.getWholePath(it)
                }
    }

}