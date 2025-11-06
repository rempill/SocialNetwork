/**
 * Optimization strategy that assigns one duck per lane to minimize the
 * maximum per-lane round-trip time. Uses a binary search on time and a
 * greedy feasibility check to decide if all lanes can be completed within
 * a given time bound.
 */
package util;

import domain.Duck;

public class BinarySearchSwimOptimizer implements OptimizationStrategy {
    /** Best assignments found for the last successful feasibility check (one duck per lane). */
    private Duck[] bestAssignments;
    private static final int MAX_ITERATIONS = 100;

    /**
     * Compute the minimal time T such that there exists an assignment of ducks to lanes where
     * each lane i (with distance distances[i]) can be completed as a round trip by its assigned
     * duck in at most T seconds. Time per lane is computed as 2*distance/speed.
     *
     * @param ducks      array of candidate ducks (their speed must be > 0)
     * @param distances  per-lane distances (M entries, M > 0)
     * @return a {@link SolveResult} containing the minimal time and the duck assigned to each lane
     */
    @Override
    public SolveResult computeMinTime(Duck[] ducks, double[] distances) {
        int M = distances.length;
        bestAssignments = new Duck[M];

        double low = 0;
        // Use the maximum distance across all lanes as upper-bound reference
        double maxDist = 0.0;
        for (double d : distances) if (d > maxDist) maxDist = d;
        double minSpeed = Double.MAX_VALUE;
        for (Duck duck : ducks) {
            if (duck.getViteza() < minSpeed) {
                minSpeed = duck.getViteza();
            }
        }
        if (ducks.length == 0 || minSpeed <= 0) {
            return new SolveResult(Double.POSITIVE_INFINITY, new Duck[M]);
        }
        double high = (2.0 * maxDist) / minSpeed;
        double minTime = high;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double mid = (low + high) / 2.0;
            Duck[] currentAssignments = new Duck[M];
            if (feasible(mid, ducks, distances, currentAssignments)) {
                minTime = mid;
                high = mid;
                bestAssignments = currentAssignments;
            } else {
                low = mid;
            }
        }
        return new SolveResult(minTime, bestAssignments);
    }

    /**
     * Check if it is possible to assign one unused duck to each lane so that every lane finishes
     * within the provided time bound. Greedily picks, for each lane, the available duck with the
     * smallest time that is still within the bound.
     *
     * @param time                the candidate maximal per-lane time bound
     * @param ducks               available ducks
     * @param distances           lane distances
     * @param currentAssignments  output array populated with chosen duck per lane when feasible
     * @return true if an assignment within the bound exists, false otherwise
     */
    private boolean feasible(double time, Duck[] ducks, double[] distances, Duck[] currentAssignments) {
        int N = ducks.length;
        int M = distances.length;
        if (N < M) return false; // not enough ducks
        boolean[] used = new boolean[N];

        for (int i = 0; i < M; i++) {
            double laneDistance = distances[i];
            int chosen = -1;
            double bestTime = Double.POSITIVE_INFINITY;

            for (int j = 0; j < N; j++) {
                if (used[j]) continue;
                Duck duck = ducks[j];
                double timeTaken = (2.0 * laneDistance) / duck.getViteza();
                if (timeTaken <= time && timeTaken < bestTime) {
                    bestTime = timeTaken;
                    chosen = j;
                }
            }
            if (chosen == -1) {
                // no available duck can finish this lane within the given time
                return false;
            }
            used[chosen] = true;
            currentAssignments[i] = ducks[chosen];
        }
        return true;
    }
}
