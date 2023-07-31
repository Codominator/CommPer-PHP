package bobby_lib.nano.networkphp;

public class JsonField<T> {
    String value;
    public JsonField(String value){
        this.value=value;
    }
    public enum Type{
        ARRAY,
        VALUE,
        JSON
    }
}
