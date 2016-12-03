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
    
    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param value signal of this wifi
     */
    public Wifi(String BSS, float value) {
        this(BSS, value, value, value);
    }
    
    /**
     * Init a wifi signal
     * 
     * @param BSS the BSSID
     * @param max max signal
     * @param min min signal
     * @param avg average of the signal
     */
    public Wifi(String BSS, float max, float min, float avg) {
        _BSS = BSS;
        _max = max;
        _min = min;
        _avg = avg;
    }
    
    /**
     * Get the BSS
     * 
     * @return the BSS
     */
    public String getBSS() {
        return _BSS;
    }
    
    /**
     * Check if the wifi have a good Likelyhood
     * 
     * @param wifi the wifi who must be compare
     * @return the score of likelyhood (or 0 if BSS are not the same)
     */
    public int getLikelyhood(Wifi wifi) {
        int res = 0;
        if(_BSS.equals(wifi.getBSS())) {
            // TODO compare wifi
            // cc @RobinPetit
        }
        return res;
    }
    
    
}
