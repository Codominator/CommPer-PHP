package bobby_lib.nano.networkphp.Errors;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.networkphp.R;


public class ErrorBox extends Dialog {
    private String title;
    private String message;
    private  ErrorHandle handle;
    private ErrorType type;


    public ErrorBox(@NonNull Context context) {
        super(context);
    }
    public ErrorBox(@NonNull Context context,String title,String message) {
        super(context);
        this.title=title;
        this.message=message;
    }

    public ErrorBox(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ErrorBox(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.error_message);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        TextView TextTitle=(TextView)findViewById(R.id.Title);
        TextTitle.setText(this.title);
        TextView TextMessage=(TextView)findViewById(R.id.ErrorMessage);
        TextMessage.setText(this.message);
        setCancelable(false);
        //Button btnOk=findViewById(R.id.btn_ack);
       // btnYes.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
       //         dismiss();
      //      }
      //  });
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public interface ErrorHandle{
      void onAcknowledge();
      void onRetry();
      void onReport();
    }
    public enum ErrorType {
        EXPECTED,
        RETRY,
        FATAL
    }

}
