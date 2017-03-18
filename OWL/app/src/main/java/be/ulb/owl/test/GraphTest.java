package be.ulb.owl.test;

import android.util.Log;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.scanner.Wifi;

/**
 * Created by Detobel36
 */

public class GraphTest extends Graph {

    public GraphTest(MainActivity main) {
        super(main);
    }


    @Override
    protected void localize(boolean displayNotFound, ArrayList<Wifi> sensedWifi) {
        ArrayList<Wifi> newSensedWifi = new ArrayList<>();

        // SCAN 1
//        newSensedWifi.add(new Wifi("82:37:73:e2:0c:ba", -60));
//        newSensedWifi.add(new Wifi("00:26:cb:4e:20:af", -62));
//        newSensedWifi.add(new Wifi("80:37:73:e2:0c:b9", -60));
//        newSensedWifi.add(new Wifi("00:26:cb:4e:20:ae", -62));
//        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:cf", -79));
//        newSensedWifi.add(new Wifi("6a:15:90:01:71:79", -88));
//        newSensedWifi.add(new Wifi("30:91:8f:65:fe:2d", -83));
//        newSensedWifi.add(new Wifi("20:c9:d0:23:80:af", -91));
//        newSensedWifi.add(new Wifi("86:25:19:00:98:fc", -79));
//        newSensedWifi.add(new Wifi("d0:c2:82:ae:d8:50", -84));
//        newSensedWifi.add(new Wifi("9c:97:26:12:91:44", -82));
//        newSensedWifi.add(new Wifi("28:c6:8e:81:6b:c2", -90));
//        newSensedWifi.add(new Wifi("bc:f2:af:8d:36:71", -89));
//        newSensedWifi.add(new Wifi("32:91:8f:65:fe:2e", -83));
//        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:c0", -73));
//        newSensedWifi.add(new Wifi("9e:97:26:12:91:47", -82));
//        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:c1", -73));
//        newSensedWifi.add(new Wifi("a0:1b:29:9b:b5:e6", -91));
//        newSensedWifi.add(new Wifi("6c:b0:ce:bb:93:66", -75));
//        newSensedWifi.add(new Wifi("f4:06:8d:51:c0:5c", -91));
//        newSensedWifi.add(new Wifi("32:91:8f:65:fe:20", -83));
//        newSensedWifi.add(new Wifi("00:26:cb:4e:68:e1", -79));
//        newSensedWifi.add(new Wifi("00:26:cb:4e:20:a0", -62));
//        newSensedWifi.add(new Wifi("a4:2b:b0:e1:3f:a8", -87));
//        newSensedWifi.add(new Wifi("88:63:df:b7:b5:d7", -85));
//        newSensedWifi.add(new Wifi("00:19:70:b4:6c:90", -90));
//        newSensedWifi.add(new Wifi("a2:1b:29:9b:b5:e8", -89));
//        newSensedWifi.add(new Wifi("82:37:73:e2:0c:bb", -57));
//        newSensedWifi.add(new Wifi("9e:97:26:12:91:4d", -85));
//        newSensedWifi.add(new Wifi("d0:c2:82:ae:d8:51", -83));
//        newSensedWifi.add(new Wifi("9e:97:26:12:91:45", -79));
//        newSensedWifi.add(new Wifi("00:78:9e:69:47:57", -90));
//        newSensedWifi.add(new Wifi("02:37:b7:6f:ce:97", -89));
//        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:ce", -79));
//        newSensedWifi.add(new Wifi("d0:bf:9c:a5:df:3b", -81));
//        newSensedWifi.add(new Wifi("00:37:b7:6f:ce:96", -90));
//        newSensedWifi.add(new Wifi("e0:b9:e5:56:fd:b5", -90));
//        newSensedWifi.add(new Wifi("00:19:70:81:97:95", -88));
//        newSensedWifi.add(new Wifi("6a:15:90:01:71:7a", -89));
//        newSensedWifi.add(new Wifi("00:26:cb:4e:20:a1", -70));
//        newSensedWifi.add(new Wifi("68:15:90:01:71:79", -87));
//        newSensedWifi.add(new Wifi("a2:1b:29:9b:b5:e7", -90));
//        newSensedWifi.add(new Wifi("02:37:b7:6f:ce:98", -91));

        // SCAN 2
        newSensedWifi.add(new Wifi("82:37:73:e2:0c:ba", -74));
        newSensedWifi.add(new Wifi("00:26:cb:4e:20:a0", -76));
        newSensedWifi.add(new Wifi("a4:2b:b0:e1:3f:a8", -91));
        newSensedWifi.add(new Wifi("00:26:cb:4e:20:af", -81));
        newSensedWifi.add(new Wifi("82:37:73:e2:0c:bb", -79));
        newSensedWifi.add(new Wifi("80:37:73:e2:0c:b9", -74));
        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:cf", -82));
        newSensedWifi.add(new Wifi("00:26:cb:4e:20:ae", -77));
        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:ce", -82));
        newSensedWifi.add(new Wifi("d0:bf:9c:a5:df:3b", -86));
        newSensedWifi.add(new Wifi("9c:97:26:12:91:44", -89));
        newSensedWifi.add(new Wifi("02:78:9e:69:47:58", -94));
        newSensedWifi.add(new Wifi("e0:b9:e5:63:91:9d", -88));
        newSensedWifi.add(new Wifi("00:26:cb:9f:b1:c0", -81));
        newSensedWifi.add(new Wifi("6c:b0:ce:bb:93:66", -81));


        Log.i(getClass().getName(), "TEST: change sensed wifi: " + newSensedWifi.toString());
        super.localize(displayNotFound, newSensedWifi);
    }


}
