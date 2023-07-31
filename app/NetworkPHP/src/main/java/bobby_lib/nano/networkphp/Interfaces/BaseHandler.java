package bobby_lib.nano.networkphp.Interfaces;

import com.android.volley.VolleyError;

public interface BaseHandler {
    void OnFailed(int ErrorType, String Message);
    boolean OnHashVerified();
    boolean OnRetry(int ErrorType);
    boolean Error(Exception e);
    void Finish();
}
