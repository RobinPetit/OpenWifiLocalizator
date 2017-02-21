package be.ulb.owl.gui.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Plan;

/**
 * Created by Detobel36
 */

public class ClickListenerChoseLocal implements View.OnClickListener {

    private final MainActivity _main;


    public ClickListenerChoseLocal(MainActivity main) {
        _main = main;
    }


    @Override
    public void onClick(View view) {
        searchLocal();
    }


    /**
     * Search a local
     */
    private void searchLocal() {
        final String[] listFinal = getAllAlias();
        AlertDialog.Builder builder = new AlertDialog.Builder(_main);
        builder.setTitle(R.string.select_local);
        builder.setItems(listFinal, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    _main.setDestination(listFinal[item]);
                } catch(NoPathException e) {
                    Log.e(getClass().getName(), "No path was found: " + e.toString());
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * Get all alias of current plan
     *
     * @return all alias of the current plan
     */
    private String[] getAllAlias() { // TODO Move this in Graph, main, ... ?
        String[] items = new String[]{};
        Plan currentPlan = _main.getCurrentPlan();
        if(currentPlan != null) {
            HashSet<String> allAlias = currentPlan.getAllAlias();

            Log.d(this.getClass().getName(), allAlias.toString());

            if(allAlias.size() > 0) {
                items = new String[allAlias.size()];
                items = allAlias.toArray(items);
            }
        }

        Arrays.sort(items);
        return items;
    }


}
