create table t1 (c1 INT, c2 timestamp);

create table t2 (
    id integer not null primary key,
    name varchar(20)
);

insert into t2 values (
  12, 'myname'
);

create table t3 (
    id integer not null primary key,
    name varchar(20)
);

create table t4 (
    id integer not null primary key,
    name varchar(20),
    a_id integer,
    FOREIGN KEY (a_id) REFERENCES t3 (id)
);

create table t5 (
    id integer not null primary key,
    c1 bigint,
    c2 decimal(10,3)
);

insert into t3 (id, name) values (
  10, 'myname'
);

insert into t4 (id, name, a_id) values (
  11, 'myname', 10
);
