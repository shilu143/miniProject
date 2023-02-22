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

CREATE TABLE EVENT (
    _event integer,
    _session integer[2]
);

insert into event values(4, ARRAY[2020,1]);

--select t1.courseid , t2.coursename, t2.ltp, t2.prereq, t2.type, t1.cgcriteria, t3.name as Instructor
--from y1_cse_offering t1
--inner join course_catalog_cse t2 on t1.courseid = t2.courseid
--inner join faculty t3 on t1.fid=t3.id;











