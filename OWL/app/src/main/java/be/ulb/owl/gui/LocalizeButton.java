package be.ulb.owl.gui;

import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.event.EventPriority;
import be.ulb.owl.event.ScanWifiUpdateEvent;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.scanner.Scanner;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.utils.DialogUtils;

/**
 * Created by Detobel36
 */

public class LocalizeButton implements ScanWifiUpdateEvent {

    private final MainActivity _main;
    private final Scanner _scanner;
    private final Graph _graph;

    private ProgressBar _loadBar;
    private ImageButton _localizeButton;

    private boolean _refreshInProgress = false;



    public LocalizeButton(MainActivity main, Scanner scanner, Graph graph) {
        _main = main;
        _scanner = scanner;
        _graph = graph;

        Scanner.addEventUpdateWifi(this, EventPriority.FIRST);

        initButton();
        initLoadBar();
        setSize();
    }

    private void setSize() {
        Display display = _main.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int sizeInt = 100;
        if(size.x/7 < sizeInt) {
            sizeInt = size.x/7;
        }
        _localizeButton.getLayoutParams().height = sizeInt;
        _localizeButton.getLayoutParams().width = sizeInt;
        _localizeButton.requestLayout();
        _loadBar.getLayoutParams().height = sizeInt;
        _loadBar.getLayoutParams().width = sizeInt;
        _loadBar.requestLayout();
    }

    private void initButton() {
        _localizeButton = (ImageButton)_main.findViewById(R.id.localizeButton);

        _localizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!_refreshInProgress) {
                    _refreshInProgress = true;

                    _graph.setDisplayNotFound(true);

                    toggleLoaderAndButton();
                    _scanner.forceRestartScanTask();

                } else {
                    DialogUtils.infoBox(_main, R.string.in_progress, R.string.in_progress_localize);

                }

            }
        });

    }


    private void initLoadBar() {
        _loadBar = (ProgressBar)_main.findViewById(R.id.progressBar);
        _loadBar.setVisibility(View.GONE);
    }


    private void toggleLoaderAndButton() {
        if(_refreshInProgress) {
            _loadBar.setVisibility(View.VISIBLE);
            _localizeButton.setVisibility(View.GONE);

        } else {
            _loadBar.setVisibility(View.GONE);
            _localizeButton.setVisibility(View.VISIBLE);

        }

    }


    @Override
    public void scanWifiUpdateEvent(ArrayList<Wifi> listWifi, ArrayList<Plan> listPlan) {
        _refreshInProgress = false;
        toggleLoaderAndButton();
        // TODO Detobel si wifi pas trouvé
    }

}
