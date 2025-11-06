package util;

import domain.Duck;

/**
 * Immutable result of an optimization: minimal time and the duck assignments
 * per lane (one duck per lane, length equals number of lanes).
 */
public class SolveResult {
    private final double minimalTime;
    private final Duck[] assignments;

    /**
     * Create a result wrapper for the optimizer output.
     *
     * @param minimalTime minimal feasible time bound
     * @param assignments per-lane duck assignments
     */
    public SolveResult(double minimalTime, Duck[] assignments) {
        this.minimalTime = minimalTime;
        this.assignments = assignments;
    }

    /**
     * @return minimal feasible time bound found by the optimizer
     */
    public double getMinimalTime() { return minimalTime; }

    /**
     * @return per-lane duck assignments chosen by the optimizer
     */
    public Duck[] getAssignments() { return assignments; }
}
