import java.io.Serializable;
import java.util.ArrayList;

public class SerializableArrayList implements Serializable {
    private ArrayList<ArrayList<String>> data;

    public SerializableArrayList(ArrayList<ArrayList<String>> data) {
        this.data = data;
    }

    public ArrayList<ArrayList<String>> getData() {
        return data;
    }
}