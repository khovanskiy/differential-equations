package ru.ifmo.ctddev.diffequations;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.javafx.JavaFXRenderer3d;
import org.jzy3d.javafx.controllers.JavaFXCameraMouseController;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.Random;

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
public class JavaFXMain extends Application {
    public static void main(String[] args) {
        Application.launch(args);
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

    @Override
    public void start(Stage stage) {
        stage.setTitle("Differential Equations");

        // Jzy3d
        JavaFXChartFactory factory = new JavaFXChartFactory();
        AWTChart chart = getDemoChart(factory, "offscreen");
        ImageView imageView = factory.bindImageView(chart);

        // JavaFX
        VBox pane = new VBox();
        TextField tf = new TextField();
        tf.setText("text");
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        pane.getChildren().add(tf);
        pane.getChildren().add(imageView);

        factory.addSceneSizeChangedListener(chart, scene);

        stage.setWidth(500);
        stage.setHeight(500);
    }

    private AWTChart getDemoChart(JavaFXChartFactory factory, String toolkit) {
        /*// -------------------------------
        // Define a function to plot
        Mapper mapper = new Mapper() {
            public double f(double x, double y) {
                return x * Math.sin(x * y);
            }
        };

        // Define range and precision for the function to plot
        Range range = new Range(-3, 3);
        int steps = 80;

        // Create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(mapper, range, steps);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        // -------------------------------
        // Create a chart
        Quality quality = Quality.Advanced;
        quality.setSmoothPolygon(true);
        //quality.setAnimated(true);

        // let factory bind mouse and keyboard controllers to JavaFX node
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        chart.getScene().getGraph().add(surface);
        return chart;*/
        int size = 100000;
        double[][] v = solveSystem(10, 8.0f / 3.0f, 10, 0.0001, size);
        Coord3d[] points = new Coord3d[size];
        for (int i = 0; i < v.length; ++i) {
            points[i] = new Coord3d(v[i][0], v[i][1], v[i][2]);
        }

        Scatter scatter = new Scatter(points);

        AWTChart chart = (AWTChart) factory.newChart(Quality.Advanced, toolkit);
        chart.getScene().add(scatter);
        return chart;
    }
}