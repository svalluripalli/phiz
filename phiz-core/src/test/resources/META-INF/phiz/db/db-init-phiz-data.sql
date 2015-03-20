/*==============================================================================
= TABLE: DESTINATIONS
==============================================================================*/
insert into destinations (
    dest_id,
    dest_uri
) values (
    '${phiz.dest.iis.dev.id}',
    '${phiz.dest.iis.dev.uri}'
);

insert into destinations (
    dest_id,
    dest_uri
) values (
    '${phiz.dest.iis.test.timeout.id}',
    '${phiz.dest.iis.test.timeout.uri}'
);

insert into destinations (
    dest_id,
    dest_uri
) values (
    '${phiz.dest.iis.test.unknown.host.id}',
    '${phiz.dest.iis.test.unknown.host.uri}'
);
