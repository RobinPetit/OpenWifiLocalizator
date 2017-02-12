package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum EdgeTable implements SQLTable {

    ID("Id"),
    NODE_1_ID("Node1Id"),
    NODE_2_ID("Node2Id");


    private static String _NAME = "Edge";

    private final String _value;

    EdgeTable(String value) {
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

}
