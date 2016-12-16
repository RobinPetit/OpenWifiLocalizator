package be.ulb.owl.gui.listener;

import android.support.v7.widget.SearchView;
import android.util.Log;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.NoPathException;

/**
 * All event
 *
 * @author Detobel36
 */
public class QueryTextListener implements SearchView.OnQueryTextListener {
    MainActivity main = MainActivity.getInstance();

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(getClass().getName(), "text sent: "+query);
        try {
            main.getGraph().findPath(query);
        } catch (NoPathException e) {
            Log.e(getClass().getName(), "No path was found: " + e.toString());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(getClass().getName(), "text modified: "+newText);
        return false;
    }
}
