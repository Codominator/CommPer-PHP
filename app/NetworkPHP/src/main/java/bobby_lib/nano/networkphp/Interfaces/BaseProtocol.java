package bobby_lib.nano.networkphp.Interfaces;

import com.android.volley.VolleyError;

public interface BaseProtocol {
    void OnReceive(String message);
    void OnError(VolleyError e);

}
