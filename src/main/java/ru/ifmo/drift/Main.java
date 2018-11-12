package ru.ifmo.drift;

import java.awt.*;
import java.util.*;
import java.util.List;
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
import ru.ifmo.drift.functions.TwoWay;
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

    private static <T> void consumeSeries(DefaultXYDataset target, String keyPrefix, List<List<T>> sources,
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

        List<ToDoubleFunction<BitArray>> functions = Arrays.asList(new OneMax(), new TwoWay());
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

            List<JTabbedPane> functionPanes = new ArrayList<>();
            for (ToDoubleFunction<BitArray> fun : functions) {
                JTabbedPane innerPane = new JTabbedPane();
                tabbedPane.add(fun.toString(), innerPane);
                functionPanes.add(innerPane);
            }

            new Thread(() -> {
                Random random = ThreadLocalRandom.current();

                for (int funIdx = 0; funIdx < functions.size(); ++funIdx) {
                    JTabbedPane innerTabbedPane = functionPanes.get(funIdx);
                    ToDoubleFunction<BitArray> fun = functions.get(funIdx);
                    for (int n = from; n <= to; n += step) {
                        DefaultXYDataset fitnessPlots = new DefaultXYDataset();
                        DefaultXYDataset driftPlots = new DefaultXYDataset();
                        Function<Random, BitArray> generator = getRandomGenerator(n);

                        for (AlgorithmWithNameAndColor instance : config) {
                            Algorithm<BitArray> algorithm = instance.algorithm;
                            String name = instance.name + ",#";
                            PlotCollector collector = new PlotCollector();
                            for (int t = 0; t < times; ++t) {
                                algorithm.optimize(fun, generator, termination, collector, random);
                            }
                            consumeSeries(fitnessPlots, name, collector.getFitnessPlots(), v -> v.evaluation, v -> v.fitness);
                            consumeSeries(driftPlots, name, collector.getDriftPlots(), v -> v.fitness, v -> v.drift);
                        }

                        JFreeChart fitnessChart = ChartFactory.createXYLineChart("Fitness Plot", "Time", "Fitness", fitnessPlots);
                        JFreeChart driftChart = ChartFactory.createXYLineChart("Drift Plot", "Fitness", "Drift", driftPlots);

                        XYPlot fitnessXYPlot = fitnessChart.getXYPlot();
                        XYPlot driftXYPlot = driftChart.getXYPlot();

                        driftXYPlot.setRangeAxis(new LogarithmicAxis(driftXYPlot.getRangeAxis().getLabel()));

                        XYItemRenderer fitnessRenderer = fitnessXYPlot.getRenderer();
                        XYItemRenderer driftRenderer = driftXYPlot.getRenderer();
                        for (int cfg = 0, j = 0; cfg < config.size(); ++cfg) {
                            Color color = config.get(cfg).color;
                            for (int t = 0; t < times; ++t, ++j) {
                                fitnessRenderer.setSeriesPaint(j, color);
                                driftRenderer.setSeriesPaint(j, color);
                            }
                        }

                        final int N = n;
                        SwingUtilities.invokeLater(() -> {
                            JPanel thePanel = new JPanel(new GridLayout(1, 2));
                            thePanel.add(new ChartPanel(fitnessChart));
                            thePanel.add(new ChartPanel(driftChart));
                            innerTabbedPane.add("N = " + N, thePanel);
                        });
                    }
                }
            }).start();
        });
    }
}
