package ru.ifmo.drift;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * A base class for an optimization algorithm.
 * @param <T> the type of the individual.
 */
public abstract class Algorithm<T> {
    /**
     * Run optimization of the given function.
     * @param function the function to optimize.
     * @param randomIndividualGenerator the function to generate random individuals.
     * @param shouldStop the termination condition.
     * @param random the random number generator to use.
     */
    public abstract void optimize(ToDoubleFunction<T> function,
                                  Function<Random, T> randomIndividualGenerator,
                                  BiPredicate<T, Double> shouldStop,
                                  OptimizationLogger<? super T> logger,
                                  Random random);
}
