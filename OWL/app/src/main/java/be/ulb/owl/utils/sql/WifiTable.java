package be.ulb.owl.utils.sql;

/**
 * Created by Detobel36
 */

public enum WifiTable implements SQLTable {

    ID("Id"),
    BSS("BSS"),
    NODE_ID("NodeId"),
    AVG("Avg"),
    VARIANCE("Variance"),
    SCANNING_DATA("ScanningDate");


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

}
