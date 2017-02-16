package be.ulb.owl.gui.listener.pathdrawing;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 15/02/17.
 */

public class CubicNaturalSplinePathDrawer extends PathDrawer {
    static private int NB_POINTS_IN_CURVE = 10;
    private List<FloatCouple> _points;
    private float[] _lengths;  // length of each sub-interval
    private int _n;  // size of _points
    private float[] _slopes;  // sort of the temporary slope

    public CubicNaturalSplinePathDrawer(int pathColor, Canvas canvas) {
        super(pathColor, canvas);
    }

    public CubicNaturalSplinePathDrawer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void drawPath(Float x1, Float y1, Float x3, Float y2) {
        // @TODO
    }

    @Override
    public void drawPath(List<FloatCouple> points) {
        _points = points;
        computeFunctions();
    }

    private void computeLengths() {
        _lengths = new float[_n-1];
        for (int i = 0; i < _n - 1; ++i)
            _lengths[i] = getXDifferenceBetweenIndices(i+1, i);
    }

    private void computeSlopes() {
        int slopeSize = _n-2;
        _slopes = new float[slopeSize];
        for (int i = 0; i < slopeSize; ++i) {
            float deltaY1 = getYDifferenceBetweenIndices(i+2, i+1);
            float deltaY2 = getYDifferenceBetweenIndices(i+1, i);
            _slopes[i] = 3 * (deltaY1 / _lengths[i + 1] - deltaY2 / _lengths[i]);
        }
    }

    private void computeFunctions() {
        _n = _points.size();
        computeLengths();
        computeSlopes();
        // those arrays are defaulted to zero by java
        float[] mu = new float[_n - 1];
        float[] z = new float[_n - 1];  // acts as second derivatives of pseudo-function f having yielded the y_i's
        float[] cubicComponent = new float[_n + 1];
        float[] quadraticComponent = new float[_n + 1];
        float[] linearComponent = new float[_n + 1];
        // no need to declare the independent component since it is points[i].getY()

        float divisor;
        for (int i = 1; i <= _n - 2; ++i) {
            divisor = 2*getXDifferenceBetweenIndices(i+1, i-1) - _lengths[i - 1] * mu[i - 1];
            mu[i] = _lengths[i] / divisor;
            z[i] = (_slopes[i - 1] - _lengths[i - 1] * z[i - 1]) / divisor;
        }
        z[_n - 2] = 0;

        boolean firstLoop = true;
        for (int i = _n - 1; i >= 0; --i) {
            float h_i = _lengths[i];
            float c_j = firstLoop ? quadraticComponent[i + 1] : 0;
            float deltaY = getYDifferenceBetweenIndices(i+1, i);

            quadraticComponent[i] = z[i] - mu[i] * c_j;
            linearComponent[i] = deltaY / h_i - h_i * (c_j + 2*quadraticComponent[i]) / 3;
            cubicComponent[i] = (c_j - quadraticComponent[i]) / (3*_lengths[i]);

            if (firstLoop)
                firstLoop = false;
        }
        ArrayList<CubicSplineFunction> splines = getSplinesFromCoefficients(
                cubicComponent,
                quadraticComponent,
                linearComponent
        );
        drawPoints(splines);
    }

    /**
     * Creates the splines objects used to draw the path
     * @param cubic an array of coefficients for the cubic terms
     * @param quadratic an array of coefficients for the quadratic terms
     * @param linear n array of coefficients for the linear terms
     * @return A list of CubicSplineFunction objects
     */
    private ArrayList<CubicSplineFunction> getSplinesFromCoefficients(float[] cubic, float[] quadratic, float[] linear) {
        ArrayList<CubicSplineFunction> splines = new ArrayList<>();
        for(int i = 0; i < _n-1; ++i) {
            splines.add(new CubicSplineFunction(
                    cubic[i],
                    quadratic[i],
                    linear[i],
                    _points.get(i).getY(),
                    _points.get(i).getX(),
                    _points.get(i+1).getX()
            ));
        }
        return splines;
    }

    /**
     * Returns the difference between two y-values at given indices
     * @param i first index to select
     * @param j second index to select
     * @return y_i - y_j
     */
     private float getYDifferenceBetweenIndices(int i, int j) {
        return _points.get(i).getY() - _points.get(j).getY();
    }

    /**
     * Returns the difference between two x-values at given indices
     * @param i first index to select
     * @param j second index to select
     * @return x_i - x_j
     */
    private float getXDifferenceBetweenIndices(int i, int j) {
        return _points.get(i).getX() - _points.get(j).getX();
    }

    private void drawPoints(List<CubicSplineFunction> splines) {
        for(CubicSplineFunction spline : splines) {
            float[] pointsList = spline.domainSeparation(NB_POINTS_IN_CURVE);
            for(float point : pointsList) {
                _canvas.drawPoint(point, spline.evaluate(point), _pathColor);
            }
        }
    }
}
