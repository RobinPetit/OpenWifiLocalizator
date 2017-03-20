package be.ulb.owl.gui;

import android.graphics.Point;
import android.view.Display;
import android.widget.ImageButton;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.listener.ClickListenerSwitchButton;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * @author Detobel36
 */

public class SwitchButton {

    private final MainActivity _main;
    private ImageButton _changePlan;

    public SwitchButton(MainActivity main) {
        _main = main;

        _changePlan = (ImageButton)_main.findViewById(R.id.changePlan);
        _changePlan.setOnClickListener(new ClickListenerSwitchButton(_main));
        Display display = _main.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        _changePlan.getLayoutParams().height = size.x/5;
        _changePlan.getLayoutParams().width= size.x/5;
        _changePlan.requestLayout();
        setButtonInvisible();
    }


    public void updateSwitchButton(Plan newPlan) {
        Node currentLocationNode = _main.getCurrentLocation();
        // If new plan is null or if the location node is null or the new plan is the plan of
        // the current location ==> invisible
        if(newPlan == null || currentLocationNode == null ||
                newPlan == currentLocationNode.getParentPlan()) {
            setButtonInvisible();
        } else {
            Point size = new Point();
            _main.getWindowManager().getDefaultDisplay().getSize(size);
            int sizeInt = 100;
            if (size.x / 7 < sizeInt) {
                sizeInt = size.x / 7;
            }
            _changePlan.getLayoutParams().height = sizeInt;
            _changePlan.getLayoutParams().width = sizeInt;
            _changePlan.requestLayout();
            setButtonVisible();
            _changePlan.setImageDrawable(newPlan.getDrawableImage());
        }
    }

    public void setButtonVisible() {
        _changePlan.setVisibility(VISIBLE);
    }

    public void setButtonInvisible() {
        _changePlan.setVisibility(INVISIBLE);
    }

}
