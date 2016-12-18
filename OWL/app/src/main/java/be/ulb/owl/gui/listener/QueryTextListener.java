package be.ulb.owl.gui.listener;

import android.util.Log;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.NoPathException;
import br.com.mauker.materialsearchview.MaterialSearchView;

/**
 * All event
 *
 * @author Detobel36
 */
public class QueryTextListener implements MaterialSearchView.OnQueryTextListener {

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(getClass().getName(), "text sent: "+query);
        try {
            MainActivity.getInstance().getGraph().findPath(query);
        } catch (NoPathException e) {
            Log.e(getClass().getName(), "No path was found: " + e.toString());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) { return false; }

}
