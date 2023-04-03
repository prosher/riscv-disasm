package rv3;

import java.util.HashMap;
import java.util.Map;

public class Labels {
    private int counter;
    private final Map<Integer, String> values;

    public Labels() {
        values = new HashMap<>();
        counter = 0;
    }

    public void insert(final int addr, final String name) {
        values.put(addr, name);
    }

    public String getLabel(final int addr) {
        if (values.containsKey(addr)) {
            return values.get(addr);
        }
        values.put(addr, "L" + counter++);
        return values.get(addr);
    }

    public boolean containAddr(final int addr) {
        return values.containsKey(addr);
    }
}
