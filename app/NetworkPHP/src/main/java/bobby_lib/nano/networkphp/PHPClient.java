package bobby_lib.nano.networkphp;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RawRes;
import androidx.work.Data;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arthenica.mobileffmpeg.AbiDetect;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import bobby_lib.nano.networkphp.Errors.RetryErrorBox;
import bobby_lib.nano.networkphp.Errors.RetryErrorListener;
import bobby_lib.nano.networkphp.Errors.SimpleErrorListener;

public class PHPClient {


    private Response.Listener<String> OnResponse;
    private Response.ErrorListener onErrorResponse;
    private final Map<String, String> DATA = new HashMap<>();
    private final Map<String, VolleyMultiPartRequest.DataPart> FILES = new HashMap<>();
    private OnErrorReceived OnErrorListener;
    private OnMessageReceived mMessageListener;
    private OnCompleted Completed;
    private OnHash hash;
    private static @RawRes int CertID;
    public void invokeSuccess(String message){
        if(mMessageListener!=null){
            mMessageListener.OnSuccess(message);
        }
    }
    public PHPClient() {
        Log.e("Arch",AbiDetect.getAbi());

    }
    public static void setCertificate(@RawRes int CertID){
        PHPClient.CertID=CertID;
    }
    public void clearData() {
        DATA.clear();
    }

    public void setData(String Tag, String value) {
        DATA.put(Tag, value);
    }

    public void setData(String Tag, Bitmap value) {
        DATA.put(Tag, DecodeImage(value));
    }
    int fileCount=0;
    public void setData(String Tag, byte[] value,String path){
        fileCount++;
        FILES.put("filename"+fileCount,new VolleyMultiPartRequest.DataPart(Tag,value,path));
    }
    public void OnReceived(OnMessageReceived mMessageListener) {

        this.mMessageListener = mMessageListener;
    }

