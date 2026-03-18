-- Drop in reverse FK order so constraints are satisfied
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS agencies;

CREATE TABLE IF NOT EXISTS agencies (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name     TEXT    NOT NULL,
    username TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS trips (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    tourist_attraction TEXT    NOT NULL,
    transport_company  TEXT    NOT NULL,
    departure_time     TEXT    NOT NULL,
    price              REAL    NOT NULL,
    available_seats    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name     TEXT    NOT NULL,
    customer_phone    TEXT    NOT NULL,
    number_of_tickets INTEGER NOT NULL,
    trip_id           INTEGER NOT NULL REFERENCES trips(id),
    agency_id         INTEGER NOT NULL REFERENCES agencies(id)
);