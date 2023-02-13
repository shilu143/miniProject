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

CREATE TABLE COURSE_CATALOG (
    id varchar,
    name varchar,
    ltp integer[3],
    prereq varchar[],
    PRIMARY KEY(id)
);

CREATE TABLE Y1_OFFERING (
    courseid varchar,
    cgcriteria real,
    fid varchar[]
);

CREATE TABLE Y2_OFFERING (
    courseid varchar,
    cgcriteria real,
    fid varchar[]
);

CREATE TABLE Y3_OFFERING (
    courseid varchar,
    cgcriteria real,
    fid varchar[]
);

CREATE TABLE Y4_OFFERING (
    courseid varchar,
    cgcriteria real,
    fid varchar[]
);

CREATE TABLE EVENT (
    _event integer,
    _session integer[2]
);

insert into event values(0, ARRAY[2023,1]);












