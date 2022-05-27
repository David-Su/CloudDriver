package cloud.test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Test {

	public static void main(String[] args) throws IOException {
		new Baidu().main();
		new Zip().main();
	}

}