    public void OnComplete(OnCompleted Completed) {

        this.Completed = Completed;
    }
    public void OnHashVerified(OnHash hashFunction) {

        this.hash = hashFunction;
    }
    public void OnError(OnErrorReceived OnErrorListener) {

        this.OnErrorListener = OnErrorListener;
    }
    private SimpleErrorListener retryListener;
    public void setOnRetryBoxExit(SimpleErrorListener l){
        this.retryListener=l;
    }
    RetryErrorBox retry;
    public void Send(Context context, String Url) {
        retry=new RetryErrorBox(context, "Error","A problem occurred while connecting, please try again");
        retry.setOnAcknowledge(new RetryErrorListener() {
            @Override
            public void onAcknowledge() {
                if(retryListener!=null){
                    retryListener.onAcknowledge();
                }
                //context.getAfinishAffinity();
            }

            @Override
            public void onRetry() {
                PHPClient.this.Send(context,Url);
            }
        });
        onErrorResponse = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.getMessage() + "");
                if (OnErrorListener != null) {
                    OnErrorListener.Error();
                    if(retry!=null)
                        retry.show();
                }
                if (Completed != null)
                    Completed.Finish();


            }
        };

        OnResponse = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("TagM", response);
                if (mMessageListener != null) {
                    JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
                    if(jsonObject==null)
                        Log.e("Error","Empty message");
                    if (jsonObject.has("ErrorCode")) {
                        String Error = new Gson().fromJson(jsonObject.remove("ErrorCode"), String.class);
                        String DATA = "";
                        if (jsonObject.has("hashMatch")) {
                            boolean hashMatch = new Gson().fromJson(jsonObject.remove("hashMatch"), boolean.class);
                            if (hashMatch && hash != null) {
                                hash.HashVerified();
                                return;
                            }
                        }
                        if (jsonObject.has("DATA")) {
                            JsonElement DataElem = jsonObject.remove("DATA");
                            try {

                                DATA = new Gson().fromJson(DataElem, String.class);

                            } catch (com.google.gson.JsonSyntaxException e) {
                                DATA = DataElem.getAsJsonObject().toString();
                            }
                        }
                        if (Error.equals("0")) {
                            mMessageListener.OnSuccess(DATA);
                        } else {
                            String ErrorMessage = "";
                            if (jsonObject.has("ErrorMessage"))
                                ErrorMessage = new Gson().fromJson(jsonObject.remove("ErrorMessage"), String.class);

                            mMessageListener.OnFailed(Error, ErrorMessage);
                        }


                    }
                }
                if (Completed != null)
                    Completed.Finish();

            }
        };


        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory(context, CertID));
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    if(OnErrorListener!=null) {
                        //OnErrorListener.Error();
                    }
                    e.printStackTrace();
                }
                httpsURLConnection.setChunkedStreamingMode(0);
                return httpsURLConnection;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context, hurlStack);


        if (FILES.isEmpty()||!isFileSent) {

            StringRequest sr = new StringRequest(Request.Method.POST, Url, OnResponse, onErrorResponse) {
                @Override
                protected Map<String, String> getParams() {
                    return DATA;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

            };

        //DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        //sr.setRetryPolicy(retryPolicy);
        queue.add(sr);
    }else{
            VolleyMultiPartRequest sr=new VolleyMultiPartRequest(Request.Method.POST,Url,OnResponse,onErrorResponse){


                @Override
                protected Map<String, String> getParams() {
                    return DATA;
                }
                protected Map<String, DataPart> getByteData() {
                    return FILES;
                }
            };
            queue.add(sr);

        }

    }
    public interface ProgressPost{
        void progress(float point);
    }
    private ProgressPost progress;
    public void OnProgress(ProgressPost post){
        progress=post;
    }
    public boolean isFileSent(){
        return isFileSent;
    }
    public void SendSync(Context context, String Url) {
        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory(context,CertID));
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    OnErrorListener.Error();
                    e.printStackTrace();
                }
                httpsURLConnection.setChunkedStreamingMode(0);
                return httpsURLConnection;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context, hurlStack);
        RequestFuture<String> future = RequestFuture.newFuture();
        if (FILES.isEmpty()||!isFileSent) {
            StringRequest request = new StringRequest(Request.Method.POST, Url, future, future) {
                @Override
                protected Map<String, String> getParams() {
                    return DATA;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

            };

            queue.add(request);
        }else{
            VolleyMultiPartRequest sr=new VolleyMultiPartRequest(Request.Method.POST,Url,future,future){
                @Override
                protected Map<String, String> getParams() {
                    return DATA;
                }
                protected Map<String, DataPart> getByteData() {
                    return FILES;
                }
            };
            sr.OnProgress(new VolleyMultiPartRequest.ProgressPost() {
                @Override
                public void progress(float point) {
                    if(progress!=null){
                        progress.progress(point);
                    }
                }
            });
            queue.add(sr);
        }
        try {
            // Set an interval for the request to timeout. This will block the
            // worker thread and force it to wait for a response for 60 seconds
            // before timing out and raising an exception
            String response = future.get(30, TimeUnit.MINUTES);
            Log.e("TagM", response);
            if (mMessageListener != null) {
                JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
                if (jsonObject.has("ErrorCode")) {
                    String Error = new Gson().fromJson(jsonObject.remove("ErrorCode"), String.class);
                    String DATA = "";
                    if(jsonObject.has("hashMatch")){
                        boolean hashMatch = new Gson().fromJson(jsonObject.remove("hashMatch"), boolean.class);
                        if(hashMatch && hash!=null){
                            hash.HashVerified();
                            return;
                        }
                    }
                    if (jsonObject.has("DATA")) {
                        JsonElement DataElem = jsonObject.remove("DATA");
                        try {

                            DATA = new Gson().fromJson(DataElem, String.class);

                        } catch (com.google.gson.JsonSyntaxException e) {
                            DATA = DataElem.getAsJsonObject().toString();
                        }
                    }
                    if (Error.equals("0")) {
                        mMessageListener.OnSuccess(DATA);
                    } else {
                        String ErrorMessage = "";
                        if (jsonObject.has("ErrorMessage"))
                            ErrorMessage = new Gson().fromJson(jsonObject.remove("ErrorMessage"), String.class);

                        mMessageListener.OnFailed(Error, ErrorMessage);
                    }


                }
            }
            if (Completed != null)
                Completed.Finish();

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            mMessageListener.OnFailed("904", "Connection is not available");
            e.printStackTrace();
        }


    }
    public interface OnCompleted {
        void Finish();
    }

    public interface OnErrorReceived {
        void Error();
    }
    public interface OnHash {
        void HashVerified();
    }
    public interface OnMessageReceived {
        void OnSuccess(String message);

        void OnFailed(String ErrorType, String Message);

    }


    public static boolean isJsonValid(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }

    public static String DecodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);

    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                if(hostname.equals("79.129.38.103"))
                    return true;
                else
                    return hv.verify(hostname, session);
            }
        };
    }


    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };

    }
    private SSLSocketFactory getSSLSocketFactory(Context contextA, @RawRes int CertID)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = contextA.getResources().openRawResource(CertID);//R.raw.client_cert); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }
    public Data buildSyncedData(String url){
        Data.Builder builder = new Data.Builder();
        builder.putString("url",url);
        for(String key: DATA.keySet()){
            builder.putString(key,DATA.get(key));
        }
        if(isFileSent)
            for (String key: FILES.keySet()){
                builder.putString(key,FILES.get(key).getPath());
            }

        return builder.build();
    }
    boolean isFileSent=true;
    public String[] getFiles(){
        String[] array=new String[FILES.size()];
        int index=0;
        for (String key: FILES.keySet()){
            array[index]=FILES.get(key).getPath();
            index++;

        }
        return array;
    }
    public void setIsFileSent(boolean value){
        isFileSent=value;
    }

    public static byte[] getBytes(File file) throws IOException {
        InputStream stream=new FileInputStream(file);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = stream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}