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
                sumImprovements += fitness - value;
            }
        }

        DriftPlotPoint makePoint() {
            return new DriftPlotPoint(fitness, sumImprovements / countChildren);
        }
    }

    private double bestFitness = Double.NaN;
    private long evaluationCount = 0;
    private List<FitnessPlotPoint> lastFitnessPlot = null;

    private final Map<Tag, Stats> currentStats = new IdentityHashMap<>();
    private final List<List<DriftPlotPoint>> driftPoints = new ArrayList<>();
    private final List<List<FitnessPlotPoint>> fitnessPlots = new ArrayList<>();

    public List<List<DriftPlotPoint>> getDriftPlots() {
        return Collections.unmodifiableList(driftPoints);
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
        List<DriftPlotPoint> driftResult = currentStats.values().stream()
                .filter(s -> s.countChildren > 0 && s.sumImprovements > 0)
                .map(Stats::makePoint)
                .sorted(Comparator.comparingDouble(o -> o.fitness))
                .collect(Collectors.toList());
        currentStats.clear();
        driftPoints.add(Collections.unmodifiableList(driftResult));
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
