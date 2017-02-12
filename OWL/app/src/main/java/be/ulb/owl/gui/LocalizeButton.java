package be.ulb.owl.gui;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.event.EventPriority;
import be.ulb.owl.event.ScanWifiUpdateEvent;
import be.ulb.owl.scanner.Scanner;
import be.ulb.owl.scanner.Wifi;

/**
 * Created by Detobel36
 */

public class LocalizeButton implements ScanWifiUpdateEvent {

    private final MainActivity _main;
    private final Scanner _scanner;

    private ProgressBar _loadBar;
    private Button _localizeButton;

    private boolean _refreshInProgress = false;



    public LocalizeButton(MainActivity main, Scanner scanner) {
        _main = main;
        _scanner = scanner;

        Scanner.addEventUpdateWifi(this, EventPriority.FIRST);

        initButton();
        initLoadBar();
    }

    private void initButton() {
        _localizeButton = (Button)_main.findViewById(R.id.localizeButton);

        _localizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!_refreshInProgress) {
                    _refreshInProgress = true;
                    toggleLoaderAndButton();
                    _scanner.forceRestartScanTask();

                } else {
                    // TODO allready in progress

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
        // TODO
    }

}
