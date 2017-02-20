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
import be.ulb.owl.graph.Plan;

/**
 * Created by Detobel36
 */

public class ClickListenerSwitchButton implements View.OnClickListener {

    private final MainActivity _main;
    private final Graph _graph;
    private boolean _currentIsShown = true;

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
        Plan current = _main.getCurrentPlan();
        Plan button = _main.getSwitchPlanButton();
        if(button == null) {
            button = current.getCampus();
        }
        if(current.isPlan() && button.isPlan()) {
            current = button.getCampus(); // current set as campus because switch
        }
        _main.setCurrentPlan(button);
        _main.setSwitchPlanButtonImage(current);
    }

}
