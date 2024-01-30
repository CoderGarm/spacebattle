package de.yuga.spacebattle.backend.devtools;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYItemLabelGenerator;
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
 * <p>
 * todo -Djava.awt.headless=false needed
 */
public class XYSplineChart extends ApplicationFrame /* todo JFrame wenn listener gefunden -> headless? */ {

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

    @Nonnull
    public ChartPanel run(@Nonnull final Orbit planetaryOrbit, @Nonnull final Map<Owner, CubicBezier> curves) {
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");
        Preconditions.checkNotNull(curves, "curves must not be empty");

        XYSeries series;
        XYSeriesCollection dataset = new XYSeriesCollection();

        addPlanetaryOrbit(planetaryOrbit, dataset);


        for (final Owner owner : curves.keySet()) {
            final CubicBezier bezier = curves.get(owner);

            final String username = owner.getUsername();
            setControlPoints(username, bezier, dataset);

            series = new XYSeries(username);

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
        return chartPanel;
    }

    private static void setControlPoints(@Nonnull final String title,
                                         @Nonnull final CubicBezier bezier,
                                         @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(title, "title must not be empty");
        Preconditions.checkNotNull(bezier, "bezier must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");


        final XYSeries planet = new XYSeries(title + " controls");
        planet.add(bezier.getP1()[0], bezier.getP1()[1]);
        planet.add(bezier.getCp1()[0], bezier.getCp1()[1]);
        planet.add(bezier.getCp2()[0], bezier.getCp2()[1]);
        planet.add(bezier.getP2()[0], bezier.getP2()[1]);
        dataset.addSeries(planet);
    }


    private static void addPlanetaryOrbit(@Nonnull final Orbit planetaryOrbit,
                                          @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final XYSeries planet = new XYSeries("Planet");
        planet.add(
                planetaryOrbit.getXCoordinate().getCoordinate().intValue(),
                planetaryOrbit.getYCoordinate().getCoordinate().intValue()
        );
        dataset.addSeries(planet);
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
        final XYSplineRenderer renderer = new XYSplineRenderer(1);

        final XYItemLabelGenerator xyItemLabelGenerator = new XYItemLabelGenerator() {
            @Override
            public String generateLabel(final XYDataset dataset, final int series, final int itemIndex) {
                switch (itemIndex) {
                    case 0:
                        return "P1";
                    case 1:
                        return "Cp1";
                    case 2:
                        return "Cp2";
                    case 3:
                        return "P2";
                    default:
                        return "";
                }
            }
        };

        renderer.setSeriesItemLabelGenerator(1, xyItemLabelGenerator);
        renderer.setSeriesItemLabelGenerator(3, xyItemLabelGenerator);

        renderer.setSeriesItemLabelsVisible(1, true);
        renderer.setSeriesItemLabelsVisible(3, true);

        final XYPlot xyplot = new XYPlot(dataset, domain, range, renderer);
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
