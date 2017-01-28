package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Created by Detobel36
 */

public enum BuildingTable implements SQLTable {

    ID("Id", 0),
    CAMPUS_ID("CampusId", 1),
    NAME("Name", 2),
    PIXEL_PER_METER("PixelPerMeter", 3),
    IMAGE_PATH("ImagePath", 4),
    X_ON_PARENT("xOnParent", 5),
    Y_ON_PARENT("yOnParent", 6),
    BG_COORD_x("bgCoordX", 7),
    BG_COORD_Y("bgCoordY", 8),
    RELATIVE_ANGLE("relativeAngle", 9);


    private static String _NAME = "Building";

    private String _value;
    private int _number;

    BuildingTable(String value, int number) {
        _value = value;
        _number = number;
    }

    @Override
    public String toString() {
        return _value;
    }

    /**
     * Get the index of the column
     *
     * @return the index of the column
     */
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
