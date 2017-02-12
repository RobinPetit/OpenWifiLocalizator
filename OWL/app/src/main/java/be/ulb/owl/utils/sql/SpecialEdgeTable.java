package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by robin on 04/02/17.
 */

public enum SpecialEdgeTable implements SQLTable{
    ID("Id"),
    NODE_1_ID("Node1Id"),
    NODE_2_ID("Node2Id"),
    WEIGHT("Weight");

    private static String _NAME = "SpecialEdges ";

    private final String _value;

    SpecialEdgeTable(String value) {
        _value = value;
    }

    @Override
    public String getCol() {
        return _value;
    }

    @Override
    public String getFullCol() {
        return _NAME + "." + _value;
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
