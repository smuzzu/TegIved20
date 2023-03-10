/**
 * Helper lib tor for NASA rest API testing
 *
 * @author  Sebastian M
 * @version 1.0
 * @since   2023-03-10
 */

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpLib {

    private static RequestConfig buildRequestConfig(){
        RequestConfig requestConfig = null;
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(40000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(40000)
                .setSocketTimeout(40000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        return requestConfig;
    }

    protected static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        RequestConfig requestConfig = buildRequestConfig();

        SSLConnectionSocketFactory sslsf=null;

        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        CloseableHttpClient httpclient =
                HttpClientBuilder.create()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setDefaultRequestConfig(requestConfig)
                        .setConnectionManagerShared(true)
                        .setMaxConnPerRoute(1000)
                        .setMaxConnTotal(1000)
                        .setSSLSocketFactory(sslsf)
                        .build();

        return httpclient;
    }


    private static String getStringFromURL(String uRL, CloseableHttpClient client, boolean DEBUG) {

        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(uRL);
        } catch (Exception e){
            String msg = "Error en getHTMLStringFromPage parseando url "+uRL;
            System.out.println(msg);
            return "nullORempty|";
        }


        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        while (retry && retries < 5) {
            retries++;


            try {
                response = client.execute(httpGet, context);
            } catch (IOException e) {
                response = null;
                String msg="Error en getHTMLStringFromPage intento #" + retries + " " + uRL;
                System.out.println(msg);
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    if (statusCode!=420 && statusCode!=403) { //429=too many requests
                        retry = false;
                    } else {
                        System.out.println("Http "+statusCode+" en getHTMLStringFromPage intento #" + retries + " " + uRL);
                    }
                }
            }

            if (retry) {
                try {
                    long pause=2000 * retries * retries;
                    if (statusCode==403){
                        pause=2L*pause;
                    }
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client = null;
                client = buildHttpClient();
            }
        }

        if (statusCode != 200) {
            if ((DEBUG) || (statusCode != 404 && statusCode != 403)) {  //403 y 404 se loguea solo con debug
                System.out.println("new status code " + statusCode + " " + uRL);
            }
            return ""+statusCode+"|";
        }

        InputStream inputStream = null;

        if (response!=null) {
            HttpEntity httpEntity = response.getEntity();
            try {
                inputStream = httpEntity.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (inputStream!=null) {
            String responseStr = getStringFromInputStream(inputStream);
            if (responseStr != null && responseStr.length() > 0) {
                return "ok|" + responseStr;
            }
        }

        return "nullORempty|";
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public static JSONObject getJsonObject(String uRL, CloseableHttpClient httpClient, boolean giveMeArray) {

        JSONObject jsonResponse=null;
        String jsonStringFromRequest = getStringFromURL(uRL, httpClient, false );
        if (!isOK(jsonStringFromRequest)){ //1 solo reintento
            try {
                Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = buildHttpClient();
            jsonStringFromRequest = getStringFromURL(uRL, httpClient, false);
        }
        if (isOK(jsonStringFromRequest)) {
            jsonStringFromRequest = jsonStringFromRequest.substring(3);
            if (jsonStringFromRequest.startsWith("[")){
                if (giveMeArray) {
                    jsonStringFromRequest = "{\"elArray\":"+jsonStringFromRequest+"}";
                }else {
                    jsonStringFromRequest = jsonStringFromRequest.substring(1, jsonStringFromRequest.length() - 1);
                }
            }
            try {
                jsonResponse = new JSONObject(jsonStringFromRequest);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return jsonResponse;
    }

    private static boolean isOK(String hmlString){
        if (hmlString!=null && hmlString.length()>1){
            String result=hmlString.substring(0,2);
            return result.equals("ok");
        }
        return false;
    }




}
