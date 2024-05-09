package ru.yandex.practicum.filmorate.recommendations;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

public class SlopeOne {
    private final Map<Film, HashMap<Film, Integer>> freq;
    private final Map<Film, HashMap<Film, Double>> diff;
    private final Map<Long, HashMap<Film, Integer>> usersFilms;
    private final Long userId;

    private static final int POSITIVE_LOW = 6; // минимальный положительный рейтинг

    public SlopeOne(Map<Long, HashMap<Film, Integer>> usersFilms, Long userId) {
        this.freq = new HashMap<>();
        this.diff = new HashMap<>();
        this.usersFilms = usersFilms;
        this.userId = userId;
    }

    public Collection<Film> slopeOne() {
        if (!usersFilms.containsKey(userId)) {
            return new ArrayList<>();
        }
        buildFreqAndDiffMatrices();
        return getRecommendations();
    }

    private void buildFreqAndDiffMatrices() {
        for (HashMap<Film, Integer> userId : usersFilms.values()) {
            for (Map.Entry<Film, Integer> e : userId.entrySet()) {
                if (!diff.containsKey(e.getKey())) {
                    diff.put(e.getKey(), new HashMap<Film, Double>());
                    freq.put(e.getKey(), new HashMap<Film, Integer>());
                }

                // сравнение рейтингов всех фильмов
                for (Map.Entry<Film, Integer> e2 : userId.entrySet()) {
                    int oldCount = 0;
                    if (freq.get(e.getKey()).containsKey(e2.getKey())) {
                        oldCount = freq.get(e.getKey()).get(e2.getKey()).intValue();
                    }

                    double oldDiff = 0.0;
                    if (diff.get(e.getKey()).containsKey(e2.getKey())) {
                        oldDiff = diff.get(e.getKey()).get(e2.getKey()).doubleValue();
                    }

                    double observedDiff = e.getValue() - e2.getValue();
                    freq.get(e.getKey()).put(e2.getKey(), oldCount + 1);
                    diff.get(e.getKey()).put(e2.getKey(), oldDiff + observedDiff);

                }
            }
        }

        for (Film j : diff.keySet()) {
            for (Film i : diff.get(j).keySet()) {
                double oldValue = diff.get(j).get(i).doubleValue();
                int count = freq.get(j).get(i).intValue();
                diff.get(j).put(i, oldValue / count);
            }
        }
    }

    private Collection<Film> getRecommendations() {
        HashMap<Film, Integer> recommendations = new HashMap<>();
        HashMap<Film, Double> uPred = new HashMap<>();
        HashMap<Film, Integer> uFreq = new HashMap<>();
        Map<Long, HashMap<Film, Integer>> outputData = new HashMap<>();

        for (Film j : diff.keySet()) {
            uFreq.put(j, 0);
            uPred.put(j, 0.0);
        }

        for (Map.Entry<Long, HashMap<Film, Integer>> e : usersFilms.entrySet()) {
            for (Film j : e.getValue().keySet()) {
                for (Film k : diff.keySet()) {
                    try {
                        double predictedValue = diff.get(k).get(j).doubleValue() + e.getValue().get(j).doubleValue();
                        double finalValue = predictedValue * freq.get(k).get(j).intValue();
                        uPred.put(k, uPred.get(k) + finalValue);
                        uFreq.put(k, uFreq.get(k) + freq.get(k).get(j).intValue());
                    } catch (NullPointerException ignored) {
                    }
                }
            }

            HashMap<Film, Integer> clean = new HashMap<Film, Integer>();

            for (Film j : uPred.keySet()) {
                if (uFreq.get(j) > 0) {
                    clean.put(j, (int) Math.round(uPred.get(j).doubleValue() / uFreq.get(j).intValue()));
                }
            }
            for (Film j : e.getValue().keySet()) {
                if (e.getValue().containsKey(j)) {
                    clean.put(j, e.getValue().get(j));
                } else if (!clean.containsKey(j)) {
                    clean.put(j, -1);
                }
            }

            recommendations.putAll(clean);
            // TODO: удалить после дебага
//            recommendations.forEach((film, rating) -> System.out.println(film.getId() + ": " + rating));
        }

        /**
         На основе полученных предполагаемых рейтингов выводим сет фильмов без уже оценённых пользователем.
         Сет сортирован по рейтингу и содержит только те фильмы, у которых рейтинг положительный.
         */

        Set<Film> recFilms = recommendations.entrySet().stream()
                .filter(e -> !usersFilms.get(userId).keySet().contains(e.getKey()) && e.getValue() >= POSITIVE_LOW)
                .sorted(Map.Entry.<Film, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // TODO : удалить после дебага
        // recFilms.forEach(film -> System.out.println(film.getId()));

        return recFilms;
    }
}
