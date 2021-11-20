create table `t1` (`c1` INT, `c2` datetime);

create table `t2` (
    `id` integer not null primary key,
    `name` varchar(20)
);

create table `t5` (
    `id` integer not null primary key,
    `c1` bigint,
    `c2` decimal(10,3)
);

insert into t2 values (
  12, 'myname'
);


