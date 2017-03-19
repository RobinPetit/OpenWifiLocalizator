package be.ulb.owl.event;

import java.util.ArrayList;

import be.ulb.owl.graph.Plan;
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
     * @param listPlan list of the plan which match with the list of scanned wifi
     */
    void scanWifiUpdateEvent(ArrayList<Wifi> listWifi, ArrayList<Plan> listPlan);

}
