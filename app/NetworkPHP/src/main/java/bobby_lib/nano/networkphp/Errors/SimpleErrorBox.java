package bobby_lib.nano.networkphp.Errors;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.networkphp.R;


public class SimpleErrorBox extends ErrorBox implements SimpleErrorListener{
    public SimpleErrorBox(@NonNull Context context, String title, String message) {
        super(context, title, message);

    }

    @Override
    public void onAcknowledge() {
        if(!(simpleListener==null))
            simpleListener.onAcknowledge();
        this.dismiss();

    }
    public SimpleErrorListener simpleListener;

    public void setOnAcknowledge(SimpleErrorListener l){
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
    }
}
