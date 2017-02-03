package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum WifiTable implements SQLTable {

    ID("Id"),
    BSS("BSS"),
    NODE_ID("NodeId"),
    MIN("Min"),
    MAX("Max"),
    AVG("Avg"),
    VARIANCE("Variance");


    private static String _NAME = "Wifi";

    private final String _value;

    WifiTable(String value) {
        _value = value;
    }

    /**
     * Get the name of the column
     *
     * @return the column name
     */
    @Override
    public String getCol() {
        return _value;
    }

    /**
     * Get the full name of the column
     *
     * @return the column name with table name before
     */
    @Override
    public String getFullCol() {
        return _NAME+"."+_value;
    }


    //////////////////////////////////////////// STATIC ////////////////////////////////////////////

    /**
     * Get the name of the current table
     *
     * @return the name of the table
     */
    public static String getName() {
        return _NAME;
    }

    /**
     * Get all column
     *
     * @return all column
     */
    public static String[] strValues() {
        ArrayList<String> res = new ArrayList<String>();
        for(SQLTable val : values()) {
            res.add(val.toString());
        }
        return (String[]) res.toArray();
    }

}
