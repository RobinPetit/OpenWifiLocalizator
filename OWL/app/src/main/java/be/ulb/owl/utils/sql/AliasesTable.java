package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum AliasesTable implements SQLTable {

    ID("Id"),
    NAME("Name");


    private static String _NAME = "Aliases";

    private final String _value;

    AliasesTable(String value) {
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
