package be.ulb.owl.gui.listener;

import android.view.View;

import be.ulb.owl.MainActivity;
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

        if(current.getCampus() != null && button.getCampus() != null) {
            current = button.getCampus(); // current set as campus because switch
        }
        _main.setCurrentPlan(button);
        _main.setSwitchPlanButtonImage(current);
    }

}
