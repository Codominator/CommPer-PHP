package bobby_lib.nano.networkphp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import bobby_lib.nano.mylibrary.R;

public class JsonData {
    public int length=0;
    private final String data;
    public JsonData(String data){
        this.data=data;
        JsonObject jsonObject;
        Log.e("is JSON",isJsonValid(data)+"");

        if(isJsonValid(data)) {
                jsonObject = new Gson().fromJson(data, JsonObject.class);
        }else if(isJsonArrayValid(data)){
            JsonArray jsonArray=new Gson().fromJson(data, JsonArray.class);
            length=jsonArray.size();
        }
        else
            Log.e("wrong","wrong");
        Log.e("length",length+"");
    }
    public JsonData get(String tag){
        JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
        String value;
        try {
            value = new Gson().fromJson(jsonObject.get(tag), String.class);
        }catch (JsonSyntaxException e){
            value=jsonObject.get(tag).toString();
        }
        Log.e("value",value);
        return new JsonData(value);

    }
    public JsonData get(int tag){
        JsonArray jsonArray = new Gson().fromJson(data, JsonArray.class);
        String value;
        try {
            value = new Gson().fromJson(jsonArray.get(tag), String.class);
        }catch (JsonSyntaxException e){
            value=jsonArray.get(tag).toString();
        }

        return new JsonData(value);

    }
    public String Retrieve(){
        return data;
    }

    public static boolean isJsonValid(String json) {
        json=json.trim();
        return json.startsWith("{") && json.endsWith("}");


    }
    public static boolean isJsonArrayValid(String json) {
        json=json.trim();
        return json.startsWith("[") && json.endsWith("]");
    }
}
