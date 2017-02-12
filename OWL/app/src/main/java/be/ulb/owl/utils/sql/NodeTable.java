package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum NodeTable implements SQLTable {
    ID("Id"),
    PLAN_ID("PlanId"),
    X("X"),
    Y("Y");

    private static String _NAME = "Node";

    private final String _value;

    NodeTable(String value) {
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
     * Get the name of the building table
     *
     * @return the name of the table
     */
    public static String getName() {
        return _NAME;
    }

}
