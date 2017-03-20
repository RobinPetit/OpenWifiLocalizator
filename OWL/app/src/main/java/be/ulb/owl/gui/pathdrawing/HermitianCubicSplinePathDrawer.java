package be.ulb.owl.gui.pathdrawing;

import android.graphics.Canvas;

import java.util.List;

import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 19/02/17.
 */

public class HermitianCubicSplinePathDrawer extends PathDrawer {
    /**
     * cubic curves are not drawn directly by a canvas function. Then the function must
     * be evaluated in a given amount of points to be displayed in order to mimic such a curve.
     */
    static private int NB_POINTS_PER_SEGMENT = 10;

    /**
     * dimension of the interpolation curve (here 3 since curves are cubic)
     */
    static private int DIMENSION = 3;

    /**
     * value to divide the derivatives by in order to add the required  constraint
     * to determine the 4 parameters of each "sub-curve".
     *
     * See Hermite splines for more information about this.
     */
    static private float DERIVATIVE_REGULARISATION_FACTOR = 2.f;

    /**
     * Array of coefficients for each monomial \alpha_{ik} * t^k and for each sub-curve
     * joining two points to interpolate
     */
    private FloatCouple[][] _alpha;

    /**
     * array of derivative values at each point. Derivative is given by a weighted average
     * of next and previous node. Two additional constraints must be set for gamma[0] and gamma[-1]
     */
    private FloatCouple[] _gamma;

    /**
     * The number of points (knots) to interpolate
     */
    private int _N = 0;

    /**
     * Constructor
     * @param pathColor color to draw the path with
     * @param canvas canvas to draw the path on
     */
    public HermitianCubicSplinePathDrawer(int pathColor, Canvas canvas) {
        super(pathColor, canvas);
    }

    /**
     * draws a path interpolating all of the given points in the correct order
     * by piece-wise cubic curves
     * @param points list of points to interpolate
     */
    @Override
    public void drawPath(List<FloatCouple> points) {
        // super.drawPath(points);
        int N = points.size();
        setNumberOfPoints(N);
        _alpha = new FloatCouple[N][DIMENSION+1];
        _gamma = new FloatCouple[N];
        computeDerivatives(points);  // get gamma
        computeCoefficients(points);  // get alpha
        drawPath();
    }

    /**
     * Draws the path when all the coefficients have benn computed
     */
    private void drawPath() {
        int N = getNumberOfPoints();
        // for each sub curve
        for(int i = 0; i < N-1; ++i) {
            float prevX = evaluateX(i, 0.f);
            float prevY = evaluateY(i, 0.f);
            // mimic a cubic curve by drawing several points of the cubic curve
            for(int step = 1; step < NB_POINTS_PER_SEGMENT+1; ++step) {
                float x = evaluateX(i, step/(float)NB_POINTS_PER_SEGMENT);
                float y = evaluateY(i, step/(float)NB_POINTS_PER_SEGMENT);
                _canvas.drawLine(prevX, prevY, x, y, _pathColor);
                prevX = x;
                prevY = y;
                //_canvas.drawPoint(x, y, _pathColor);
            }
        }
    }

    /**
     * @link _N
     * @param N the new amount of nodes that must be interpolated
     */
    private void setNumberOfPoints(int N) {
        _N = N;
    }

    /**
     * get the amount of nodes to interpolate
     * @return The number of knots
     */
    protected int getNumberOfPoints() {
        return _N;
    }

    /**
     *
     * @param points
     */
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

    /**
     * Compute the constraints on the derivatives (for cubic hermitian splines, this constraint
     * is given by \gamma_i = (p_{i+1} - p_{i-1})/R, with R the regularisation factor and p_i = (x_i, y_i)
     * the ith point to interpolate)
     * @param points The list of knots to interpolate
     */
    private void computeDerivatives(List<FloatCouple> points) {
        setExtremaDerivatives(points);
        int N = getNumberOfPoints();
        for(int i = 1; i < N-1; ++i) {
            FloatCouple nextPoint = points.get(i+1);
            FloatCouple prevPoint = points.get(i-1);
            _gamma[i] = nextPoint.subtract(prevPoint).multiply(1/DERIVATIVE_REGULARISATION_FACTOR);
        }
    }

    /**
     * pretty arbitrary additional constraint on \gamma_0 and gamma_{N-1}
     * @param points The list of points to interpolate
     */
    protected void setExtremaDerivatives(List<FloatCouple> points) {
        int N = getNumberOfPoints();
        // here, set the first derivative of the curve to be null at both extremities
        // (theoretically, any value could fit here)
        _gamma[0] = new FloatCouple(.0f, .0f);
        _gamma[N-1] = new FloatCouple(.0f, .0f);
    }

    /**
     * evaluates the x-value of the interpolation function on given segment at given value
     * @param idx The index representing the segment to evaluate the interpolation on
     * @param t The value to evaluate at (\in [0, 1])
     * @return The value of the interpolating function on segment idx at value t
     */
    private float evaluateX(int idx, float t) {
        float[] xCoeff = new float[_alpha[idx].length];
        for(int i = 0; i < xCoeff.length; ++i) {
            xCoeff[i] = _alpha[idx][i].getX();
        }
        return evaluatePolynomial(xCoeff, t);
    }

    /**
     * evaluates the y-value of the interpolation function on given segment at given value
     * @param idx The index representing the segment to evaluate the interpolation on
     * @param t The value to evaluate at (\in [0, 1])
     * @return The value of the interpolating function on segment idx at value t
     */
    private float evaluateY(int idx, float t) {
        float[] yCoeff = new float[_alpha[idx].length];
        for(int i = 0; i < yCoeff.length; ++i) {
            yCoeff[i] = _alpha[idx][i].getY();
        }
        return evaluatePolynomial(yCoeff, t);
    }

    /**
     * Evaluates a generic polynomial determined by its coefficients at given value
     * @param coeff Array of coefficients such that coeff[i] is the coefficient of t^i
     * @param t the real value to evaluate the polynomial at
     * @return The value taken by the polynomial at value t
     */
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
