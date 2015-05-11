package ru.ifmo.ctddev.diffequations;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.javafx.JavaFXRenderer3d;
import org.jzy3d.javafx.controllers.JavaFXCameraMouseController;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Showing how to pipe an offscreen Jzy3d chart image to a JavaFX ImageView.
 * <p>
 * {@link JavaFXChartFactory} delivers dedicated  {@link JavaFXCameraMouseController}
 * and {@link JavaFXRenderer3d}
 * <p>
 * Support
 * Rotation control with left mouse button hold+drag
 * Scaling scene using mouse wheel
 * Animation (camera rotation with thread)
 *
 * @author victor
 */
public class GUIMain extends Application {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final JavaFXChartFactory factory = new JavaFXChartFactory();
    private final Pane canvas = new Pane();
    private final VBox root = new VBox();
    private final Scene scene = new Scene(root);
    private final ConsoleTask consoleTask = new ConsoleTask();
    private int iterations = 100000;
    private int points = 10000;
    private double r = 24;
    private double b = 8.0 / 3.0;
    private double sigma = 10;
    private double dt = 1e-3;
    private DifferentialEquationSystem.Method method = DifferentialEquationSystem.Method.ExplicitEuler;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static double[][] solveSystem(DifferentialEquationSystem.Method method,
            final double r, final double b, final double sigma, double dt, int iterations) {
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
        return differentialEquationSystem.solve(method, x0, dt, iterations);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Differential Equations: Lorenz system");
        stage.setWidth(640);
        stage.setHeight(480);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                consoleTask.cancel();
                Platform.exit();
                System.exit(0);
            }
        });
        stage.setScene(scene);

        root.getChildren().add(canvas);
        update();

        service.submit(consoleTask);

        stage.show();
    }

    private void update() {
        if (this.points > this.iterations) {
            this.points = this.iterations;
        }
        System.out.println("Solving...");
        double[][] v = solveSystem(method, r, b, sigma, dt, iterations);
        Coord3d[] points = new Coord3d[this.points];
        int step = iterations / this.points;
        for (int i = 0, j = 0; i < points.length && j < v.length; ++i, j += step) {
            points[i] = new Coord3d(v[j][0], v[j][1], v[j][2]);
        }
        System.out.println("Rebuilding...");

        Scatter scatter = new Scatter(points, Color.RED);

        AWTChart chart = (AWTChart) factory.newChart(Quality.Nicest, IChartComponentFactory.Toolkit.offscreen);
        chart.getScene().add(scatter);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = factory.bindImageView(chart);
                factory.addSceneSizeChangedListener(chart, scene);

                canvas.getChildren().clear();
                canvas.getChildren().add(imageView);

                System.out.println("Graph has been updated.");
            }
        });
    }

    private void handleCommand(String command) {
        System.out.println("Command: " + command);
        String[] args = command.split(" ");
        switch (args[0]) {
            case "set": {
                switch (args[1].toLowerCase()) {
                    case "iterations":
                    case "i":
                    case "iter":
                    case "it": {
                        iterations = Math.abs(Integer.parseInt(args[2]));
                    }
                    break;
                    case "points":
                    case "pts":
                    case "p": {
                        points = Math.abs(Integer.parseInt(args[2]));
                    }
                    break;
                    case "b": {
                        b = Double.parseDouble(args[2]);
                    }
                    break;
                    case "r": {
                        r = Double.parseDouble(args[2]);
                    }
                    break;
                    case "sigma":
                    case "s": {
                        r = Double.parseDouble(args[2]);
                    }
                    break;
                    case "t":
                    case "dt": {
                        dt = Double.parseDouble(args[2]);
                    }
                    break;
                    case "method": {
                        switch (args[2].toLowerCase()) {
                            case "expliciteuler":
                            case "explicit_euler": {
                                method = DifferentialEquationSystem.Method.ExplicitEuler;
                            }
                            break;
                            case "impliciteuler":
                            case "implicit_euler": {
                                method = DifferentialEquationSystem.Method.ImplicitEuler;
                            }
                            break;
                            case "explicitrungekutta":
                            case "explicit_runge_kutta": {
                                method = DifferentialEquationSystem.Method.ExplicitRungeKutta;
                            }
                            break;
                            case "explicitadamsbashfort":
                            case "explicit_adams_bashfort": {
                                method = DifferentialEquationSystem.Method.ExplicitAdamsBashfort;
                            }
                            break;
                        }
                    }
                    break;
                }
                update();
            }
            break;
            case "verbose":
            case "info":
            case "v": {
                System.out.println("R = " + r);
                System.out.println("Sigma = " + sigma);
                System.out.println("dt = " + dt);
                System.out.println("Iterations = " + iterations);
                System.out.println("Points = " + points);
                System.out.println("Method = " + method.toString());
            }
            break;
            case "render":
            case "update":
            case "u": {
                update();
            }
            break;
        }
    }

    private class ConsoleTask implements Runnable {

        private volatile boolean isCanceled = false;

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (!isCanceled) {
                    if (reader.ready()) {
                        handleCommand(reader.readLine());
                    }
                    Thread.sleep(1000);
                }
                System.out.println("End of task");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            isCanceled = true;
        }

        public boolean isCanceled() {
            return this.isCanceled;
        }
    }
}