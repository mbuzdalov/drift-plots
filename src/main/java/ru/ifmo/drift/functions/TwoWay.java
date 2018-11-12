package ru.ifmo.drift.functions;

import java.util.function.ToDoubleFunction;

import ru.ifmo.drift.util.BitArray;

public final class TwoWay implements ToDoubleFunction<BitArray> {
    @Override
    public double applyAsDouble(BitArray value) {
        int n = value.size();
        int firstHalf = (n / 2) & ~1;
        int countFirstHalf = 0;
        for (int i = 0; i < firstHalf; ++i) {
            countFirstHalf += value.get(i) ? 1 : 0;
        }
        int distanceFromEdges = Math.min(countFirstHalf, firstHalf - countFirstHalf);
        if (distanceFromEdges != 0) {
            return n - firstHalf + distanceFromEdges;
        }
        int countSecondHalf = 0;
        if (value.get(0)) {
            // OneMax here
            for (int i = firstHalf; i < n; ++i) {
                countSecondHalf += value.get(i) ? 1 : 0;
            }
        } else {
            // LeadingOnes here
            for (int i = firstHalf; i < n; ++i) {
                if (value.get(i)) {
                    ++countSecondHalf;
                } else {
                    break;
                }
            }
        }
        return n - firstHalf - countSecondHalf;
    }

    @Override
    public String toString() {
        return "TwoWay";
    }
}
