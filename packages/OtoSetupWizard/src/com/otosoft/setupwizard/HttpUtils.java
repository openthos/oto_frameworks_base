package com.otosoft.setupwizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtils {
    public static Object getHttpsURLConnection(String path) throws IOException {

        URL url = new URL(path);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");

        // set SSLSocketFoactory  here two 1.need security credential 2.need no security credential
        if (urlConnection instanceof HttpsURLConnection) {
            SSLContext sslContext = HttpUtils.getSSLContext();
            if (sslContext != null) {
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(
                                                         HttpUtils.hostnameVerifier);

                return urlConnection;
            }
        }
        else{
            return urlConnection;
        }

        return null;
//        // set proprity
//        urlConnection.setConnectTimeout(8 * 1000);
//        urlConnection.setReadTimeout(8 * 1000);
//
//        int responseCode = urlConnection.getResponseCode();
//        if (responseCode == 200) { // ask successful
//            InputStream inputStream = urlConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line = "";
//            StringBuffer result = new StringBuffer();
//            while ((line = reader.readLine()) != null) {
//              result.append(line);
//            }
//            System.out.println(result);
//            inputStream.close();
//        }
//        urlConnection.disconnect();
    }

    public static SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)  {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    private static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    //package the requet body
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
      StringBuffer stringBuffer = new StringBuffer();        //get the reponse code of service
      try {
           for (Map.Entry<String, String> entry : params.entrySet()) {
               stringBuffer.append(entry.getKey())
                      .append("=")
                      .append(URLEncoder.encode(entry.getValue(), encode))
                      .append("&");
           }
           stringBuffer.deleteCharAt(stringBuffer.length() - 1);
       } catch (Exception e) {
           e.printStackTrace();
       }
       return stringBuffer;
    }

   //deal with the return stream data
   public static String dealResponseResult(InputStream inputStream) {
      String resultData = null;
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int len = 0;
       try {
          while((len = inputStream.read(data)) != -1) {
             byteArrayOutputStream.write(data, 0, len);
          }
     } catch (IOException e) {
         e.printStackTrace();
        }
       resultData = new String(byteArrayOutputStream.toByteArray());
       return resultData;
   }
}
