package ru.ifmo.drift.functions;

import java.util.function.ToDoubleFunction;

import ru.ifmo.drift.util.BitArray;

/**
 * A simple and naive implementation of OneMax.
 */
public final class OneMax implements ToDoubleFunction<BitArray> {
    @Override
    public double applyAsDouble(BitArray bitArray) {
        int result = 0;
        for (int i = bitArray.size() - 1; i >= 0; --i) {
            result += bitArray.get(i) ? 1 : 0;
        }
        return result;
    }

    @Override
    public String toString() {
        return "OneMax";
    }
}
