package bobby_lib.nano.networkphp.Errors;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.networkphp.R;


public class RetryErrorBox extends ErrorBox implements RetryErrorListener{
    public RetryErrorBox(@NonNull Context context, String title, String message) {
        super(context, title, message);

    }

    @Override
    public void onAcknowledge() {
        if(!(simpleListener==null))
            simpleListener.onAcknowledge();
        this.dismiss();

    }

    @Override
    public void onRetry() {
        if(!(simpleListener==null))
            simpleListener.onRetry();
        this.dismiss();
    }

    public RetryErrorListener simpleListener;

    public void setOnAcknowledge(RetryErrorListener l){
        this.simpleListener=l;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn_Ack=findViewById(R.id.btn_ack);
        btn_Ack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAcknowledge();
            }
        });
        Button btn_Report=findViewById(R.id.Report);
        btn_Report.setVisibility(View.GONE);
        Button btn_Retry=findViewById(R.id.Retry);
        btn_Retry.setVisibility(View.VISIBLE);
        btn_Retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRetry();
            }
        });
    }
}
