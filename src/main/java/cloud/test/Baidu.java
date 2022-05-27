package cloud.test;

import java.io.IOException;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Baidu {
	public void main() throws IOException {
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url(HttpUrl.parse("http://openapi.baidu.com/oauth/2.0/authorize").newBuilder()
						.addQueryParameter("response_type", "code")
						.addQueryParameter("client_id", "BhXz74ZdQLavqDqPfvG9IyyKniw6regE")
						.addQueryParameter("redirect_uri", "oob").addQueryParameter("scope", "basic,netdisk").build())
				.build();

		Response response = client.newCall(request).execute();
		System.out.print(response.body().string());

	}
}
