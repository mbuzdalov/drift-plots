package ru.ifmo.drift;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import ru.ifmo.drift.algorithms.OnePlusOneEA;
import ru.ifmo.drift.functions.OneMax;
import ru.ifmo.drift.loggers.PlotCollector;
import ru.ifmo.drift.util.BitArray;
import ru.ifmo.drift.util.CommandLineArgs;

public class Main {
    private static Function<Random, BitArray> getRandomGenerator(int nBits) {
        return random -> {
            boolean[] rv = new boolean[nBits];
            for (int i = 0; i < nBits; ++i) {
                rv[i] = random.nextBoolean();
            }
            return BitArray.wrap(rv);
        };
    }

    private static <T> void consumeOneSeries(DefaultXYDataset target, String keyPrefix, List<List<T>> sources,
                                             ToDoubleFunction<T> first, ToDoubleFunction<T> second) {
        for (int i = 0; i < sources.size(); ++i) {
            List<T> source = sources.get(i);
            String key = keyPrefix + (i + 1);
            double[][] plot = new double[2][source.size()];
            for (int j = 0; j < source.size(); ++j) {
                T value = source.get(j);
                plot[0][j] = first.applyAsDouble(value);
                plot[1][j] = second.applyAsDouble(value);
            }
            target.addSeries(key, plot);
        }
    }

    private static class AlgorithmWithNameAndColor {
        final Algorithm<BitArray> algorithm;
        final String name;
        final Color color;

        private AlgorithmWithNameAndColor(Algorithm<BitArray> algorithm, String name, Color color) {
            this.algorithm = algorithm;
            this.name = name;
            this.color = color;
        }
    }

    public static void main(String[] args0) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        CommandLineArgs args = new CommandLineArgs(args0);
        int from = Integer.parseInt(args.getOption("from", "100"));
        int to = Integer.parseInt(args.getOption("to", "1000"));
        int step = Integer.parseInt(args.getOption("step", "100"));
        int times = Integer.parseInt(args.getOption("times", "25"));

        ToDoubleFunction<BitArray> fun = new OneMax();
        BiPredicate<BitArray, Double> termination = (ind, fitness) -> fitness == 0;

        List<AlgorithmWithNameAndColor> config = Arrays.asList(
                new AlgorithmWithNameAndColor(new OnePlusOneEA(1), "1/n", Color.BLUE),
                new AlgorithmWithNameAndColor(new OnePlusOneEA(3), "3/n", Color.RED)
        );

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JTabbedPane tabbedPane = new JTabbedPane();
            frame.add(tabbedPane);
            frame.setVisible(true);

            new Thread(() -> {
                Random random = ThreadLocalRandom.current();

                for (int n = from; n <= to; n += step) {
                    DefaultXYDataset fitnessPlots = new DefaultXYDataset();
                    DefaultXYDataset absDriftPlots = new DefaultXYDataset();
                    DefaultXYDataset relDriftPlots = new DefaultXYDataset();
                    Function<Random, BitArray> generator = getRandomGenerator(n);

                    for (AlgorithmWithNameAndColor instance : config) {
                        Algorithm<BitArray> algorithm = instance.algorithm;
                        String name = instance.name + ",#";
                        PlotCollector collector = new PlotCollector();
                        for (int t = 0; t < times; ++t) {
                            algorithm.optimize(fun, generator, termination, collector, random);
                        }
                        consumeOneSeries(fitnessPlots, name, collector.getFitnessPlots(), v -> v.evaluation, v -> v.fitness);
                        consumeOneSeries(absDriftPlots, name, collector.getAbsoluteDriftPlots(), v -> v.fitness, v -> v.drift);
                        consumeOneSeries(relDriftPlots, name, collector.getRelativeDriftPlots(), v -> v.fitness, v -> v.drift);
                    }

                    JFreeChart fitnessChart = ChartFactory.createXYLineChart("Fitness Plot", "Time", "Fitness", fitnessPlots);
                    JFreeChart absDriftChart = ChartFactory.createXYLineChart("Absolute Drift Plot", "Fitness", "Drift", absDriftPlots);
                    JFreeChart relDriftChart = ChartFactory.createXYLineChart("Relative Drift Plot", "Fitness", "Drift", relDriftPlots);

                    XYPlot fitnessXYPlot = fitnessChart.getXYPlot();
                    XYPlot absDriftXYPlot = absDriftChart.getXYPlot();
                    XYPlot relDriftXYPlot = relDriftChart.getXYPlot();

                    relDriftXYPlot.setDomainAxis(new LogarithmicAxis(relDriftXYPlot.getDomainAxis().getLabel()));
                    relDriftXYPlot.setRangeAxis(new LogarithmicAxis(relDriftXYPlot.getRangeAxis().getLabel()));

                    XYItemRenderer fitnessRenderer = fitnessXYPlot.getRenderer();
                    XYItemRenderer absDriftRenderer = absDriftXYPlot.getRenderer();
                    XYItemRenderer relDriftRenderer = relDriftXYPlot.getRenderer();
                    for (int cfg = 0, j = 0; cfg < config.size(); ++cfg) {
                        Color color = config.get(cfg).color;
                        for (int t = 0; t < times; ++t, ++j) {
                            fitnessRenderer.setSeriesPaint(j, color);
                            absDriftRenderer.setSeriesPaint(j, color);
                            relDriftRenderer.setSeriesPaint(j, color);
                        }
                    }

                    final int N = n;
                    SwingUtilities.invokeLater(() -> {
                        JPanel thePanel = new JPanel(new GridLayout(1, 3));
                        thePanel.add(new ChartPanel(fitnessChart));
                        thePanel.add(new ChartPanel(absDriftChart));
                        thePanel.add(new ChartPanel(relDriftChart));
                        tabbedPane.add("N = " + N, thePanel);
                    });
                }
            }).start();
        });
    }
}
