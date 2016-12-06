package be.ulb.owl.gui.listener;

import android.support.v7.widget.SearchView;
import android.util.Log;

/**
 * All event
 *
 * @author Detobel36
 */
public class QueryTextListener implements SearchView.OnQueryTextListener {

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(getClass().getName(), "text envoyé : "+query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(getClass().getName(), "text modifié : "+newText);
        return false;
    }
}
