package be.ulb.owl.gui;

/**
 * Created by robin on 15/02/17.
 */

public final class FloatCouple {
    private Float _x;
    private Float _y;

    public FloatCouple(Float x, Float y) {
        _x = x;
        _y = y;
    }

    public Float getX() {
        return _x;
    }

    public Float getY() {
        return _y;
    }

    public FloatCouple add(final FloatCouple other) {
        return new FloatCouple(_x + other.getX(), _y + other.getY());
    }

    public FloatCouple subtract(final FloatCouple other) {
        return new FloatCouple(_x - other.getX(), _y - other.getY());
    }

    public FloatCouple multiply(float factor) {
        return new FloatCouple(_x*factor, _y*factor);
    }

    @Override
    public String toString() {
        return "(" + _x + ", " + _y + ")";
    }
}
