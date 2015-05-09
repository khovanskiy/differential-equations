package ru.ifmo.ctddev.diffequations;

import java.util.Arrays;

public class DifferentialEquationSystem {
    private static final double EPS = 1e-6;
    private static final long MAX_ITERATIONS = 1000;

    public static enum Method {
        ExplicitEuler {
            @Override
            public String toString() {
                return "Явный метод Эйлера";
            }
        },
        ImplicitEuler {
            @Override
            public String toString() {
                return "Неявный метод Эйлера";
            }
        },
        ExplicitRungeKutta {
            @Override
            public String toString() {
                return "Явный метод Рунге-Кутты 4 порядка";
            }
        },
        ExplicitAdamsBashfort {
            @Override
            public String toString() {
                return "Явный метод Адамса-Бэшфорта 4 порядка";
            }
        },
    }

    private final Function[] functions;
    private final int n;

    /**
     * Creates new differential equations system.
     *
     * @param functions f[i](x(t), t) = dx_i/dt(x(t), t)
     */
    public DifferentialEquationSystem(Function[] functions) {
        this.functions = functions;
        this.n = functions.length;
    }

    /**
     * Calculates the function x(t) using the differential equations
     *
     * @param method     method of solving
     * @param x0         initial state, x0[i] = x0_i, i = 0..n-1, x0[n] = t0
     * @param dt         step
     * @param iterations number of steps
     * @return array of vectors, res[n][i] = x_i(t0 + (n + 1) * dt)
     */
    public double[][] solve(Method method, double[] x0, double dt, int iterations) {
        switch (method) {
            case ExplicitEuler:
                return explicitEulerMethod(x0, dt, iterations);
            case ImplicitEuler:
                return implicitEulerMethod(x0, dt, iterations);
            case ExplicitRungeKutta:
                return explicitRungeKuttaMethod(x0, dt, iterations);
            case ExplicitAdamsBashfort:
                return explicitAdamsMethod(x0, dt, iterations);
            default:
                return null;
        }
    }

    private double[][] explicitEulerMethod(final double[] x0, final double dt, final int iterations) {
        double[][] result = new double[iterations][n];
        double[] x = Arrays.copyOf(x0, x0.length);
        double[] dx = new double[n];
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < n; j++) {
                dx[j] = functions[j].calculate(x) * dt;
            }
            for (int j = 0; j < n; j++) {
                x[j] += dx[j];
                result[i][j] = x[j];
            }
            x[n] += dt;
        }
        return result;
    }

    private double[][] implicitEulerMethod(final double[] x0, final double dt, final int iterations) {
        double[][] result = new double[iterations][n];
        final double[] x = Arrays.copyOf(x0, x0.length);
        double[] dx = new double[n];
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < n; j++) {
                dx[j] = functions[j].calculate(x) * dt;
            }
            Function[] equations = new Function[n];
            for (int jj = 0; jj < n; jj++) {
                final int j = jj;
                equations[j] = new Function() {
                    private final double[] arg = new double[n + 1];

                    @Override
                    public double calculate(double[] dx) {
                        for (int k = 0; k < n; k++) {
                            arg[k] = x[k] + dx[k];
                        }
                        arg[n] = x[n];
                        return dx[j] - functions[j].calculate(arg) * dt;
                    }
                };
            }
            EquationSystem equationSystem = new EquationSystem(equations);
            dx = equationSystem.universalMethod(dx, EPS, MAX_ITERATIONS);
            for (int j = 0; j < n; j++) {
                x[j] += dx[j];
                result[i][j] = x[j];
            }
            x[n] += dt;
        }
        return result;
    }

    private double[][] explicitRungeKuttaMethod(final double[] x0, final double dt, final int iterations) {
        double[][] result = new double[iterations][n];
        double[] x = Arrays.copyOf(x0, x0.length);
        double[][] k = new double[4][n];
        double[] tmp = Arrays.copyOf(x0, x0.length);
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < n; j++) {
                k[0][j] = functions[j].calculate(x);
            }
            for (int j = 0; j < n; j++) {
                tmp[j] = x[j] + k[0][j] * dt / 2;
            }
            tmp[n] = x[n] + dt / 2;
            for (int j = 0; j < n; j++) {
                k[1][j] = functions[j].calculate(tmp);
            }
            for (int j = 0; j < n; j++) {
                tmp[j] = x[j] + k[1][j] * dt / 2;
            }
            tmp[n] = x[n] + dt / 2;
            for (int j = 0; j < n; j++) {
                k[2][j] = functions[j].calculate(tmp);
            }
            for (int j = 0; j < n; j++) {
                tmp[j] = x[j] + k[2][j] * dt;
            }
            tmp[n] = x[n] + dt;
            for (int j = 0; j < n; j++) {
                k[3][j] = functions[j].calculate(tmp);
            }
            for (int j = 0; j < n; j++) {
                x[j] += dt * (k[0][j] + 2 * k[1][j] + 2 * k[2][j] + k[3][j]) / 6;
                result[i][j] = x[j];
            }
            x[n] += dt;
        }
        return result;
    }

    private double[][] explicitAdamsMethod(final double[] x0, final double dt, final int iterations) {
        if (iterations <= 3) {
            return explicitRungeKuttaMethod(x0, dt, iterations);
        } else {
            double[][] result = new double[iterations][n];
            double[][] tmp = new double[4][n + 1];
            tmp[0] = Arrays.copyOf(x0, x0.length);
            double[][] firstRes = explicitRungeKuttaMethod(x0, dt, 3);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < n; j++) {
                    result[i][j] = firstRes[i][j];
                    tmp[i + 1][j] = firstRes[i][j];
                }
                tmp[i + 1][n] = tmp[0][n] + (i + 1) * dt;
            }
            double[] cur = new double[n];
            for (int i = 3; i < iterations; i++) {
                for (int j = 0; j < n; j++) {
                    cur[j] = tmp[3][j] + (functions[j].calculate(tmp[0]) - 5 * functions[j].calculate(tmp[1])
                            + 19 * functions[j].calculate(tmp[2]) + 9 * functions[j].calculate(tmp[3])) * dt / 24;
                }
                for (int q = 0; q < 3; q++) {
                    System.arraycopy(tmp[q + 1], 0, tmp[q], 0, n + 1);
                }
                for (int j = 0; j < n; j++) {
                    tmp[3][j] = cur[j];
                    result[i][j] = cur[j];
                }
                tmp[3][n] += dt;
            }
            return result;
        }
    }
}
