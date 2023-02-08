DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE _USER (
    id varchar,
    role varchar,
    hashedpass varchar,
    PRIMARY KEY(id)
);

CREATE TABLE STUDENT (
    name varchar,
    id varchar,
    dept varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE TABLE FACULTY (
    name varchar,
    id varchar,
    dept varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE TABLE OFFICE (
    name varchar,
    id varchar,
    dept varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE OR REPLACE PROCEDURE CREATE_USER(
   id varchar,
   role varchar,
   hashedpass varchar
)
LANGUAGE plpgsql
AS $$
    BEGIN
        INSERT INTO _USER VALUES(id, role, hashedpass);
    END;
$$;









