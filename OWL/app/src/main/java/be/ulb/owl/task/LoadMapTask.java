package be.ulb.owl.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import be.ulb.owl.graph.Campus;

/**
 * @author Detobel36
 */

public class LoadMapTask extends AsyncTask<ArrayList<Campus>, Void, Void> {


    @Override
    protected Void doInBackground(ArrayList<Campus>... allCampus) {
        for(ArrayList<Campus> listCampus : allCampus) {
            for(Campus campus : listCampus) {
                campus.loadAllPlan();
            }
        }
        Log.d(getClass().getName(), "End in background");

        return null;
    }



    @Override
    protected void onPostExecute(Void v) {
        Log.d(getClass().getName(), "Post execute !");
    }


    ////////////////////////////////////////// STATIC //////////////////////////////////////////



}
