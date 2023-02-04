DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE AIMS_USER (
    name varchar,
    id varchar,
    role varchar,
    dept varchar,
    email varchar,
    hashedpass varchar,
    PRIMARY KEY(id)
);

create or replace procedure CREATE_USER(
   name varchar,
   id varchar,
   role varchar,
   dept varchar,
   email varchar,
   hashedpass varchar
)
language plpgsql
as $$
begin
    insert into AIMS_USER values(name, id, role, dept, email, hashedpass);
end;
$$;







