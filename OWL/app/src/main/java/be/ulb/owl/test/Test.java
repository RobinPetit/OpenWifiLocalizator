package be.ulb.owl.test;

import android.util.Log;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

/**
 * Created by Detobel36
 */

public class Test {

    private static final MainActivity main = MainActivity.getInstance();



    public static void testBestPath() {
        if(!MainActivity.isTest()) {
            Log.w(Test.class.getName(), "Test mode is disabled !");
            return;
        }

        Graph graph = main.getGraph();

        // 57 & 3
        int[] startingEnd = {21, 57};
        int[] arrivalEnd = {14, 3};
        assert(startingEnd.length == arrivalEnd.length);
        ArrayList<Node> allNodes = graph .getAllNodes();

        for(int i = 0; i < startingEnd.length; ++i) {
            Node src = allNodes.get(startingEnd[i]);
            Node dest = allNodes.get(arrivalEnd[i]);
            Log.i(Test.class.getName(), "Testing best path between nodes " + src.getID() + " and " + dest.getID());
            try {
                ArrayList<Path> overallPath = graph .bestPath(src, dest);
                String pathString = ""+src.getID();
                Node current = src;
                int k = 0;
                while(!current.equals(dest)) {
                    current = overallPath.get(k++).getOppositeNodeOf(current);
                    pathString += " --> " + current.getID();
                }

                Log.i(Test.class.getName(), "Found path is given by: " + pathString);
                if(i == 1) {
                    main.drawPath(overallPath);
                }

            } catch (NoPathException e) {
                Log.e(Test.class.getName(), "No path has been found between nodes " + startingEnd[i]
                        + " and " + arrivalEnd[i] + " even though it was supposed to!");
            }
        }
    }


    public static void testWifi() {
        Graph mainGraph = main.getGraph();

        if(!(mainGraph instanceof GraphTest) || !MainActivity.isTest()) {
            Log.w(Test.class.getName(), "Test mode is disabled !");
            return;
        }

        GraphTest graph = (GraphTest) mainGraph;
        // ---- test ----

        ArrayList<Wifi> tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 71.2f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 84.2f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 75.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 70.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 52.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 61.2f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:36", 81.2f, 0f));
        tmp.add(new Wifi("d0:57:4c:cb:4a:71", 77.0f, 0f));
        tmp.add(new Wifi("fe:52:8d:c6:63:5a", 86.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 71.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 78.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 64.5f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 74.5f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 67.5f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 64.7f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:ca", 84.7f, 0f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 84.3f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 89.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 79.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 78.7f, 0f));
        tmp.add(new Wifi("00:37:b7:64:c3:66", 90.0f, 0f));

        Node position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 1.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 66.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 81.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 58.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 76.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 64.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 79.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 82.0f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 92.0f, 0f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 86.6f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 76.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 85.2f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 79.4f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 80.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 71.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 74.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 77.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 73.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 77.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 70.3f, 0f));

        position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 2.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 37.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 82.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 83.2f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 33.2f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 88.0f, 0f));

        position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 3.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 57.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 71.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 52.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 78.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 80.2f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 88.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 91.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 91.0f, 0f));
        tmp.add(new Wifi("24:b6:57:8d:34:41", 80.6f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 69.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 67.8f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 71.2f, 0f));

        position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 4.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 86.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 82.4f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 69.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 71.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a0", 71.2f, 0f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 88.4f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 85.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 87.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 84.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 82.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 76.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 76.3f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 76.7f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 84.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 86.0f, 0f));

        position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 5.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 81.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 54.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 73.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 83.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 78.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 58.8f, 0f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 86.0f, 0f));
        tmp.add(new Wifi("f8:e9:03:cb:00:a0", 87.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:74", 87.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 87.5f, 0f));

        position = graph.forceWhereAmI(tmp);
        main.draw(position);
        System.out.print("Test 6.\n res = ");
        System.out.println(position.getID());
    }



}
