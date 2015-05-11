package ru.ifmo.ctddev.diffequations;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.Random;

public class AWTMain extends AbstractAnalysis {

    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new AWTMain());
    }

    @Override
    public void init() {
        int size = 100000;
        double[][] v = solveSystem(10, 8.0f / 3.0f, 10, 0.0001, size);
        Coord3d[] points = new Coord3d[size];
        for (int i = 0; i < v.length; ++i) {
            points[i] = new Coord3d(v[i][0], v[i][1], v[i][2]);
        }

        Scatter scatter = new Scatter(points);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().add(scatter);
    }

    public static double[][] solveSystem(final double r, final double b, final double sigma, double dt, int iterations) {
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
        return differentialEquationSystem.solve(DifferentialEquationSystem.Method.ExplicitRungeKutta,
                x0, dt, iterations);
    }
}