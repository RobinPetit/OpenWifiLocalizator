package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum NodeTable implements SQLTable {
    ID("Id", 0),
    BUILDING_ID("BuildingId", 1),
    X("X", 2),
    Y("Y", 3),
    BUILDING_2_ID("Building2Id", 4);

    private static String _NAME = "Node";

    private String _value;
    private int _number;

    NodeTable(String value, int number) {
        _value = value;
        _number = number;
    }

    @Override
    public String toString() {
        return _value;
    }

    @Override
    public int getIndex() {
        return _number;
    }

    //////////////////////////////////////////// STATIC ////////////////////////////////////////////

    /**
     * Get the name of the building table
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
