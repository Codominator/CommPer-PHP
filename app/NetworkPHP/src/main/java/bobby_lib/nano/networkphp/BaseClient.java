package bobby_lib.nano.networkphp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.RawRes;

import com.android.volley.toolbox.HurlStack;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import bobby_lib.nano.networkphp.Interfaces.ResponseHandler;

public abstract class BaseClient {
    protected final Map<String, String> DATA = new HashMap<>();
    protected final Map<String, String> HEADERS = new HashMap<>();
    public Map<String, String> getHeaders(){
        return HEADERS;
    }
    public Map<String, String> getData(){
        return DATA;
    }
    protected static @RawRes int CertID;

    public static void setCertificate(@RawRes int CertID) {
        BaseClient.CertID = CertID;
    }
    public void clearHeaders() {
        HEADERS.clear();
    }
    public void setHeader(String Tag, String value) {
        HEADERS.put(Tag, value);
    }
    public void clearData() {
        DATA.clear();
    }
    public void setData(String Tag, String value) {
        DATA.put(Tag, value);
    }
    protected String hostName=null;
    public void setExclusiveHost(String host){
        hostName=host;

    }
    public abstract void Send(Context context,String url);

    public abstract boolean SyncSend(Context context,String url);

    protected HurlStack buildConnection(Context context, ResponseHandler handler) {

        return new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    if(BaseClient.CertID!=0)
                        httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory(context, CertID));
                    if(hostName!=null)
                        httpsURLConnection.setHostnameVerifier(getHostnameVerifier(hostName));
                } catch (Exception e) {
                    if (handler != null) {
                        handler.Error(e);
                        //OnErrorListener.Error();
                    }
                    e.printStackTrace();
                }
                httpsURLConnection.setChunkedStreamingMode(0);
                return httpsURLConnection;
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


    private HostnameVerifier getHostnameVerifier(String hostName) {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();

                if (hostname.equals(hostName))
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
