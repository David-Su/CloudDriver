import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Test {

	public static void main(String[] args) {
		System.out.print("adfasdfasdf");
		
		
        try {
        	HttpURLConnection mConnection = (HttpURLConnection) new URL("http://release.anjiu.cn/account/initial").openConnection();
			mConnection.setRequestMethod("POST");
	        mConnection.setReadTimeout(20000);
	        mConnection.setConnectTimeout(20000);
	        mConnection.setRequestProperty("Accept-Encoding", "identity");
	        mConnection.setDoInput(true);
	        mConnection.setDoOutput(true);
	       mConnection.setUseCaches(false);
	        OutputStream outputStream = mConnection.getOutputStream();
	        
//	        String p = "latitude=&platformId=104&imsi=null&deviceId=4d2fb336-a834-4b76-aa2c-4874c81f6947&resolution=1080+x+2265&systemVersion=29&platform=Android+10&manufacturer=HUAWEI&clientType=1&appId=220214107347&location=&model=ELE-AL00&networkType=wifi&longitude=&";
	        
	        String p = "latitude=&platformId=156&imsi=null&deviceId=9dd1e0e8-3b2a-46fe-a832-fa7d1f608bbd&resolution=1080+x+2265&systemVersion=29&platform=Android+10&manufacturer=HUAWEI&clientType=1&appId=220214107347&location=&model=ELE-AL00&networkType=wifi&longitude=&";
	        
	        outputStream.write(p.getBytes());
	        outputStream.flush();
	        outputStream.close();
	        
	        mConnection.connect();
	        
            String contentEncoding = mConnection.getContentEncoding();
            String readInputStreamToString;
            if (contentEncoding == null || !contentEncoding.contains("gzip")) {
                readInputStreamToString = readInputStreamToString(mConnection.getInputStream());
            } else {
                readInputStreamToString = readInputStreamToString(new GZIPInputStream(mConnection.getInputStream()));
            }

    		System.out.print("readInputStreamToString:"+readInputStreamToString);

		} catch (Exception e) {
			
			e.printStackTrace();
		}


	}
	
    private static String readInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                byteArrayOutputStream.write(bArr, 0, read);
            } else {
                String str = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                return str;
            }
        }
    }

}
