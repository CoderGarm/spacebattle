package de.yuga.spacebattle.backend.devtools;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.combat.dto.AccelerationProfile;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElements;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An example of a time series chart create using JFreeChart.  For the most
 * part, default settings are used, except that the renderer is modified to
 * show filled shapes (as well as lines) at each data point.
 * <p>
 * todo -Djava.awt.headless=false needed
 */
public class XYSplineChart extends ApplicationFrame /* todo JFrame wenn listener gefunden -> headless? */ {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(XYSplineChart.class);

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
    public ChartPanel run(@Nonnull final Orbit planetaryOrbit, @Nonnull final Map<Owner, Maneuver> maneuvers) {
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");
        Preconditions.checkNotNull(maneuvers, "maneuvers must not be empty");

        XYSeries series;
        XYSeriesCollection dataset = new XYSeriesCollection();

        addPlanetaryOrbit(planetaryOrbit, dataset);

        final Set<Integer> helpSeriesIndices = new HashSet<>();
        final Set<Integer> mainSeriesIndices = new HashSet<>();
        for (final Owner owner : maneuvers.keySet()) {
            final Maneuver maneuver = maneuvers.get(owner);
            final ManeuverElements maneuverElements = maneuver.getCourseItems();

            final String username = owner.getUsername();
            series = new XYSeries(username + " '" + maneuver.getManeuverName() + "'");

            final List<ManeuverElement> courseElements = maneuverElements.getManeuverElements();

            for (final ManeuverElement courseElement : courseElements) {
                final CubicBezier bezier = courseElement.getCurve();
                final int sequenceNo = courseElement.getSequenceNo();

                helpSeriesIndices.add(setControlPoints(username + " #" + sequenceNo, bezier, dataset));

                final double length = bezier.getLength();
                for (double i = 0; i <= 1; ) {
                    double j = i + 0.01;
                    final double[] start = bezier.getPointAtLength(length * j);
                    final double[] end = bezier.getPointAtLength(length * j);
                    series.add(start[0], start[1]);
                    i = j;
                }
            }
            mainSeriesIndices.add(setSeries(dataset, series));
            drawIntersectionPoint(maneuver, dataset);
            drawThrustReversalPoints(maneuver, dataset);
        }

        final ChartPanel chartPanel = createPanel(dataset, mainSeriesIndices, helpSeriesIndices);
        chartPanel.setPreferredSize(new Dimension(900, 900));
        setContentPane(chartPanel);
        return chartPanel;
    }

    private static int setSeries(final XYSeriesCollection dataset, final XYSeries series) {
        dataset.addSeries(series);
        return dataset.indexOf(series);
    }

    private static void drawThrustReversalPoints(@Nonnull final Maneuver maneuver, @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final XYSeries series = new XYSeries(maneuver.getAgent().getOwner().getUsername() + " Thrust Reversal Points", false, true);
        final List<AccelerationProfile> accelerationProfile = maneuver.getAccelerationProfile();
        for (int i = 1; i < accelerationProfile.size(); i++) {
            final AccelerationProfile last = accelerationProfile.get(i - 1);
            final AccelerationProfile current = accelerationProfile.get(i);

            if (current.getDynamicInfo().getAcceleration().compareTo(last.getDynamicInfo().getAcceleration()) != 0) {
                final Orbit p = last.getDynamicInfo().getPosition();
                series.add(p.getXCoordinate().getCoordinate(), p.getYCoordinate().getCoordinate());
            }
        }

        if (!series.getItems().isEmpty()) {
            dataset.addSeries(series);
        }
    }

    private static void drawIntersectionPoint(@Nonnull final Maneuver maneuver, @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final Orbit intersectionPoint = maneuver.getIntersectionPoint();
        if (intersectionPoint != null) {
            final XYSeries intersect = new XYSeries("Intersection Point", false, true);
            intersect.add(intersectionPoint.getXCoordinate().getCoordinate(), intersectionPoint.getYCoordinate().getCoordinate());
            dataset.addSeries(intersect);
        }
    }

    private static int setControlPoints(@Nonnull final String title,
                                        @Nonnull final CubicBezier bezier,
                                        @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(title, "title must not be empty");
        Preconditions.checkNotNull(bezier, "bezier must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");


        final XYSeries series = new XYSeries(title + " controls", false, true);
        series.add(bezier.getP1()[0], bezier.getP1()[1]);
        series.add(bezier.getCp1()[0], bezier.getCp1()[1]);
        series.add(bezier.getCp2()[0], bezier.getCp2()[1]);
        series.add(bezier.getP2()[0], bezier.getP2()[1]);
        return setSeries(dataset, series);
    }


    private static void addPlanetaryOrbit(@Nonnull final Orbit planetaryOrbit,
                                          @Nonnull final XYSeriesCollection dataset) {
        Preconditions.checkNotNull(planetaryOrbit, "planetaryOrbit must not be empty");
        Preconditions.checkNotNull(dataset, "dataset must not be empty");

        final XYSeries planet = new XYSeries("Planet", false, true);
        planet.add(
                planetaryOrbit.getXCoordinate().getCoordinate().intValue(),
                planetaryOrbit.getYCoordinate().getCoordinate().intValue()
        );
        dataset.addSeries(planet);
    }

    @Nonnull
    public static ChartPanel createPanel(@Nonnull final XYDataset dataset,
                                         @Nonnull final Set<Integer> mainSeriesIndices,
                                         @Nonnull final Set<Integer> helpSeriesIndices) {
        Preconditions.checkNotNull(dataset, "dataset must not be empty");
        Preconditions.checkNotNull(mainSeriesIndices, "mainSeriesIndices must not be empty");
        Preconditions.checkNotNull(helpSeriesIndices, "helpSeriesIndices must not be empty");

        final JFreeChart chart = createChart(dataset, mainSeriesIndices, helpSeriesIndices);
        final ChartPanel panel = new ChartPanel(chart, false);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    @Nonnull
    private static JFreeChart createChart(@Nonnull final XYDataset dataset,
                                          @Nonnull final Set<Integer> mainSeriesIndices,
                                          @Nonnull final Set<Integer> helpSeriesIndices) {
        Preconditions.checkNotNull(dataset, "dataset must not be empty");
        Preconditions.checkNotNull(mainSeriesIndices, "mainSeriesIndices must not be empty");
        Preconditions.checkNotNull(helpSeriesIndices, "helpSeriesIndices must not be empty");

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

        helpSeriesIndices.forEach(index -> {
            renderer.setSeriesItemLabelGenerator(index, xyItemLabelGenerator);
            renderer.setSeriesItemLabelsVisible((int) index, true);
        });

        mainSeriesIndices.forEach(index -> renderer.setSeriesShapesVisible((int) index, false));

        final XYPlot xyplot = new XYPlot(dataset, domain, range, renderer);
        return new JFreeChart(xyplot);
    }

}
