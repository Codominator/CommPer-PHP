package bobby_lib.nano.networkphp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.RawRes;
import androidx.work.Data;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import java.util.logging.Handler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CommClient implements Response.ErrorListener, Response.Listener<String> {

    public CommClient() {

    }

    private final Map<String, String> DATA = new HashMap<>();
    private ResponseHandler handler;

    public void setResponseHandler(ResponseHandler handler) {
        this.handler = handler;
    }

    ;


    private static @RawRes int CertID;

    public static void setCertificate(@RawRes int CertID) {
        CommClient.CertID = CertID;
    }

    public void clearData() {
        DATA.clear();
    }

    public void setData(String Tag, String value) {
        DATA.put(Tag, value);
    }

    public void Send(Context context, String url) {

        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory(context, CertID));
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    if (handler != null) {
                        handler.Error();
                        //OnErrorListener.Error();
                    }
                    e.printStackTrace();
                }
                httpsURLConnection.setChunkedStreamingMode(0);
                return httpsURLConnection;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context, hurlStack);


        // if (FILES.isEmpty()||!isFileSent) {

        StringRequest sr = new StringRequest(Request.Method.POST, url, this, this) {
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
       /* }else{
            VolleyMultiPartRequest sr=new VolleyMultiPartRequest(Request.Method.POST,url,this,this){


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
*/
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("VolleyError", error.getMessage() + "");
        if (handler != null) {
            handler.Error();
            handler.OnRetry(0);
            handler.Finish();
            //  if(retry!=null)
            //       retry.show();
            //   Completed.Finish();
        }


    }


    @Override
    public void onResponse(String response) {
        Log.e("TagM", response);
        if (handler != null) {
            JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
            if (jsonObject == null)
                Log.e("Error", "Empty message");
            if (jsonObject.has("ErrorCode")) {
                String Error = new Gson().fromJson(jsonObject.remove("ErrorCode"), String.class);
                String DATA = "";
                if (jsonObject.has("hashMatch")) {
                    boolean hashMatch = new Gson().fromJson(jsonObject.remove("hashMatch"), boolean.class);
                    if (hashMatch) {
                        handler.OnHashVerified();
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
                    handler.OnSuccess(DATA, false);
                } else {
                    String ErrorMessage = "";
                    if (jsonObject.has("ErrorMessage"))
                        ErrorMessage = new Gson().fromJson(jsonObject.remove("ErrorMessage"), String.class);

                    handler.OnFailed(Integer.parseInt(Error), ErrorMessage);
                }


            }
        }
        if (handler != null)
            handler.Finish();

    }

    public Data buildSyncedData(String url) {
        Data.Builder builder = new Data.Builder();
        builder.putString("url", url);
        for (String key : DATA.keySet()) {
            builder.putString(key, DATA.get(key));
        }
       /* if(isFileSent)
            for (String key: FILES.keySet()){
                builder.putString(key,FILES.get(key).getPath());
            }*/

        return builder.build();
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


    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();

                if (hostname.equals("79.129.38.103"))
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
                            if (certs != null && certs.length > 0) {
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
                            if (certs != null && certs.length > 0) {
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
}


