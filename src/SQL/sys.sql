DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE _USER (
    id varchar,
    role varchar,
    pass varchar,
    PRIMARY KEY(id)
);

CREATE TABLE STUDENT (
    name varchar,
    id varchar,
    deptid varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE TABLE FACULTY (
    name varchar,
    id varchar,
    deptid varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE TABLE OFFICE (
    name varchar,
    id varchar,
    deptid varchar,
    email varchar,
    contact varchar,
    PRIMARY KEY(id)
);

CREATE TABLE DEPARTMENT (
    deptid varchar,
    deptname varchar,
    PRIMARY KEY(deptid)
);

CREATE TABLE EVENT (
    _event integer,
    _session integer[2]
);

CREATE TABLE UG_REQ (
    PC_CREDITS numeric(5,2),
    EC_CREDITS numeric(5,2),
    E_CREDITS numeric(5,2),
    SEM1 numeric(5,2),
    SEM2 numeric(5,2),
    MULT numeric(5,2)
);

insert into event values(0, ARRAY[2020,1]);
insert into UG_REQ values(20.0, 20.0, 20.0, 18, 18, 1.25);

















