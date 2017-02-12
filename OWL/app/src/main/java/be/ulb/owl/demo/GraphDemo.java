package be.ulb.owl.demo;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.Campus;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.scanner.Wifi;

/**
 * Make a demonstration (override some method)
 *
 * @author Detobel36
 */

public class GraphDemo extends Graph {


    private static int _offset;
    private static ArrayList<Node> _demoMotions = new ArrayList<Node>();


    public GraphDemo() {
        super();
        _offset = 0;
    }


    /**
     * Add manually point to a plan
     *
     */
    private void setPlan () {
        if (0 == _offset) {
            Campus campus = getCampus("Solbosh");
            Plan currentPlan = campus.getPlan("P.F");
            _demoMotions.add(currentPlan.getNode(64));
            _demoMotions.add(currentPlan.getNode(63));
            _demoMotions.add(currentPlan.getNode(9));
            _demoMotions.add(currentPlan.getNode(13));
            _demoMotions.add(currentPlan.getNode(20));
            _demoMotions.add(currentPlan.getNode(21));
            _demoMotions.add(currentPlan.getNode(27));
            _demoMotions.add(currentPlan.getNode(29));
            _demoMotions.add(currentPlan.getNode(31));
            _demoMotions.add(currentPlan.getNode(32));
            _demoMotions.add(currentPlan.getNode(34));
            _demoMotions.add(currentPlan.getNode(35));
            _demoMotions.add(currentPlan.getNode(23));
            _demoMotions.add(currentPlan.getNode(16));
            _demoMotions.add(currentPlan.getNode(42));
        }
    }


    @Override
    protected Node whereAmI(ArrayList<Wifi> sensed) {
        Node res = null;
        if(MainActivity.isDemo()) {
            setPlan();
            res = _demoMotions.get(_offset);
            _offset = (_offset + 1) % _demoMotions.size();
        }

        return res;
    }

}
