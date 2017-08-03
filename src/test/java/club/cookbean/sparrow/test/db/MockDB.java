package club.cookbean.sparrow.test.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/2 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class MockDB {
    private static final String TAG = "[MockDB]";

    private Map<String, DataHolder> table;

    public static MockDB getDB() {
        return new MockDB();
    }

    private MockDB() {
        this.table = new HashMap<>();
    }

    public DataHolder get(String id) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(TAG+" get: id="+id);
        return table.get(id);
    }

    public List<DataHolder> list() {

        List<DataHolder> data = new ArrayList<>(table.size());
        for (DataHolder dataHolder : table.values()) {
            data.add(dataHolder);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(TAG + " list");
        return data;
    }

    public void add(DataHolder dataHolder) {
        table.put(dataHolder.field, dataHolder);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(TAG+" add: data="+dataHolder);
    }




    public static class DataHolder {
        private String field;
        private String value;

        public DataHolder() {}

        public DataHolder(String filed, String value) {
            this.field = filed;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "DataHolder{" +
                    "field='" + field + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
