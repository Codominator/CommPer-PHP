package bobby_lib.nano.CommPerPHP;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Network;
import android.os.Bundle;

import bobby_lib.nano.networkphp.CommClient;
import bobby_lib.nano.networkphp.DefaultHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommClient client=new CommClient();
        client.setData("Bob","cool");
        client.setResponseHandler(new DefaultHandler());
        client.Send(this,"");
        //c
    }
}