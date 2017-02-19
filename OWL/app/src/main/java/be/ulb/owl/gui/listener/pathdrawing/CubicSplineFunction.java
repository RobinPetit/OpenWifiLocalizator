package be.ulb.owl.gui.listener.pathdrawing;

/**
 * Created by robin on 16/02/17.
 */

public class CubicSplineFunction {
    private float _a, _b, _c, _d;
    private float _origin;
    private float _begDomain;
    private float _endDomain;

    /**
     * Creates a cubic spline function in form a*(x-o)^3 + b*(x-o)^2 + c*(x-o) + d
     * where o is the "origin"
     * @param a coefficient of the cubic monomial
     * @param b coefficient of the quadratic monomial
     * @param c coefficient of the linear monomial
     * @param d independent term
     * @param begDomain infimum of connected compact domain
     * @param endDomain supremum of connected compact domain
     */
    public CubicSplineFunction(float a, float b, float c, float d, float begDomain, float endDomain) {
        assert begDomain < endDomain;
        _a = a;
        _b = b;
        _c = c;
        _d = d;
        _origin = _begDomain = begDomain;
        _endDomain = endDomain;
    }

    /**
     * evaluates the function in x
     * @param x the value to evaluate the function in
     * @return the value of the function at given point x
     */
    public float evaluate(float x) {
        float ret = _d;
        x -= _origin;
        float xMinusOrigin = x;
        ret += _c * xMinusOrigin;  // c*(x-o) + d
        xMinusOrigin *= x;
        ret += _b * xMinusOrigin;  // b*(c-o)^2 + c*(x-o) + d
        xMinusOrigin *= x;
        ret += _a * xMinusOrigin;  // a*(c-o)^3 + b*(x-o)^2 + c*(x-o) + d
        return ret;
    }

    /**
     * Returns a list of equidistant strictly increasing values inside the interval domain
     * @param points the number of requested points in the interval
     * @return a list of `points` values
     */
    public float[] domainSeparation(int points) {
        assert points > 1;
        float[] ret = new float[points];
        float stepLength = (_endDomain-_begDomain) / (points-1);
        float tmp = _begDomain;
        for(int i = 0; i < points; ++i) {
            ret[i] = tmp;
            tmp += stepLength;
        }
        return ret;
    }
}
