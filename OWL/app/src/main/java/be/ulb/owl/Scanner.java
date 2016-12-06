package be.ulb.owl;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Scan all wifi
 * 
 * @author denishoornaert
 */
public class Scanner {

    private static final MainActivity main = MainActivity.getInstance();

    private boolean _defaultWifiEnable = true;
    private WifiManager _wifiManager = null;

    private String _network;
    private HashMap<String, ArrayList<Float>> _accesPoints;
    private Runtime _r;

    public Scanner () {
        initWifiManager();

        _wifiManager.startScan();
        List<ScanResult> result = _wifiManager.getScanResults();
        // TODO

        _network = "wlp3s0";
        _accesPoints = new HashMap<String, ArrayList<Float>>();
        _r = Runtime.getRuntime();
    }

    private Float avg (ArrayList<Float> tmp) {
        Float sum = 0.0f;
        for (Float elem : tmp) {
            sum += elem;
        }
        return sum/tmp.size();
    }

    private BufferedReader getData () {
        BufferedReader bufferedreader = null;
        try {
            Process p = _r.exec("iw dev "+_network+" scan");
            InputStream in = p.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            InputStreamReader inread = new InputStreamReader(buf);
            bufferedreader = new BufferedReader(inread);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return bufferedreader;
    }

    private void parse (BufferedReader data) {
        try {
            String key = "";
            String line = "";
            while ((line = data.readLine()) != null) {
                if ((line.substring(0, 3)).equals("BSS")) {
                    key = line.substring(4, 21);
                }
                else if (line.substring(0, 7).equals("\tsignal")) {
                    if (!_accesPoints.containsKey(key)) {
                        System.out.println("put");
                        _accesPoints.put(key, new ArrayList<Float>());
                        _accesPoints.get(key).add(Float.valueOf(line.substring(8, line.length()-4)));
                    }
                    else {
                        System.out.println("add");
                        _accesPoints.get(key).add(Float.valueOf(line.substring(8, line.length()-4)));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<Wifi> scan () {
        ArrayList<Wifi> temp = new ArrayList<Wifi>();
        for (int i = 0; i < 5; i++) {
            parse(getData());
        }
        
        for(String key : _accesPoints.keySet()) {
            ArrayList<Float> values = _accesPoints.get(key);
            temp.add(new Wifi(key, Collections.max(values), 
                    Collections.min(values), avg(values)));
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

}
