package be.ulb.owl.utils.sql;

import java.util.ArrayList;

/**
 * Interface to have same function in all Table object
 *
 * Created by Detobel36
 */
public interface SQLTable {

    public abstract String toString();

    /**
     * Get the index of the column
     *
     * @return the index of the column
     */
    public abstract int getIndex();
}
