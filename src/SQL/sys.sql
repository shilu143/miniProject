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

CREATE TABLE UG_REQ (
    PC_CREDITS numeric(5,2),
    EC_CREDITS numeric(5,2),
    E_CREDITS numeric(5,2)
);


insert into event values(4, ARRAY[2020,1]);
insert into UG_REQ values(20.0, 20.0, 20.0);



SELECT SUM(LTP[1] + LTP[2] / 2) AS credits
FROM (
    SELECT t1.courseid, t2.batch, t2.ltp, t2.type, t1.grade
    FROM _2020csb1102 t1
    INNER JOIN course_catalog_cse t2
        ON t2.courseid = t1.courseid
) table1
INNER JOIN (
  SELECT courseid, MAX(batch) as max_value
  FROM course_catalog_cse table2
  WHERE batch <= 2024
  GROUP BY courseid
) subquery
ON table1.courseid = subquery.courseid
   AND table1.batch = subquery.max_value
WHERE table1.type = 'pc' AND table1.grade IS NOT NULL
    AND LOWER(table1.grade) <> 'f';













