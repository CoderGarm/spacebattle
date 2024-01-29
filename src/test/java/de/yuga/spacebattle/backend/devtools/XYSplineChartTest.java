package de.yuga.spacebattle.backend.devtools;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

class XYSplineChartTest {


    public static final String Title = "Spline Test";

    public static void main(String[] args) {
        EventQueue.invokeLater(new XYSplineChartTest()::display);
    }

    private void display() {
        XYSeries series = new XYSeries("Frequency");
        series.add(0, 1);
        series.add(1, 1);
        series.add(2, 3);
        series.add(3, 7);
        series.add(4, 11);
        series.add(5, 21);
        series.add(6, 28);
        series.add(7, 16);
        series.add(8, 22);
        series.add(9, 7);
        series.add(10, 1);
        series.add(11, 2);
        XYDataset dataset = new XYSeriesCollection(series);
        String[] labels = new String[series.getItemCount()];
        labels[0] = "<46";
        labels[1] = "46-55";
        labels[2] = "56-65";
        labels[3] = "66-75";
        labels[4] = "76-85";
        labels[5] = "86-95";
        labels[6] = "96-105";
        labels[7] = "106-115";
        labels[8] = "116-125";
        labels[9] = "126-135";
        labels[10] = "136-145";
        labels[11] = ">146";
        NumberAxis domain = new SymbolAxis("X", labels);
        NumberAxis range = new NumberAxis("Y");
        XYSplineRenderer r = new XYSplineRenderer(8);
        XYPlot xyplot = new XYPlot(dataset, domain, range, r);
        JFreeChart chart = new JFreeChart(xyplot);
        ChartPanel chartPanel = new ChartPanel(chart) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        JFrame frame = new JFrame(Title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
