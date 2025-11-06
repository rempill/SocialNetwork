package util;

import domain.Duck;

/**
 * Strategy interface for computing an optimal assignment of ducks to race lanes
 * that minimizes the maximal per-lane round-trip time.
 */
public interface OptimizationStrategy {
    /**
     * Compute the minimal feasible time and an assignment of ducks to lanes.
     *
     * @param ducks     available ducks (candidates)
     * @param distances per-lane distances (M entries)
     * @return {@link SolveResult} containing the minimal time and the chosen assignment
     */
    SolveResult computeMinTime(Duck[] ducks, double[] distances);
}
