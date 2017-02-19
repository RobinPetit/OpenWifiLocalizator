package be.ulb.owl.scanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.ulb.owl.MainActivity;
import be.ulb.owl.event.EventPriority;
import be.ulb.owl.event.ScanWifiUpdateEvent;

/**
 * Manage wifi activator and scan task
 * 
 * @author denishoornaert
 */
public class Scanner {

    private final MainActivity _main;

    private static HashMap<ScanWifiUpdateEvent, EventPriority> _eventScanWifiUpdate =
            new HashMap<ScanWifiUpdateEvent, EventPriority>();


    private final int SCAN_TIME_INTERVAL = 10;  // in seconds

    private ScanTask _scanTask = null;

    private boolean _defaultWifiEnable = true;
    private WifiManager _wifiManager = null;
    private List<ScanResult> _lastResult = null; // two last scanned point (check if player move (debug))
    private ArrayList<Wifi> _lastWifiAccess = new ArrayList<Wifi>();


    /**
     * Constructor (call when the app start)
     */
    public Scanner(MainActivity main) {
        _main = main;

        forceEnableWifi();

        if(!_wifiManager.isWifiEnabled()) {
            Log.d(getClass().getName(), "Wifi disable");
        }


        if(!_wifiManager.startScan()) {
            Log.e(getClass().getName(), "WifiManager could not start a scan :/");
        }

    }



    ////////////////////////////////////////// MAKE SCAN //////////////////////////////////////////

    /**
     * Calcul the average of a test
     *
     * @param tmp list of the frequence
     * @return the average
     */
    private int avg(ArrayList<Integer> tmp) {
        int sum = 0;
        for(int elem : tmp) {
            sum += elem;
        }
        return sum/tmp.size();
    }


    /**
     * Save all wifi in the _accessPoints attribute
     *
     * @return HashMap with bss and avg of signal OR null if same that the last request
     */
    protected HashMap<String, Integer> getData() {
        _wifiManager.startScan(); // TODO Test débug
        List<ScanResult> results = _wifiManager.getScanResults();

        if(updateLastResult(results)) {
            Log.w(Scanner.class.getName(), "Scan have not change !");
            return null;
        }

        HashMap<String, Integer> res = new HashMap<String, Integer>();
        for (ScanResult scanRes :results) {
            String key = scanRes.BSSID;
            Integer value = scanRes.level;

            if (!res.containsKey(key)) {
                res.put(key, value);

            } else {
                Log.w(getClass().getName(), "Scan detect two same bss !!!");

            }

        }

        return res;
    }


    /**
     * Check if current result of wifi manager equals to the new scan.  If it's different, update
     * this values
     * 
     * @param results list of scanned wifi
     * @return True if the result have been update
     */
    private boolean updateLastResult(List<ScanResult> results) {
        boolean res = _lastResult != null && _lastResult.equals(results);
        if(res) {
            _lastResult = results;
        }
        return res;
    }



    ///////////////////////////////////// INFO FROM SCAN-TASK /////////////////////////////////////

    /**
     * ScanTask have finish and give the new scan result
     *
     * @param newScan the new scan resuls
     */
    protected void newScanFinish(HashMap<String, ArrayList<Integer>> newScan) {
        ArrayList<Wifi> temp = new ArrayList<Wifi>();

        for(String key : newScan.keySet()) {
            ArrayList<Integer> values = newScan.get(key);
            temp.add(new Wifi(key, avg(values), -1));
        }

        if(temp.isEmpty()) {
            Log.i(Scanner.class.getName(), "No wifi in this area");
        }

        _lastWifiAccess = temp;


        // TODO optimiser (deux boucles pour la même chose)
        for(ScanWifiUpdateEvent event : _eventScanWifiUpdate.keySet()) {
            if(_eventScanWifiUpdate.get(event) == EventPriority.FIRST) {
                event.scanWifiUpdateEvent(temp);
            }
        }

        for(ScanWifiUpdateEvent event : _eventScanWifiUpdate.keySet()) {
            if(_eventScanWifiUpdate.get(event) == EventPriority.LAST) {
                event.scanWifiUpdateEvent(temp);
            }
        }

        _scanTask = null;
        startScanTask();
    }


    /////////////////////// ACTIVE/DESACTIVE WIFI ////////////:

    /**
     * Init the wifiManager
     *
     * @return True if all is ok (False if we can't init it)
     */
    public boolean initWifiManager() {
        if(_wifiManager == null) {
            Object service = _main.getSystemService(Context.WIFI_SERVICE);
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
    private boolean forceEnableWifi() {
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
     * Stop scan task
     */
    public void stopScanTask() {
        if(_scanTask != null) {
            _scanTask.cancel(true);
        }
        _scanTask = null;
    }


    /**
     * Start a scan task (but wait some second before the test)
     */
    public void startScanTask() {
        startScanTask(false);
    }

    /**
     * Start a scan task
     *
     * @param forceStart True to begin scan directly
     */
    public void startScanTask(boolean forceStart) {
        if(_scanTask == null) {
            _scanTask = new ScanTask(this, forceStart ? 0 : SCAN_TIME_INTERVAL);
            _scanTask.execute();

        } else {
            Log.e(getClass().getName(), "ScanTask allready exist !");
        }

    }


    public void forceRestartScanTask() {
        stopScanTask();
        startScanTask(true);
    }


    /////////////////////////////////////// GETTER & SETTER ///////////////////////////////////////

    /**
     * Get the last result of wifi access scan
     *
     * @return an ArrayList with all last scanned wifi
     */
    public ArrayList<Wifi> getLastWifiAccess() {
        return _lastWifiAccess;
    }



    /////////////////////////////////////// LISTENER EVENT ///////////////////////////////////////

    public static void addEventUpdateWifi(ScanWifiUpdateEvent event) {
        addEventUpdateWifi(event, EventPriority.LAST);
    }

    public static void addEventUpdateWifi(ScanWifiUpdateEvent event, EventPriority eventPriority) {
        _eventScanWifiUpdate.put(event, eventPriority);
    }

}
