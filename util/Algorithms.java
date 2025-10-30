package util;

import domain.User;

import java.util.*;

public class Algorithms {
    public static void dfs(User currentUser, Set<User> visited) {
        visited.add(currentUser);
        for (User friend : currentUser.getFriends()) {
            if (!visited.contains(friend)) {
                dfs(friend, visited);
            }
        }
    }

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
        return -1; // Nu ar trebui să se întâmple dacă sunt în aceeași comunitate
    }

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
