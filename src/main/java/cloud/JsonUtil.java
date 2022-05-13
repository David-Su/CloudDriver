package cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

public final class JsonUtil {

	private static Gson gson = new Gson();

	public static <T> T fromJsonStream(InputStream is, Class<T> classOfT) {
		return fromJsonReader(new BufferedReader(new InputStreamReader(is)), classOfT);
	}

	public static <T> T fromJsonReader(BufferedReader reader, Class<T> classOfT) {
		try {
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return gson.fromJson(sb.toString(), classOfT);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String toJson(Object src) {
		return gson.toJson(src);
	}
	
	
	public  static <T> T fromJson(String json,Class<T> classOfT) {
		return gson.fromJson(json,classOfT);
	}
}
