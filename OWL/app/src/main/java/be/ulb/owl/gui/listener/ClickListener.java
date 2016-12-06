package be.ulb.owl.gui.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Plan;

/**
 * Event when a click is make
 *
 * @author  Detobel36
 */

public class ClickListener implements View.OnClickListener {

    private static final MainActivity main = MainActivity.getInstance();


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.changePlan:
                switchPlan();
                break;

            case R.id.local:
                searchLocal();
                break;

        }
    }


    /**
     * Switch between the two different global plans
     */
    private void switchPlan(){
        final String[] items = {"Plaine", "Solbosch", "P.F"}; // TODO Remove test (P.F)

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                String name = items[item];
                main.setCurrentPlan(Graph.getPlan(name));

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Search a local
     */
    private void searchLocal() {

        final String[] items;
        Plan currentPlan = main.getCurrentPlan();
        if(currentPlan != null) {
            ArrayList<String> allAlias = currentPlan.getAllAlias();
            Log.d(this.getClass().getName(), allAlias.toString());

            if(!allAlias.isEmpty() && allAlias.toArray() instanceof String[]) {
                items = (String[]) allAlias.toArray();
            } else {
                items = new String[]{};
            }

        } else {
            items = (String[]) Arrays.asList("Exemple1", "Exemple2").toArray();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle("Locaux");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.i(getClass().getName(), "Sélection du local : "+items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


}
