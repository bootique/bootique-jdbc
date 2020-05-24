create table b (
    id integer not null primary key,
    name varchar(20)
);
--
CREATE OR REPLACE FUNCTION insert_procedure() RETURNS void
AS $$
begin
  INSERT INTO b (id, name) values (55, 'xxx');
  end;
$$
LANGUAGE PLPGSQL
--
CREATE OR REPLACE FUNCTION insert_procedure() RETURNS void
AS $$
begin
  INSERT INTO b (id, name) values (66, 'yyy');
  end;
$$
LANGUAGE PLPGSQL

