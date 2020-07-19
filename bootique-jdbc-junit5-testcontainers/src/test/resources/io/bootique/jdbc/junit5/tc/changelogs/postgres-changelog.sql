--liquibase formatted sql

--changeset postgres:create-schema
create table b (
    id integer not null primary key,
    name varchar(20)
);

--changeset postgres:insert_data
insert into b values (
  12, 'myname'
);

