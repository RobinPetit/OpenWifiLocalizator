package be.ulb.owl.event;

import java.util.ArrayList;

import be.ulb.owl.scanner.Wifi;

/**
 * Event when the wifi scan have been updated
 *
 * @author Detobel36
 */

public interface ScanWifiUpdateEvent {

    void scanWifiUpdateEvent(ArrayList<Wifi> listWifi);

}
