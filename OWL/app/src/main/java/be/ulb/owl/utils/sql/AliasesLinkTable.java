package be.ulb.owl.utils.sql;

/**
 * Created by Detobel36
 */
public enum AliasesLinkTable implements SQLTable {

    NODE_ID("NodeId"),
    ALIAS_ID("AliasId");


    private static String _NAME = "AliasesLink";

    private final String _value;

    AliasesLinkTable(String value) {
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
