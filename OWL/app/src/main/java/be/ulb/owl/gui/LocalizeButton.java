package be.ulb.owl.gui;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.event.EventPriority;
import be.ulb.owl.event.ScanWifiUpdateEvent;
import be.ulb.owl.graph.Graph;
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
    }


    private void initButton() {
        _localizeButton = (ImageButton)_main.findViewById(R.id.localizeButton);

        _localizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!_refreshInProgress) {
                    _refreshInProgress = true;

                    _graph.setDisplayNotFound(true);
                    _graph.loadAllWifi();

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
    public void scanWifiUpdateEvent(ArrayList<Wifi> listWifi) {
        _refreshInProgress = false;
        toggleLoaderAndButton();
        // TODO Detobel si wifi pas trouvé
    }

}
