package cloud.util

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object JsonUtil {

    val gson = Gson()

    fun <T> fromJsonStream(input: InputStream, classOfT: Class<T>): T {
        return fromJsonReader(BufferedReader(InputStreamReader(input)), classOfT)
    }

    fun <T> fromJsonReader(reader: BufferedReader, classOfT: Class<T>): T {

        val sb = StringBuilder()
        var line: String? = ""
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        return gson.fromJson(sb.toString(), classOfT)
    }

    fun toJson(src: Any?): String {
        return gson.toJson(src)
    }

    fun <T> fromJson(json: String?, classOfT: Class<T>?): T {
        return gson.fromJson(json, classOfT)
    }
}