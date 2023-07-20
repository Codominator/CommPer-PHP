package bobby_lib.nano.networkphp;

public interface ResponseHandler {

    void OnSuccess(String message, boolean isHashed);
    void OnFailed(int ErrorType, String Message);
    boolean OnHashVerified();
    boolean OnRetry(int ErrorType);
    boolean Error();
    void Finish();
}
