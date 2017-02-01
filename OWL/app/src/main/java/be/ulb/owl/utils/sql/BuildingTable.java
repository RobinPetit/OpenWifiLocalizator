package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum BuildingTable implements SQLTable {

    ID("Id"),
    CAMPUS_ID("CampusId"),
    NAME("Name"),
    PPM("Ppm"),
    IMAGE_PATH("ImagePath"),
    X_ON_PARENT("XOnParent"),
    Y_ON_PARENT("YOnParent"),
    BG_COORD_X("BgCoordX"),
    BG_COORD_Y("BgCoordY"),
    RELATIVE_ANGLE("RelativeAngle");


    private static String _NAME = "Building";

    private final String _value;

    BuildingTable(String value) {
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
