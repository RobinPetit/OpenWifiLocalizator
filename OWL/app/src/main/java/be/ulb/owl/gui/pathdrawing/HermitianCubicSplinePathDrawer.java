package be.ulb.owl.gui.pathdrawing;

import android.graphics.Canvas;

import java.util.List;

import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 19/02/17.
 */

public class HermitianCubicSplinePathDrawer extends PathDrawer {
    static private int NB_POINTS_PER_SEGMENT = 50;
    static private int DIMENSION = 3;
    static private float DERIVATIVE_REGULARISATION_FACTOR = 2.f;
    private FloatCouple[][] _alpha;
    private FloatCouple[] _gamma;
    private int _N = 0;

    public HermitianCubicSplinePathDrawer(int pathColor, Canvas canvas) {
        super(pathColor, canvas);
    }

    @Override
    public void drawPath(Float x1, Float y1, Float x3, Float y2) {
        // @TODO
    }

    @Override
    public void drawPath(List<FloatCouple> points) {
        super.drawPath(points);
        int N = points.size();
        setNumberOfPoints(N);
        _alpha = new FloatCouple[N][DIMENSION+1];
        _gamma = new FloatCouple[N];
        computeDerivatives(points);
        computeCoefficients(points);
        drawPath();
    }

    private void drawPath() {
        int N = getNumberOfPoints();
        for(int i = 0; i < N-1; ++i) {
            for(int step = 0; step < NB_POINTS_PER_SEGMENT+1; ++step) {
                float x = evaluateX(i, step/(float)NB_POINTS_PER_SEGMENT);
                float y = evaluateY(i, step/(float)NB_POINTS_PER_SEGMENT);
                _canvas.drawPoint(x, y, _pathColor);
            }
        }
    }

    private void setNumberOfPoints(int N) {
        _N = N;
    }

    protected int getNumberOfPoints() {
        return _N;
    }

    private void computeCoefficients(List<FloatCouple> points) {
        int N = getNumberOfPoints();
        for(int i = 0; i < N-1; ++i) {
            FloatCouple point = points.get(i);
            _alpha[i][0] = new FloatCouple(point.getX(), point.getY());
            _alpha[i][1] = _gamma[i];
            // \alpha_{i2} = 3*(x_{i+1}-x_i) - (\gamma_{i+1} + 2*\gamma_i)
            _alpha[i][2] = points.get(i+1).subtract(point).multiply(3).subtract(_gamma[i].multiply(2).add(_gamma[i+1]));
            // \alpha_{i3} = \gamma_i + \gamma_{i+1} + 2*(x_i - x_{i-1})
            _alpha[i][3] = _gamma[i].add(_gamma[i+1]).add(point.subtract(points.get(i+1)).multiply(2));
        }
    }

    private void computeDerivatives(List<FloatCouple> points) {
        setExtremaDerivatives(points);
        int N = getNumberOfPoints();
        for(int i = 1; i < N-1; ++i) {
            FloatCouple nextPoint = points.get(i+1);
            FloatCouple prevPoint = points.get(i-1);
            _gamma[i] = nextPoint.subtract(prevPoint).multiply(1/DERIVATIVE_REGULARISATION_FACTOR);
        }
    }

    protected void setExtremaDerivatives(List<FloatCouple> points) {
        int N = getNumberOfPoints();
        _gamma[0] = new FloatCouple(.0f, .0f);
        _gamma[N-1] = new FloatCouple(.0f, .0f);
    }

    private float evaluateX(int idx, float t) {
        float[] xCoeff = new float[_alpha[idx].length];
        for(int i = 0; i < xCoeff.length; ++i) {
            xCoeff[i] = _alpha[idx][i].getX();
        }
        return evaluatePolynomial(xCoeff, t);
    }

    private float evaluateY(int idx, float t) {
        float[] yCoeff = new float[_alpha[idx].length];
        for(int i = 0; i < yCoeff.length; ++i) {
            yCoeff[i] = _alpha[idx][i].getY();
        }
        return evaluatePolynomial(yCoeff, t);
    }

    static private float evaluatePolynomial(final float[] coeff, float t) {
        float tPowered = 1;
        float ret = 0;
        for(float coefficient : coeff) {
            ret += tPowered * coefficient;
            tPowered *= t;
        }
        return ret;
    }
}
