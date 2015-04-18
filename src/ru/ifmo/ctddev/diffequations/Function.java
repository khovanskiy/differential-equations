package ru.ifmo.ctddev.diffequations;

import java.util.Arrays;

public abstract class Function {
    private static final double EPS = 1e-6;

    public abstract double calculate(double[] x);

    public double[] totalDerivative(double[] x) {
        double[] xn = Arrays.copyOf(x, x.length);
        double y = calculate(x);
        double[] res = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            xn[i] += EPS;
            res[i] = (calculate(xn) - y) / EPS;
            xn[i] = x[i];
        }
        return res;
    }
}
