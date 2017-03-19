/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.scanner;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Information about a wifi scan
 * 
 * @author Detobel36
 */
public class Wifi {
    
    private final String _BSS;
    private final float _avg;
    private final float _variance;

    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param value signal of this wifi
     */
    public Wifi(String BSS, float value) {
        this(BSS, value, -1);
    }
    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param avg average of the signal
     * @param variance variance of all signal
     */
    public Wifi(String BSS, float avg, float variance) {
        _BSS = BSS;
        _avg = Math.abs(avg);
        _variance = variance;
    }
    
    /**
     * Get the BSS
     * 
     * @return the BSS
     */
    public String getBSS() {
        return _BSS;
    }

    public boolean equals(Wifi wifi) {
        return _BSS.equals(wifi.getBSS());
    }

    public float getAvg() {
        return _avg;
    }

    public float getVariance() {
        return _variance;
    }


    /////////////// STATIC ///////////////

    /**
     * Convert a list of wifi on a list of bss string
     *
     * @param listWifi list of wifi which must be convert
     * @return an HashSet of string with all BSS
     */
    public static HashSet<String> wifiListToBssList(ArrayList<Wifi> listWifi) {
        HashSet<String> allBss = new HashSet<>();
        for(Wifi wifi : listWifi) {
            allBss.add(wifi.getBSS());
        }
        return allBss;
    }

}
