package be.ulb.owl.test;

import java.util.ArrayList;
import java.util.Random;

import be.ulb.owl.MainActivity;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;

/**
 * Created by Detobel36
 */

public class GraphTest extends Graph {

    public GraphTest(MainActivity main) {
        super(main);
    }

    @Override
    protected Node whereAmI(ArrayList<Wifi> sensed) {
        return getAllNodes().get(new Random().nextInt(getAllNodes().size()));
    }


    /**
     * Allow whereAmI only for test
     *
     * @param sensed all wifi arround the user (artificial created)
     * @return the position where the user is
     * @see Graph#whereAmI(ArrayList)
     */
    protected Node forceWhereAmI(ArrayList<Wifi> sensed) {
        return super.whereAmI(sensed);
    }



}
