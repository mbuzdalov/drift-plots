package ru.ifmo.drift;

/**
 * This is a base class for all classes that wish to track the optimization process.
 * @param <T>
 */
public abstract class OptimizationLogger<T> {
    /**
     * A tag that marks a particular individual.
     * Although different individuals at different times might be backed by the same object,
     * tags should be different.
     */
    public static final class Tag {
        private final Tag[] parents;
        private Tag(Tag... parents) {
            this.parents = parents;
        }

        public int parentCount() {
            return parents.length;
        }
        public Tag parentAt(int index) {
            return parents[index];
        }
    }

    /**
     * This function is called by the optimization algorithm when it starts optimization;
     */
    public abstract void logProcessStarted();

    /**
     * This function is called by the optimization algorithm when it finishes optimization;
     */
    public abstract void logProcessFinished();

    /**
     * This function is called by the optimization algorithm just after it evaluated a fitness of an individual.
     * @param tag the tag corresponding to the evaluated individual.
     * @param fitness the fitness of the individual.
     */
    public abstract void logFitnessEvaluation(Tag tag, double fitness);

    /**
     * Creates a tag out of an individual and the tags of its parents.
     * @param individual the individual to create a tag for.
     * @param parents the tags of the parents of the individual.
     * @return the tag for the individual.
     */
    public final Tag createTag(@SuppressWarnings("unused") T individual, Tag... parents) {
        return new Tag(parents);
    }
}
