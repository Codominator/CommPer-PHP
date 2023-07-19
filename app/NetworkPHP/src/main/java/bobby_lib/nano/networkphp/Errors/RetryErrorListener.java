package bobby_lib.nano.networkphp.Errors;

public interface RetryErrorListener {

     void onAcknowledge();

     void onRetry();
}
