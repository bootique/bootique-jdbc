create table "a" (
    "id" integer not null primary key,
    "name" varchar(20)
);

create table "b" (
    "id" integer not null primary key,
    "name" varchar(20),
    "a_id" integer references "a" ("id")
);

insert into "a" ("id", "name") values (
  10, 'myname'
);

insert into "b" ("id", "name", "a_id") values (
  11, 'myname', 10
);

