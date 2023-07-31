package bobby_lib.nano.CommPerPHP;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Network;
import android.os.Bundle;
import android.util.Log;

import bobby_lib.nano.networkphp.CommClient;
import bobby_lib.nano.networkphp.DefaultHandler;
import bobby_lib.nano.networkphp.JsonData;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JsonData data=new JsonData("{" +
                "'id': '0001'," +
                "'type': 'donut'," +
                "'name': 'Cake'," +
                "'ppu': 0.55," +
                "'batters':" +
                "{" +
                "'batter':" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{ 'id': '1001', 'type': 'Regular' },\n" +
                "\t\t\t\t\t{ 'id': '1002', 'type': 'Chocolate' },\n" +
                "\t\t\t\t\t{ 'id': '1003', 'type': 'Blueberry' },\n" +
                "\t\t\t\t\t{ 'id': '1004', 'type': \"Devil's Food\" }\n" +
                "\t\t\t\t]\n" +
                "\t\t},\n" +
                "\t'topping':\n" +
                "\t\t[\n" +
                "\t\t\t{ 'id': '5001', 'type': 'None' },\n" +
                "\t\t\t{ 'id': '5002', 'type': 'Glazed' },\n" +
                "\t\t\t{ 'id': '5005', 'type': 'Sugar' },\n" +
                "\t\t\t{ 'id': '5007', 'type': 'Powdered Sugar' },\n" +
                "\t\t\t{ 'id': '5006', 'type': 'Chocolate with Sprinkles' },\n" +
                "\t\t\t{ 'id': '5003', 'type': 'Chocolate' },\n" +
                "\t\t\t{ 'id': '5004', 'type': 'Maple' }" +
                "]}");
        Log.e("id",data.get("batters").get("batter").get(3).get("type").Retrieve());
       // CommClient client=new CommClient();
        //client.setData("Bob","cool");
        //client.setResponseHandler(new DefaultHandler());
       // client.setExclusiveHost("bobbo.duckdns.org");
       // client.Send(this,"https://bobbo.duckdns.org//phptest.php");
        //c

    }
}