package ru.yandex.practicum.filmorate.recommendations;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

public class SlopeOne {
    private final Map<Film, HashMap<Film, Integer>> freq;
    private final Map<Long, List<Film>> usersFilms;
    private final Long userId;

    public SlopeOne(Map<Long, List<Film>> usersFilms, Long userId) {
        this.freq = new HashMap<>();
        this.usersFilms = usersFilms;
        this.userId = userId;
    }

    public Collection<Film> slopeOne() {
        if (!usersFilms.containsKey(userId)) {
            return new ArrayList<>();
        }
        buildFreqMatrix();
        return getRecommendations();
    }

    private void buildFreqMatrix() {
        for (Map.Entry<Long, List<Film>> e : usersFilms.entrySet()) {
            for (Film f : e.getValue()) {
                if (!freq.containsKey(f)) {
                    freq.put(f, new HashMap<>());
                }

                for (Film f2 : e.getValue()) {
                    int oldCount = 0;
                    if (freq.get(f).containsKey(f2)) {
                        oldCount = freq.get(f).get(f2);
                    }
                    freq.get(f).put(f2, oldCount + 1);
                }
            }
        }
    }

    private Collection<Film> getRecommendations() {
        HashMap<Film, Integer> recommendations = new HashMap<>();
        for (Film f : usersFilms.get(userId)) {
            for (Film f2 : freq.keySet()) {
                try {
                    int oldCount = 0;
                    if (recommendations.containsKey(f2)) {
                        oldCount = recommendations.get(f2);
                    }
                    int count = freq.get(f2).get(f);
                    if (!usersFilms.get(userId).contains(f2)) {
                        recommendations.put(f2, oldCount + count);
                    }
                } catch (NullPointerException ignored) {
                }
            }
        }
        return recommendations.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}