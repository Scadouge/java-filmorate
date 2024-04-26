DROP TABLE IF EXISTS likes, film_genre, friendship, genre, films, mpa, users;

CREATE TABLE IF NOT EXISTS users
(
    user_id  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR NOT NULL,
    login    VARCHAR NOT NULL,
    email    VARCHAR NOT NULL,
    birthday DATE    NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR NOT NULL,
    description VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR      NOT NULL,
    description  VARCHAR(200) NOT NULL,
    duration     INTEGER      NOT NULL,
    release_date DATE         NOT NULL,
    mpa_id       INTEGER,

    FOREIGN KEY (mpa_id) REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS genre
(
    genre_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship
(
    user_id   INTEGER,
    friend_id INTEGER,
    status    VARCHAR NOT NULL,

    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id INTEGER,
    user_id INTEGER,

    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_genre
(
    film_id  INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,

    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre (genre_id) ON DELETE CASCADE
);