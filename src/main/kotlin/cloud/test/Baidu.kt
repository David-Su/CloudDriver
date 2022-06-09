package cloud.test

import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.IOException

class Baidu {
    @Throws(IOException::class)
    fun main() {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(HttpUrl.parse("http://openapi.baidu.com/oauth/2.0/authorize").newBuilder()
                        .addQueryParameter("response_type", "code")
                        .addQueryParameter("client_id", "BhXz74ZdQLavqDqPfvG9IyyKniw6regE")
                        .addQueryParameter("redirect_uri", "oob").addQueryParameter("scope", "basic,netdisk").build())
                .build()
        val response = client.newCall(request).execute()
        print(response.body().string())
    }
}