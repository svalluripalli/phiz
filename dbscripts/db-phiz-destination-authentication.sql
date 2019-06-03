/*==============================================================================
= TABLE: DESTINATIONS
==============================================================================*/
alter table destinations
    add column username varchar(256);
alter table destinations
    add column password varchar(256);

/*==============================================================================
= TABLE: ACCESS_CONTROL
==============================================================================*/
create table access_control
(
    id  int  not null primary key,
    src_id varchar(256) not null,
    dest_id  varchar(128)  not null
);