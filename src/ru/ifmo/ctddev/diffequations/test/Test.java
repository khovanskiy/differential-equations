package ru.ifmo.ctddev.diffequations.test;

import ru.ifmo.ctddev.diffequations.DifferentialEquationSystem;
import ru.ifmo.ctddev.diffequations.Function;

public class Test {

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
}
