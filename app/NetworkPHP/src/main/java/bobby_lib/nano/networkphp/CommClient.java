package bobby_lib.nano.networkphp;

import android.content.Context;
import android.util.Log;

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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import bobby_lib.nano.networkphp.Interfaces.BaseProtocol;
import bobby_lib.nano.networkphp.Interfaces.ResponseHandler;

public class CommClient extends BaseClient implements Response.ErrorListener, Response.Listener<String> {

    public CommClient() {
        setHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    private ResponseHandler handler;
    BaseProtocol baseProtocol;
    public void setResponseHandler(ResponseHandler handler) {
        this.handler = handler;
        this.baseProtocol = null;
    }
    public void setResponseHandler(BaseProtocol baseProtocol) {
        this.baseProtocol = baseProtocol;
        this.handler = null;
    }
    String url="";
    @Override
    public void Send(Context context, String url) {
        this.url=url;
        HurlStack hurlStack = buildConnection(context,handler);

        RequestQueue queue = Volley.newRequestQueue(context, hurlStack);


        // if (FILES.isEmpty()||!isFileSent) {

        StringRequest sr = new StringRequest(Request.Method.POST, url, this, this) {
            @Override
            protected Map<String, String> getParams() {
                return DATA;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return HEADERS;
            }

        };

        //DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        //sr.setRetryPolicy(retryPolicy);
        queue.add(sr);

    }

    @Override
    public boolean SyncSend(Context context, String url) {
        this.url=url;
        HurlStack hurlStack = buildConnection(context,handler);
        RequestQueue queue = Volley.newRequestQueue(context, hurlStack);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            protected Map<String, String> getParams() {
                return DATA;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return HEADERS;
            }

        };

        queue.add(request);
        try {
            // Set an interval for the request to timeout. This will block the
            // worker thread and force it to wait for a response for 60 seconds
            // before timing out and raising an exception
            String response = future.get(30, TimeUnit.MINUTES);
            Log.e("TagM", response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            //mMessageListener.OnFailed("904", "Connection is not available");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(baseProtocol!=null){
            baseProtocol.OnError(error);
            return;
        }
        Log.e("VolleyError", error.getMessage() + "");
        if (handler != null) {
            handler.Error(error);
            handler.OnRetry(0);
            handler.Finish();
            //  if(retry!=null)
            //       retry.show();
            //   Completed.Finish();
        }


    }


    @Override
    public void onResponse(String response) {
        Log.d("Reply from url "+url+":", response);
        if(baseProtocol!=null){
            baseProtocol.OnReceive(response);
            return;
        }
        if(!isJsonValid(response)){
            handler.OnSuccess(response,false);
            return;
        }

        if (handler != null) {
            JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
            if (jsonObject == null)
                Log.e("Error", "Empty message");

            Set<String> keys=jsonObject.keySet();
            for(String key:keys) {
                Log.e("key", key);
            }
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


    String tusServer=null;
    public void setTusUrlServer(String url){
        tusServer=url;
    }
    public void SendTusFile(){

    }
    public static boolean isJsonValid(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }
}


