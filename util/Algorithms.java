package util;

import domain.User;

import java.util.*;

/**
 * Utility algorithms for traversing and analyzing the user graph.
 * Contains DFS, BFS shortest path and diameter calculation helpers used by the service layer.
 */
public class Algorithms {
    /**
     * Depth-first search that marks all reachable users starting from currentUser.
     *
     * @param currentUser starting user
     * @param visited set to collect visited users
     */
    public static void dfs(User currentUser, Set<User> visited) {
        visited.add(currentUser);
        for (User friend : currentUser.getFriends()) {
            if (!visited.contains(friend)) {
                dfs(friend, visited);
            }
        }
    }

    /**
     * Compute the shortest path length between start and end users using BFS.
     *
     * @param start source user
     * @param end target user
     * @return number of edges in the shortest path or -1 if not reachable
     */
    public static int bfsShortestPath(User start, User end) {
        Queue<User> queue = new LinkedList<>();
        Map<User, Integer> distances = new HashMap<>();

        queue.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            User current = queue.poll();

            if (current.equals(end)) {
                return distances.get(current);
            }

            for (User friend : current.getFriends()) {
                if (!distances.containsKey(friend)) {
                    distances.put(friend, distances.get(current) + 1);
                    queue.add(friend);
                }
            }
        }
        return -1; // Not reachable
    }

    /**
     * Compute the diameter of a community (list of users) as the maximum shortest-path
     * distance between any two users in the list.
     *
     * @param community list of users forming a connected component
     * @return the diameter (max shortest-path length)
     */
    public static int getDiameter(List<User> community) {
        if (community.size() < 2) return 0;

        int maxShortestPath = 0;
        for (int i = 0; i < community.size(); i++) {
            for (int j = i + 1; j < community.size(); j++) {
                User u = community.get(i);
                User v = community.get(j);

                int shortestPath = bfsShortestPath(u, v);
                maxShortestPath = Math.max(maxShortestPath, shortestPath);
            }
        }
        return maxShortestPath;
    }
}
