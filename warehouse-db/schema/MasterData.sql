--liquibase formatted sql

--changeset michaluk.michal:1.CartDefinition
create table warehouse.CartDefinition (
    refNo varchar(64) primary key,
    definition json not null
);

insert into warehouse.CartDefinition (refNo, definition)
       values ('900900', '["900901", "900902", "900903", "900904", "900905", "900906", "900907"]');
