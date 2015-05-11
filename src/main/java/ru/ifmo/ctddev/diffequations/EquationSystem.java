package ru.ifmo.ctddev.diffequations;

import java.util.Arrays;

public class EquationSystem {

    private final static double GRADIENT_DESCENT_PRECISION = 1e-6;
    private Function[] functions;
    private int n;

    /**
     * Creates new equation system.
     *
     * @param functions functions that return 0 on the answer. Number of functions must be equal to the functions arity.
     */
    public EquationSystem(Function[] functions) {
        this.functions = functions;
        n = functions.length;
    }

    /**
     * Finds infinity norm of vector x
     *
     * @param x vector
     * @return ||x||{Inf}
     */
    public static double getNorm(double[] x) {
        double norm = Math.abs(x[0]);
        for (int i = 1; i < x.length; i++) {
            if (Math.abs(x[i]) > norm) {
                norm = Math.abs(x[i]);
            }
        }
        return norm;
    }

    /**
     * Finds argument of rough function minimum
     *
     * @param f           unary function
     * @param x0          initial argument
     * @param initialStep initial dx
     * @param precision   minimum dx
     * @return x| f(x) is rough minimum
     */
    public static double gradientDescent(Function f, double x0, double initialStep, double precision) {
        double[] arg = new double[1];
        arg[0] = x0;
        double derivative = f.totalDerivative(arg)[0];
        double step = initialStep, x = x0, x1, min = f.calculate(arg);
        while (step > precision) {
            if (derivative < 0) {
                x1 = x + step;
            } else {
                x1 = x - step;
            }
            arg[0] = x1;
            double cur = f.calculate(arg);
            if (cur < min) {
                min = cur;
                x = x1;
                arg[0] = x;
                derivative = f.totalDerivative(arg)[0];
            } else {
                step /= 2;
            }
        }
        return x;
    }

    /**
     * @param x argument
     * @return discrepancy = sum fi(x)*fi(x), i = 0..n-1
     */
    public double discrepancy(double[] x) {
        double discrepancy = 0, q;
        for (int i = 0; i < n; i++) {
            q = functions[i].calculate(x);
            discrepancy += q * q;
        }
        return discrepancy;
    }

    /**
     * @param x0 x0
     * @param d  d
     * @param t  t
     * @return {@link ru.ifmo.ctddev.diffequations.EquationSystem#discrepancy}(x0 + t * d)
     */
    public double discrepancy(double[] x0, double[] d, double t) {
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = x0[i] + t * d[i];
        }
        return discrepancy(x);
    }

    /**
     * Finds solution of equation F'dx+Fx=0, where F is matrix of fi
     *
     * @param x initial function argument
     * @return dx
     */
    public double[] linearDerivativeSolution(double[] x) {
        double[] b = new double[n];
        double[][] matrix = new double[n][];
        for (int i = 0; i < n; i++) {
            b[i] = -functions[i].calculate(x);
            matrix[i] = functions[i].totalDerivative(x);
        }
        Matrix m = new Matrix(matrix);
        return m.gaussMethod(b);
    }

    /**
     * Finds local discrepancy minimum on line x(t) = x + t * direction
     *
     * @param x initial argument
     * @param d line direction
     * @return t| x(t) = x + t * direction is local discrepancy minimum
     */
    public double localMinimum(final double[] x, final double[] d) {
        double cur = discrepancy(x);
        cur = Math.min(cur, discrepancy(x, d, 1));
        double r = 1, dr;
        do {
            r *= 2;
            dr = discrepancy(x, d, r);
            cur = Math.min(cur, dr);
        } while (dr <= cur);
        Function f = new Function() {
            @Override
            public double calculate(double[] arg) {
                return discrepancy(x, d, arg[0]);
            }
        };
        return gradientDescent(f, 1, 0.5, GRADIENT_DESCENT_PRECISION);
    }

    /**
     * @param eps           precision of finding x
     * @param maxIterations maximum iterations count
     * @return argument x, discrepancy(x) < eps
     */
    public double[] universalMethod(double[] x0, double eps, long maxIterations) {
        double[] x = Arrays.copyOf(x0, x0.length);
        for (int q = 0; q < maxIterations; q++) {
            double[] dx = linearDerivativeSolution(x);
            double k = localMinimum(x, dx);
            for (int i = 0; i < n; i++) {
                x[i] += k * dx[i];
            }
            if (getNorm(dx) < eps) break;
        }
        return x;
    }
}
