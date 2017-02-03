/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

/**
 * Information about a wifi scan
 * 
 * @author Detobel36
 */
public class Wifi {
    
    private final String _BSS;
    private final float _max;
    private final float _min;
    private final float _avg;
    private final float _variance;

    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param value signal of this wifi
     */
    public Wifi(String BSS, float value) {
        this(BSS, value, value, value, -1);
    }
    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param max max signal
     * @param min min signal
     * @param avg average of the signal
     * @param variance variance of all signal
     */
    public Wifi(String BSS, float max, float min, float avg, float variance) {
        _BSS = BSS;
        _max = max;
        _min = min;
        _avg = avg;
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

    public float getMax() { return _max; }

    public float getMin() { return _min; }

    public float getVariance() {
        return _variance;
    }
    
}
