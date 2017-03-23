package be.ulb.owl.utils.sql;

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

}
