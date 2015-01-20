/*==============================================================================
= TABLE: DESTINATIONS
==============================================================================*/
create table destinations (
    dest_id varchar(128) not null primary key,
    dest_uri varchar(1024) not null
);
