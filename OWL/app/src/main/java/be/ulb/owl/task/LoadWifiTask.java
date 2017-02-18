package be.ulb.owl.task;

import android.os.AsyncTask;

import be.ulb.owl.graph.Graph;

/**
 * Background task to load all Wifi
 *
 * @author Detobel36
 */

public class LoadWifiTask extends AsyncTask<Void, Void, Void> {

    private final Graph _graph;

    public LoadWifiTask(Graph graph) {
        _graph = graph;
    }


    @Override
    protected Void doInBackground(Void... vide) {
        // TODO
        return null;
    }



    @Override
    protected void onPostExecute(Void v) {
        // TODO
        _graph.setAllWifiLoaded();
    }




}