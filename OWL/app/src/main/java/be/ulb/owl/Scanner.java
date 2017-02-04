package be.ulb.owl;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.*;
import java.lang.Object;

/**
 * Scan all wifi
 * 
 * @author denishoornaert
 */
public class Scanner {

    private static final MainActivity main = MainActivity.getInstance();
    private static final int SCAN_TIME_INTERVAL = 2;  // in seconds

    private Thread _scanTask = null;
    private boolean _taskInProgress = false;

    private boolean _defaultWifiEnable = true;
    private WifiManager _wifiManager = null;
    private HashMap<String, ArrayList<Integer>> _accesPoints;
    private List<ScanResult> _lastRestult = null;

    public Scanner () {
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
    private void getData() {
        List<ScanResult> results = _wifiManager.getScanResults();

        if(_lastRestult != null && _lastRestult.equals(results)) {
            Log.w(getClass().getName(), "");
        }

        _lastRestult = results;

        for (ScanResult res :results) {
            String key = res.BSSID;
            Integer value = res.level;
            if (!_accesPoints.containsKey(key)) {
                _accesPoints.put(key, new ArrayList<Integer>());
                _accesPoints.get(key).add(value);
            }
            else {
                _accesPoints.get(key).add(value);
            }
        }
    }

    /**
     * Scan all around wifi
     *
     * @return the list of all wifi
     */
    public ArrayList<Wifi> scan() {
        _accesPoints.clear();
        ArrayList<Wifi> temp = new ArrayList<Wifi>();
        for (int i = 0; i < 5; i++) {
            getData();
        }
        
        for(String key : _accesPoints.keySet()) {
            ArrayList<Integer> values = _accesPoints.get(key);
            temp.add(new Wifi(key, avg(values), -1));
        }

        if(temp.isEmpty()) {
            Log.i(getClass().getName(), "No wifi in this area");
        }

        return temp;
    }


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
