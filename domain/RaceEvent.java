package domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Special event for swimming ducks. It auto-selects M ducks according to
 * simple swimming criteria and simulates the race.
 */
public class RaceEvent extends Event {
    private final int lanes; // M
    private final List<Duck> participants = new ArrayList<>();
    private double[] distances; // per-lane distances

    /**
     * Construct a race event with the specified number of lanes (M). Distances default to 1.0.
     *
     * @param id    event id
     * @param name  event name
     * @param lanes number of lanes (M > 0)
     */
    public RaceEvent(int id, String name, int lanes) {
        super(id, name);
        if(lanes <= 0) throw new IllegalArgumentException("lanes must be > 0");
        this.lanes = lanes;
        this.distances = new double[lanes];
        for (int i = 0; i < lanes; i++) this.distances[i] = 1.0; // default
    }

    /**
     * @return number of lanes (M) in this race
     */
    public int getLanes() { return lanes; }

    /**
     * @return the currently selected race participants (read-only snapshot semantics)
     */
    public List<Duck> getParticipants() { return participants; }

    /**
     * Set the per-lane distances, must have exactly M entries.
     *
     * @param distances distances per lane
     * @throws IllegalArgumentException if array is null or its length != M
     */
    public void setDistances(double[] distances){
        if(distances == null || distances.length != lanes) throw new IllegalArgumentException("distances length must be M");
        this.distances = distances;
    }

    /**
     * Select up to M participant ducks from the provided list, filtering to swimmers (Inotator)
     * and sorting by speed desc, then endurance desc.
     *
     * @param allDucks candidate ducks (may contain non-swimmers)
     */
    public void selectParticipants(List<Duck> allDucks){
        participants.clear();
        if(allDucks == null) return;
        List<Duck> swimmers = allDucks.stream()
                .filter(d -> d instanceof Inotator)
                .sorted(Comparator.comparingDouble(Duck::getViteza).reversed()
                        .thenComparing(Comparator.comparingDouble(Duck::getRezistenta).reversed()))
                .limit(lanes)
                .collect(Collectors.toList());
        participants.addAll(swimmers);
        notifySubscribers("Race participants selected: " + participants.stream().map(Duck::getUsername).collect(Collectors.joining(", ")));
    }

    /**
     * Run the race using the current participants and configured distances and return a textual
     * report per lane (one line per lane assigned) plus a final line with the minimal total time.
     *
     * @return list of report lines describing per-lane assignments and minimal total time
     */
    public List<String> runRaceAndReport(){
        List<String> report = new ArrayList<>();
        if(participants.isEmpty()){
            report.add("No participants selected.");
            return report;
        }
        util.OptimizationStrategy opt = new util.BinarySearchSwimOptimizer();
        util.SolveResult result = opt.computeMinTime(participants.toArray(new Duck[0]), distances);
        Duck[] assignment = result.getAssignments();
        double minimalTime = result.getMinimalTime();

        for (int i = 0; i < assignment.length; i++) {
            Duck d = assignment[i];
            if (d == null) continue;
            int lane = i + 1;
            double t = (2.0 * distances[i]) / d.getViteza();
            report.add(String.format("Duck %d on lane %d: t = %.3f s", d.getId(), lane, t));
        }
        report.add(String.format("Minimal total time: %.3f s", minimalTime));
        notifySubscribers("Race finished. Results available.");
        return report;
    }
}
