package ru.yandex.practicum.filmorate.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class SqlHelper {
    private final StringBuilder sb;

    public SqlHelper() {
        sb = new StringBuilder();
    }

    public SqlHelper insert(LinkedHashMap<Field, Object> args) {
        Optional<Field> firstField = args.keySet().stream().findFirst();
        if (firstField.isPresent()) {
            sb.append("INSERT INTO ");
            sb.append(firstField.get().getTable());
            sb.append("(");
            sb.append(String.join(",", args.keySet().stream().map(String::valueOf)
                    .collect(Collectors.toCollection(LinkedList::new))));
            sb.append(") VALUES (");
            sb.append(String.join(",", args.values().stream().map(String::valueOf)
                    .collect(Collectors.toCollection(LinkedList::new))));
            sb.append(") ");
        }
        return this;
    }

    public SqlHelper select(Field... fields) {
        sb.append("SELECT ");
        sb.append(String.join(",", Arrays.stream(fields)
                .map(Field::getAliasField).collect(Collectors.toSet())));
        sb.append(" ");
        return this;
    }

    public SqlHelper update(Field... fields) {
        if (fields.length > 0) {
            sb.append("UPDATE ");
            sb.append(fields[0].getTable());
            sb.append(" SET ");
            sb.append(Arrays.stream(fields)
                    .map(f -> String.format("%s=?", f.name()))
                    .collect(Collectors.joining(",")));
            sb.append(" ");
        }
        return this;
    }

    public SqlHelper withValue(Object arg) {
        int index = sb.lastIndexOf("?");
        sb.replace(index, index + 1, arg.toString());
        return this;
    }

    public SqlHelper delete(Table table) {
        sb.append("DELETE FROM ");
        sb.append(table.getAliasTable()).append(" ");
        return this;
    }

    public SqlHelper where(Field field, Object... args) {
        if (args.length == 1) {
            if (args[0] instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) args[0];
                if (collection.size() == 1) {
                    where(String.format("%s = %s", field.getAliasField(),
                            collection.stream().findFirst().get()));
                }
                if (collection.size() > 1) {
                    where(String.format("%s IN (%s)", field.getAliasField(),
                            String.join(",", collection.stream().map(String::valueOf).collect(Collectors.toSet()))));
                }
            } else {
                where(String.format("%s = %s", field.getAliasField(), args[0]));
            }
        }
        if (args.length > 1) {
            where(String.format("%s IN (%s)", field.getAliasField(),
                    String.join(",", Arrays.stream(args).map(String::valueOf).collect(Collectors.toSet()))));
        }
        return this;
    }

    public SqlHelper where(Field field, String operator, String arg) {
        where(String.format("%s %s %s", field.getAliasField(), operator, arg));
        return this;
    }

    public SqlHelper where(String str) {
        sb.append("WHERE ").append(str).append(" ");
        return this;
    }

    public SqlHelper and(Field field, Object arg) {
        sb.append("AND ");
        equals(field, arg);
        return this;
    }

    public SqlHelper or(Field field, Object arg) {
        sb.append("OR ");
        equals(field, arg);
        return this;
    }

    private void equals(Field field, Object arg) {
        sb.append(field.getTable().getAlias()).append(".").append(field).append("=").append(arg);
        sb.append(" ");
    }

    public String equals(String arg1, String arg2) {
        return String.format("%s=%s", arg1, arg2);
    }

    public SqlHelper from(Table table) {
        sb.append("FROM ");
        sb.append(table);
        sb.append(" ").append(table.getAlias());
        sb.append(" ");
        return this;
    }

    public SqlHelper leftJoin(Field arg1, Field arg2) {
        sb.append("LEFT ");
        join(arg1, arg2);
        return this;
    }

    public SqlHelper rightJoin(Field arg1, Field arg2) {
        sb.append("RIGHT ");
        join(arg1, arg2);
        return this;
    }

    private void join(Field arg1, Field arg2) {
        sb.append("JOIN ")
                .append(arg1.getTable().getAliasTable())
                .append(" ON ")
                .append(arg1.getAliasField())
                .append("=")
                .append(arg2.getAliasField())
                .append(" ");
    }

    public SqlHelper orderByDesc(Field field) {
        orderBy(field);
        sb.append("DESC").append(" ");
        return this;
    }

    public SqlHelper orderBy(Field field) {
        sb.append("ORDER BY ").append(field.getAliasField()).append(" ");
        return this;
    }

    public SqlHelper groupBy(Field field) {
        sb.append("GROUP BY ").append(field.getAliasField()).append(" ");
        return this;
    }

    public SqlHelper having(String str) {
        sb.append("HAVING ").append(str).append(" ");
        return this;
    }

    public SqlHelper limit(int limit) {
        sb.append("LIMIT ").append(limit).append(" ");
        return this;
    }

    public SqlHelper append(String str) {
        sb.append(str);
        return this;
    }

    public String getYear(Field field) {
        return String.format("EXTRACT(YEAR FROM CAST(%s AS date))", field.getAliasField());
    }

    @Getter
    @RequiredArgsConstructor
    public enum Field {
        DIRECTOR_ID(Table.DIRECTOR),
        DIRECTOR_NAME(Table.DIRECTOR),

        EVENT_ID(Table.EVENTS),
        EVENT_USER_ID(Table.EVENTS),
        EVENT_ENTITY_ID(Table.EVENTS),
        EVENT_TIMESTAMP(Table.EVENTS),
        EVENT_TYPE(Table.EVENTS),
        EVENT_OPERATION(Table.EVENTS),

        FILM_DIRECTOR_FILM_ID(Table.FILM_DIRECTOR),
        FILM_DIRECTOR_DIRECTOR_ID(Table.FILM_DIRECTOR),

        FILM_GENRE_FILM_ID(Table.FILM_GENRE),
        FILM_GENRE_GENRE_ID(Table.FILM_GENRE),

        FILM_ID(Table.FILMS),
        FILM_NAME(Table.FILMS),
        FILM_DESCRIPTION(Table.FILMS),
        FILM_DURATION(Table.FILMS),
        FILM_RELEASE_DATE(Table.FILMS),
        FILM_MPA_ID(Table.FILMS),
        FILM_RATING(Table.FILMS),

        FRIENDSHIP_USER_ID(Table.FRIENDSHIP),
        FRIENDSHIP_FRIEND_ID(Table.FRIENDSHIP),
        FRIENDSHIP_STATUS(Table.FRIENDSHIP),

        GENRE_ID(Table.GENRE),
        GENRE_NAME(Table.GENRE),

        LIKE_FILM_ID(Table.LIKES),
        LIKE_USER_ID(Table.LIKES),

        MPA_ID(Table.MPA),
        MPA_NAME(Table.MPA),
        MPA_DESCRIPTION(Table.MPA),

        REVIEW_RATED_REVIEW_ID(Table.REVIEW_RATED),
        REVIEW_RATED_USER_ID(Table.REVIEW_RATED),
        REVIEW_RATED_RATED(Table.REVIEW_RATED),

        REVIEW_ID(Table.REVIEWS),
        REVIEW_CONTENT(Table.REVIEWS),
        REVIEW_IS_POSITIVE(Table.REVIEWS),
        REVIEW_USER_ID(Table.REVIEWS),
        REVIEW_FILM_ID(Table.REVIEWS),
        REVIEW_RATING(Table.REVIEWS),

        USER_ID(Table.USERS),
        USER_NAME(Table.USERS),
        USER_LOGIN(Table.USERS),
        USER_EMAIL(Table.USERS),
        USER_BIRTHDAY(Table.USERS);

        private final Table table;

        public String getAliasField() {
            return this.getTable().getAlias() + "." + this;
        }
    }

    @Getter
    public enum Table {
        DIRECTOR,
        EVENTS,
        FILM_DIRECTOR,
        FILM_GENRE,
        FILMS,
        FRIENDSHIP,
        GENRE,
        LIKES,
        MPA,
        REVIEW_RATED,
        REVIEWS,
        USERS;

        String getAliasTable() {
            return this.name() + " " + getAlias();
        }

        String getAlias() {
            return this.name().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
