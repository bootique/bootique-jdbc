--liquibase formatted sql

--changeset derby:create-schema
create table "b" (
    "id" integer not null primary key,
    "name" varchar(20)
);

--changeset derby:insert_data
insert into "b" ("id", "name") values (
  10, 'myname'
);

