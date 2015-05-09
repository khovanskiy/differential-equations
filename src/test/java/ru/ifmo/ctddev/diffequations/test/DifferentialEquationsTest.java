package ru.ifmo.ctddev.diffequations.test;

import org.junit.Test;
import ru.ifmo.ctddev.diffequations.DifferentialEquationSystem;
import ru.ifmo.ctddev.diffequations.Function;
import ru.ifmo.ctddev.diffequations.RandomHolder;

import java.util.Random;

public class DifferentialEquationsTest {

    public static void runSimpleTest() {
        Function[] functions = new Function[2];
        functions[0] = new Function() {
            @Override
            public double calculate(double[] x) {
                return x[1];
            }
        };
        functions[1] = new Function() {
            @Override
            public double calculate(double[] x) {
                return -x[0];
            }
        };
        DifferentialEquationSystem differentialEquationSystem = new DifferentialEquationSystem(functions);
        double[] x0 = new double[]{0, 1, 0};
        double[][] answer = differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitEuler,
                x0, 0.001, 3141);
        System.out.println(DifferentialEquationSystem.Method.ExplicitEuler.toString() + ": " + answer[3140][0] + " " + answer[3140][1]);
        answer = differentialEquationSystem.solve(DifferentialEquationSystem.Method.ImplicitEuler, x0, 0.001, 3141);
        System.out.println(DifferentialEquationSystem.Method.ImplicitEuler.toString() + ": " + answer[3140][0] + " " + answer[3140][1]);
        answer = differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitRungeKutta, x0, 0.001, 3141);
        System.out.println(DifferentialEquationSystem.Method.ExplicitRungeKutta.toString() + ": " + answer[3140][0] + " " + answer[3140][1]);
        answer = differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitAdamsBashfort, x0, 0.001, 3141);
        System.out.println(DifferentialEquationSystem.Method.ExplicitAdamsBashfort.toString() + ": " + answer[3140][0] + " " + answer[3140][1]);
    }

    public static void checkRBSigma(final double r, final double b, final double sigma, double dt, int iterations) {
        Random random = RandomHolder.random;
        double[] x0 = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble(), 0};
        Function[] functions = new Function[3];
        functions[0] = new Function() {
            @Override
            public double calculate(double[] x) {
                return sigma * (x[1] - x[0]);
            }
        };
        functions[1] = new Function() {
            @Override
            public double calculate(double[] x) {
                return -x[0] * x[2] + r * x[0] - x[1];
            }
        };
        functions[2] = new Function() {
            @Override
            public double calculate(double[] x) {
                return x[0] * x[1] - b * x[2];
            }
        };
        DifferentialEquationSystem differentialEquationSystem = new DifferentialEquationSystem(functions);
        double[][] result = differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitEuler,
                x0, dt, iterations);
        System.out.println(result[iterations - 2][0] + " " + result[iterations - 2][1] + " " + result[iterations - 2][2]);
        System.out.println(result[iterations - 1][0] + " " + result[iterations - 1][1] + " " + result[iterations - 1][2]);
    }

    public static void getRBSigma(final double r, final double b, final double sigma, double dt, int iterations) {
        Random random = RandomHolder.random;
        double[] x0 = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble(), 0};
        Function[] functions = new Function[3];
        functions[0] = new Function() {
            @Override
            public double calculate(double[] x) {
                return sigma * (x[1] - x[0]);
            }
        };
        functions[1] = new Function() {
            @Override
            public double calculate(double[] x) {
                return -x[0] * x[2] + r * x[0] - x[1];
            }
        };
        functions[2] = new Function() {
            @Override
            public double calculate(double[] x) {
                return x[0] * x[1] - b * x[2];
            }
        };
        DifferentialEquationSystem differentialEquationSystem = new DifferentialEquationSystem(functions);
        differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitEuler,
                x0, dt, iterations);
    }
}
