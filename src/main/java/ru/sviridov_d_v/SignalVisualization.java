package ru.sviridov_d_v;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.apache.commons.math3.complex.Complex;


import javax.swing.*;
import java.awt.*;

public class SignalVisualization extends ApplicationFrame {
    public SignalVisualization(String title) {
        super(title);
        setLayout(new BorderLayout());

        XYSeriesCollection dataset = new XYSeriesCollection();

        // Generate and plot signals and their spectra
        for (int frequency : new int[]{1, 2, 4, 8}) {
            XYSeries signalSeries = generateSignal(frequency);
            XYSeries spectrumSeries = calculateSpectrum(signalSeries, frequency);
            dataset.addSeries(signalSeries);
            dataset.addSeries(spectrumSeries);
        }

        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel, BorderLayout.CENTER);
    }

    private XYSeries generateSignal(int frequency) {
        XYSeries signalSeries = new XYSeries("Signal " + frequency + " Hz");
        double duration = 1.0; // Duration of the signal in seconds
        double sampleRate = 1000.0; // Sample rate in Hz
        double dt = 1.0 / sampleRate;

        for (double t = 0; t < duration; t += dt) {
            double value = Math.sin(2 * Math.PI * frequency * t); // Generate a harmonic signal
            signalSeries.add(t, value);
        }

        return signalSeries;
    }

    private XYSeries calculateSpectrum(XYSeries signalSeries, int frequency) {
        XYSeries spectrumSeries = new XYSeries("Spectrum " + frequency + " Hz");
        int N = signalSeries.getItemCount(); // Number of samples

        // Find the next power of 2 greater than or equal to N
        int paddedLength = Integer.highestOneBit(N - 1) << 1;

        double[] signalData = new double[paddedLength];
        for (int i = 0; i < N; i++) {
            signalData[i] = signalSeries.getY(i).doubleValue();
        }

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] spectrum = transformer.transform(signalData, TransformType.FORWARD);

        double sampleRate = 1000.0; // Sample rate in Hz
        for (int k = 0; k < paddedLength; k++) {
            double frequencyHz = k * sampleRate / paddedLength;
            double magnitude = spectrum[k].abs(); // Get magnitude of the complex number
            spectrumSeries.add(frequencyHz, magnitude);
        }

        return spectrumSeries;
    }

    private JFreeChart createChart(XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Signal and Spectrum Plots",
                "Time (s) / Frequency (Hz)",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        return chart;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SignalVisualization demo = new SignalVisualization("Signal and Spectrum Plots");
            demo.pack();
            demo.setVisible(true);
        });
    }
}
