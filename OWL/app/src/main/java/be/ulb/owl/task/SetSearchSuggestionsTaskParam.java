package be.ulb.owl.task;

import java.util.ArrayList;

import br.com.mauker.materialsearchview.MaterialSearchView;

/**
 * Created by robin on 23/03/17.
 */

public class SetSearchSuggestionsTaskParam {
    private MaterialSearchView _searchView;
    private ArrayList<String> _suggestions;

    public SetSearchSuggestionsTaskParam(MaterialSearchView searchView, ArrayList<String> suggestions) {
        _searchView = searchView;
        _suggestions = suggestions;
    }

    public MaterialSearchView getSearchView() {
        return _searchView;
    }

    public ArrayList<String> getSuggestions() {
        return _suggestions;
    }
}
