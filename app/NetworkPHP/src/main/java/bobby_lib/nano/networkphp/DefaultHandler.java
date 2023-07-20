package bobby_lib.nano.networkphp;

public class DefaultHandler implements ResponseHandler{
    @Override
    public void OnSuccess(String message, boolean isHashed) {

    }

    @Override
    public void OnFailed(int ErrorType, String Message) {

    }

    @Override
    public boolean OnHashVerified() {
        return false;
    }

    @Override
    public boolean OnRetry(int ErrorType) {
        return false;
    }

    @Override
    public boolean Error() {
        return false;
    }

    @Override
    public void Finish() {

    }
}
