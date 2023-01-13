package cloud.config

import cloud.util.FileUtil
import com.auth0.jwt.algorithms.Algorithm

class Cons {
    object Env {
        const val IS_RELEASE = false
    }

    object Token {
        const val KEY_USERNAME = "key_username"
        private const val SECRET = "jfaksdjfiaosbjxcvbnfng"
        val ALGORITHM: Algorithm = Algorithm.HMAC256(SECRET)
    }

    object Path {
        private val ROOT_DIR = FileUtil.getWholePath(System.getProperty("user.home"), "CloudDriver")
        private val TEMP_DIR = FileUtil.getWholePath(ROOT_DIR, "temp")
        val DATA_DIR = FileUtil.getWholePath(ROOT_DIR, "data")
        val TEMP_UPLOAD_DIR = FileUtil.getWholePath(TEMP_DIR, "upload")
        val TEMP_PREVIEW_DIR = FileUtil.getWholePath(TEMP_DIR, "preview")
        const val USER_DIR_STUB = "." //用户目录占位符
    }
}