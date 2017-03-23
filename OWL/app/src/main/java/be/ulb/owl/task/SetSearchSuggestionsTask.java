package be.ulb.owl.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by robin on 23/03/17.
 */

public class SetSearchSuggestionsTask extends AsyncTask<SetSearchSuggestionsTaskParam, Void, Void> {

    private long _startingTime = 0;

    @Override
    protected Void doInBackground(SetSearchSuggestionsTaskParam... params) {
        _startingTime = System.currentTimeMillis();
        ArrayList<String> suggestions = params[0].getSuggestions();
        params[0].getSearchView().addSuggestions(suggestions);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        long endingTime = System.currentTimeMillis();
        Log.d(getClass().getName(), "Suggestions added, took " + (endingTime - _startingTime) + " ms");
    }
}
