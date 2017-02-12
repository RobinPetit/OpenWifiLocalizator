package be.ulb.owl.event;

import java.util.ArrayList;

import be.ulb.owl.scanner.Wifi;

/**
 * Event when the wifi scan have been updated
 *
 * @author Detobel36
 */

public interface ScanWifiUpdateEvent {

    /**
     * Event call when the list of wifi have been updated
     *
     * @param listWifi list of the scanned wifi
     * @return False to stop the event
     */
    boolean scanWifiUpdateEvent(ArrayList<Wifi> listWifi);

}
