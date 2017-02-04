package be.ulb.owl.utils.sql;

/**
 * Created by Detobel36
 */

public enum PlanTable implements SQLTable {

    ID("Id"),
    CAMPUS_ID("CampusId"),
    NAME("Name"),
    PPM("Ppm"),
    IMAGE_DIRECTORY("ImageDirectory"),
    X_ON_PARENT("XOnParent"),
    Y_ON_PARENT("YOnParent"),
    BG_COORD_X("BgCoordX"),
    BG_COORD_Y("BgCoordY"),
    RELATIVE_ANGLE("RelativeAngle");


    private static String _NAME = "Plan";
    private final String _value;

    PlanTable(String value) {
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
