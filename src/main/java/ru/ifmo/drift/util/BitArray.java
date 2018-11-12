package ru.ifmo.drift.util;

/**
 * An interface to an unmodifiable bit array.
 *
 * Ensures that the fitness function does not try to change the individual.
 */
public abstract class BitArray {
    /**
     * Returns the size of the array.
     * @return the size of the array.
     */
    public abstract int size();

    /**
     * Returns the bit value at the given index.
     * @param index the index.
     * @return the bit value.
     */
    public abstract boolean get(int index);

    /**
     * Returns a new boolean array which contains the same bits.
     * @return the new boolean array.
     */
    public boolean[] toBooleanArray() {
        int size = size();
        boolean[] rv = new boolean[size];
        for (int i = 0; i < size; ++i) {
            rv[i] = get(i);
        }
        return rv;
    }

    /**
     * Wraps the given array of Boolean values.
     * @param array the array to wrap.
     * @return the wrapper around the array.
     */
    public static BitArray wrap(final boolean[] array) {
        return new BitArray() {
            @Override
            public int size() {
                return array.length;
            }

            @Override
            public boolean get(int index) {
                return array[index];
            }

            @Override
            public boolean[] toBooleanArray() {
                return array.clone();
            }
        };
    }
}
