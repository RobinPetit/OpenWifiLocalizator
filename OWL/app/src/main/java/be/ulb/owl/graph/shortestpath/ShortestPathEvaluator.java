package be.ulb.owl.graph.shortestpath;

import java.util.ArrayList;

import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

/**
 * Created by robin on 10/12/16.
 */

public abstract class ShortestPathEvaluator {
    protected Node _src;
    protected Node _dest;

    protected boolean _found;
    protected boolean _executed;

    public ShortestPathEvaluator(Node from, Node to) {
        _src = from;
        _dest = to;
        _found = _executed = false;
    }

    protected abstract void lookup();

    /**
     *
     * @return A list of path representing where the user needs to reach the given points
     * @throws NoPathException
     */
    public ArrayList<Path> find() throws NoPathException {
        if(!_executed)
            lookup();
        if(!_found)
            throw new NoPathException("Unable to find a path between nodes");
        return null;
    }
}
