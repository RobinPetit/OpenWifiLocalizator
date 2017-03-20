package be.ulb.owl.gui.listener;

import android.view.View;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;

/**
 * Created by Detobel36
 */

public class ClickListenerSwitchButton implements View.OnClickListener {

    private final MainActivity _main;

    public ClickListenerSwitchButton(MainActivity main) {
        this._main = main;
    }

    @Override
    public void onClick(View view) {
        switchPlan();
    }

    /**
     * Switch between the two different global plans
     */
    private void switchPlan() {
        Plan currentPlanLocation = null;
        Node currentNode = _main.getCurrentLocation();
        if(currentNode != null) {
            currentPlanLocation = currentNode.getParentPlan();
        }

        if(currentPlanLocation != null && currentPlanLocation != _main.getCurrentPlan()) {
            _main.setCurrentPlan(currentPlanLocation);
        }

        // Current plan view
//        Plan current = _main.getCurrentPlan();
//        // New plan which must be viewed
//        Plan button = _main.getSwitchPlanButton();
//        if(button == null) {
//            button = _main.getCurrentLocation().getParentPlan(); //current.getCampus();
//        }
//
//        if(_main.getCurrentLocation() != null &&
//                current != _main.getCurrentLocation().getParentPlan() &&
//                button != _main.getCurrentLocation().getParentPlan())  {
//            current = _main.getCurrentLocation().getParentPlan(); //button.getCampus(); // current set as campus because switch
//        }
//        _main.setCurrentPlan(button);
//        _main.setSwitchPlanButtonImage(current);
    }

}
