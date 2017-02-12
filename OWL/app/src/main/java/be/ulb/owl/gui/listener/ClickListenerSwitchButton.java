package be.ulb.owl.gui.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.graph.Campus;
import be.ulb.owl.graph.Graph;

/**
 * Created by Detobel36
 */

public class ClickListenerSwitchButton implements View.OnClickListener {

    private final MainActivity _main;
    private final Graph _graph;

    public ClickListenerSwitchButton(MainActivity main, Graph graph) {
        this._main = main;
        this._graph = graph;
    }

    @Override
    public void onClick(View view) {
        switchPlan();
    }

    /**
     * Switch between the two different global plans
     */
    private void switchPlan() {
        ArrayList<String> campusName = new ArrayList<String>();
        for(Campus campus : _graph.getAllCampus()) {
            campusName.add(campus.getName());
        }

        final String[] items = campusName.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(_main);
        builder.setTitle(R.string.select_map);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                String name = items[item];
                Log.d(getClass().getName(), "Name of the map: " + name);
                _main.setCurrentPlan(_graph.getCampus(name));

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
