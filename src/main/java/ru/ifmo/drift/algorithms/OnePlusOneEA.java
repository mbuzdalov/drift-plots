package ru.ifmo.drift.algorithms;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import ru.ifmo.drift.Algorithm;
import ru.ifmo.drift.OptimizationLogger;
import ru.ifmo.drift.util.BitArray;

public final class OnePlusOneEA extends Algorithm<BitArray> {
    private final double probabilityOverN;

    public OnePlusOneEA(double probabilityOverN) {
        this.probabilityOverN = probabilityOverN;
    }

    @Override
    public void optimize(ToDoubleFunction<BitArray> function,
                         Function<Random, BitArray> randomIndividualGenerator,
                         BiPredicate<BitArray, Double> shouldStop,
                         OptimizationLogger<? super BitArray> logger,
                         Random random) {
        logger.logProcessStarted();
        boolean[] myIndividual = randomIndividualGenerator.apply(random).toBooleanArray();
        BitArray myIndividualWrapper = BitArray.wrap(myIndividual);
        OptimizationLogger.Tag myTag = logger.createTag(myIndividualWrapper);
        double myFitness = function.applyAsDouble(myIndividualWrapper);
        logger.logFitnessEvaluation(myTag, myFitness);
        int n = myIndividual.length;
        double p = probabilityOverN / n;
        double log1p = Math.log1p(-p);

        int[] flips = new int[n];

        while (!shouldStop.test(myIndividualWrapper, myFitness)) {
            int bitCount = 0;
            do {
                int bitToFlip = -1;
                while (bitToFlip < n) {
                    if (bitToFlip >= 0) {
                        flips[bitCount++] = bitToFlip;
                        myIndividual[bitToFlip] ^= true;
                    }
                    bitToFlip += (int) (1 + Math.log(random.nextDouble()) / log1p);
                }
            } while (bitCount == 0);
            OptimizationLogger.Tag newTag = logger.createTag(myIndividualWrapper, myTag);
            double newFitness = function.applyAsDouble(myIndividualWrapper);
            logger.logFitnessEvaluation(newTag, newFitness);
            if (myFitness > newFitness) {
                myFitness = newFitness;
                myTag = newTag;
            } else {
                for (int i = 0; i < bitCount; ++i) {
                    myIndividual[flips[i]] ^= true;
                }
            }
        }
        logger.logProcessFinished();
    }
}
