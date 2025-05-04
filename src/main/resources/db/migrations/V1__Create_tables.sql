CREATE TABLE authors
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL
);

CREATE TABLE books
(
    id             SERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    author_id      INTEGER REFERENCES authors (id),
    published_year INTEGER
);

CREATE TABLE readers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(100)        NOT NULL,
    last_name  VARCHAR(100)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE readers_books
(
    PRIMARY KEY (reader_id, book_id),
    reader_id INTEGER REFERENCES readers (id),
    book_id   INTEGER REFERENCES books (id)
);