package be.ulb.owl;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.*;
import java.lang.Object;

/**
 * Scan all wifi and stock the last wifi status
 * 
 * @author denishoornaert
 */
public class Scanner {

    private static final MainActivity main = MainActivity.getInstance();
    private static final int SCAN_TIME_INTERVAL = 10;  // in seconds

    private Thread _scanTask = null;
    private boolean _taskInProgress = false;

    private boolean _defaultWifiEnable = true;
    private static WifiManager _wifiManager = null;
    private static HashMap<String, ArrayList<Integer>> _accesPoints; // last scanned point
    private static List<ScanResult> _lastRestult = null; // two last scanned point (check if player move (debug))

    /**
     * Constructor (call when the app start)
     */
    public Scanner() {
        forceEnableWifi();

        if(!_wifiManager.isWifiEnabled()) {
            Log.d(getClass().getName(), "Wifi disable");
        }
        _wifiManager.startScan();
        _accesPoints = new HashMap<String, ArrayList<Integer>>();
    }

    /**
     * Calcul the average of a test
     *
     * @param tmp list of the frequence
     * @return the average
     */
    private Integer avg(ArrayList<Integer> tmp) {
        Integer sum = 0;
        for (Integer elem : tmp) {
            sum += elem;
        }
        return sum/tmp.size();
    }

    /**
     * Save all wifi in the _accessPoints attribute
     */
    public static void getData() {
        List<ScanResult> results = _wifiManager.getScanResults();

        Log.d(Scanner.class.getName(), "Get DATA: " + results.toArray().toString());

        if(_lastRestult != null && _lastRestult.equals(results)) {
            Log.w(Scanner.class.getName(), "");
        }

        _lastRestult = results;

        for (ScanResult res :results) {
            String key = res.BSSID;
            Integer value = res.level;
            if (!_accesPoints.containsKey(key)) {
                ArrayList<Integer> listAcces = new ArrayList<Integer>();
                listAcces.add(value);
                _accesPoints.put(key, listAcces);
                Log.d(Scanner.class.getName(), "Get DATA: create " + key + " -> " + value);

            } else {
                _accesPoints.get(key).add(value);
                Log.d(Scanner.class.getName(), "Get DATA: add " + key + " -> " + value +
                        " (res " + _accesPoints.get(key) + ")");
            }
        }
    }

    public static void resetAccesPoint() {
        _accesPoints.clear();
    }

    /**
     * Scan all around wifi
     *
     * @return the list of all wifi
     */
//    public ArrayList<Wifi> scan() {
//        _accesPoints.clear();
//        ArrayList<Wifi> temp = new ArrayList<Wifi>();
//        for (int i = 0; i < 3; i++) {
//            getData();
//        }
//
//        for(String key : _accesPoints.keySet()) {
//            ArrayList<Integer> values = _accesPoints.get(key);
//            temp.add(new Wifi(key, avg(values), -1));
//        }
//
//        if(temp.isEmpty()) {
//            Log.i(getClass().getName(), "No wifi in this area");
//        }
//
//        return temp;
//    }


    /**
     * Init the wifiManager
     *
     * @return True if all is ok (False if we can't init it)
     */
    public boolean initWifiManager() {
        if(_wifiManager == null) {
            Object service = main.getSystemService(Context.WIFI_SERVICE);
            if(service instanceof WifiManager) {
                _wifiManager = (WifiManager) service;
                return true;
            }
        }
        return false;
    }

    /**
     * Force ON wifi and record the current statut of wifi
     *
     * @return True if operation success
     */
    public boolean forceEnableWifi() {
        initWifiManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                _wifiManager.isScanAlwaysAvailable()) {
            Log.i(getClass().getName(), "ScanAlwaysAvailable Enjoy !");
            return true;
        }

        if(_wifiManager != null) {
            _defaultWifiEnable = _wifiManager.isWifiEnabled();
            return _wifiManager.setWifiEnabled(true);
        }
        return false;
    }

    /**
     * Reset the status of the wifi (berfore he was forced to ON)
     */
    public void resetWifiStatus() {
        if(_wifiManager != null) {
            _wifiManager.setWifiEnabled(_defaultWifiEnable);
        }
    }

    /**
     * Start the scan task
     */
    public void startScanTask() {
        if(!_taskInProgress && _scanTask == null) {
            _scanTask = new Thread() {
                @Override
                public void run() {
                    while(!isInterrupted() && _taskInProgress) {
                        try {
                            // sleep a given amount of time
                            Thread.sleep(1000 * SCAN_TIME_INTERVAL);
                            // and then try to localize user
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    main.localize(false);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            _taskInProgress = true;
            _scanTask.start();

        }
    }

    /**
     * Stop temporary the scan thread
     */
    public void stopScanTask() {
        _scanTask.interrupt();
        _taskInProgress = false;
        _scanTask = null;
    }

}
