/*==============================================================================
= TABLE: DESTINATIONS
==============================================================================*/
create table destinations (
    dest_id varchar(128) not null primary key,
    dest_uri varchar(1024) not null,
    username varchar(256),
    password varchar(256)
);

/*==============================================================================
= TABLE: ACCESS_CONTROL
==============================================================================*/
create table access_control (
    id  int  not null primary key,
    src_id varchar(256) not null,
    dest_id  varchar(128)  not null
);
