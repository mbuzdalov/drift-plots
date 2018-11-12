package ru.ifmo.drift.loggers;

import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.drift.OptimizationLogger;

public final class PlotCollector extends OptimizationLogger<Object> {
    public static class FitnessPlotPoint {
        public final long evaluation;
        public final double fitness;

        FitnessPlotPoint(long evaluation, double fitness) {
            this.evaluation = evaluation;
            this.fitness = fitness;
        }
    }

    public static class DriftPlotPoint {
        public final double fitness;
        public final double drift;

        private DriftPlotPoint(double fitness, double drift) {
            this.fitness = fitness;
            this.drift = drift;
        }

        private DriftPlotPoint toRelative() {
            return new DriftPlotPoint(fitness, fitness - drift);
        }
    }

    private static class Stats {
        final double fitness;

        int countChildren = 0;
        double sumImprovements = 0;

        Stats(double fitness) {
            this.fitness = fitness;
        }

        void accept(double value) {
            ++countChildren;
            if (value < fitness) {
                sumImprovements += value;
            } else {
                sumImprovements += fitness;
            }
        }

        DriftPlotPoint makeAbsolutePoint() {
            return new DriftPlotPoint(fitness, sumImprovements / countChildren);
        }
    }

    private double bestFitness = Double.NaN;
    private long evaluationCount = 0;
    private List<FitnessPlotPoint> lastFitnessPlot = null;

    private final Map<Tag, Stats> currentStats = new IdentityHashMap<>();
    private final List<List<DriftPlotPoint>> absDriftPoints = new ArrayList<>();
    private final List<List<DriftPlotPoint>> relDriftPoints = new ArrayList<>();
    private final List<List<FitnessPlotPoint>> fitnessPlots = new ArrayList<>();

    public List<List<DriftPlotPoint>> getAbsoluteDriftPlots() {
        return Collections.unmodifiableList(absDriftPoints);
    }

    public List<List<DriftPlotPoint>> getRelativeDriftPlots() {
        return Collections.unmodifiableList(relDriftPoints);
    }

    public List<List<FitnessPlotPoint>> getFitnessPlots() {
        return Collections.unmodifiableList(fitnessPlots);
    }

    @Override
    public void logProcessStarted() {
        currentStats.clear();
        bestFitness = Double.NaN;
        evaluationCount = 0;
        lastFitnessPlot = new ArrayList<>();
    }

    @Override
    public void logProcessFinished() {
        List<DriftPlotPoint> absResult = currentStats.values().stream()
                .filter(s -> s.countChildren > 0 && s.sumImprovements > 0)
                .map(Stats::makeAbsolutePoint)
                .sorted(Comparator.comparingDouble(o -> o.fitness))
                .collect(Collectors.toList());
        List<DriftPlotPoint> relResult = absResult.stream().map(DriftPlotPoint::toRelative).collect(Collectors.toList());
        currentStats.clear();
        absDriftPoints.add(Collections.unmodifiableList(absResult));
        relDriftPoints.add(Collections.unmodifiableList(relResult));
        fitnessPlots.add(Collections.unmodifiableList(lastFitnessPlot));
        lastFitnessPlot = null;
    }

    @Override
    public void logFitnessEvaluation(Tag tag, double fitness) {
        currentStats.put(tag, new Stats(fitness));
        for (int i = tag.parentCount() - 1; i >= 0; --i) {
            currentStats.get(tag.parentAt(i)).accept(fitness);
        }
        if (!(fitness >= bestFitness)) { // also covers the NaN case
            if (evaluationCount > 0) {
                lastFitnessPlot.add(new FitnessPlotPoint(evaluationCount - 1, bestFitness));
            }
            lastFitnessPlot.add(new FitnessPlotPoint(evaluationCount, fitness));
            bestFitness = fitness;
        }
        ++evaluationCount;
    }
}
