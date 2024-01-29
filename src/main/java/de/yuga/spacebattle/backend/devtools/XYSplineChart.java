package de.yuga.spacebattle.backend.devtools;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Map;

/**
 * An example of a time series chart create using JFreeChart.  For the most
 * part, default settings are used, except that the renderer is modified to
 * show filled shapes (as well as lines) at each data point.
 */
public class XYSplineChart extends ApplicationFrame {

    private static final long serialVersionUID = 1L;

    /**
     * A demonstration application showing how to create a simple time series
     * chart.  This example uses monthly data.
     *
     * @param title the frame title.
     */
    public XYSplineChart(@Nonnull final String title) {
        super(title);

    }

    public void run(@Nonnull final Orbit planetaryOrbit, @Nonnull final Map<Owner, CubicBezier> curves) {
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");
        Preconditions.checkNotNull(curves, "curves must not be empty");

        XYSeries series;
        XYSeriesCollection dataset = new XYSeriesCollection();

        final XYSeries planet = new XYSeries("Planet");
        planet.add(
                planetaryOrbit.getXCoordinate().getCoordinate().intValue(),
                planetaryOrbit.getYCoordinate().getCoordinate().intValue()
        );
        dataset.addSeries(planet);

        for (final Owner owner : curves.keySet()) {
            final CubicBezier bezier = curves.get(owner);
            series = new XYSeries(owner.getUsername());

            final double length = bezier.getLength();
            for (double i = 0; i <= 1; ) {
                double j = i + 0.01;
                final double[] start = bezier.getPointAtLength(length * j);
                final double[] end = bezier.getPointAtLength(length * j);
                series.add(start[0], start[1]);
                i = j;
            }
            dataset.addSeries(series);
        }

        final ChartPanel chartPanel = createPanel(dataset);
        chartPanel.setPreferredSize(new Dimension(500, 500));
        setContentPane(chartPanel);
    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     * @return A chart.
     */
    @Nonnull
    private static JFreeChart createChart(@Nonnull final XYDataset dataset) {
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final NumberAxis domain = new NumberAxis("X");
        final NumberAxis range = new NumberAxis("Y");
        final XYSplineRenderer r = new XYSplineRenderer(1);
        final XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        return new JFreeChart(xyplot);
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    @Nonnull
    public static ChartPanel createPanel(@Nonnull final XYDataset dataset) {
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final JFreeChart chart = createChart(dataset);
        final ChartPanel panel = new ChartPanel(chart, false);

        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
}
