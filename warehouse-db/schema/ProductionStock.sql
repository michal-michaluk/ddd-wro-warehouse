--liquibase formatted sql

--changeset michaluk.michal:1.init
drop table if exists warehouse.ProductStockHistory;
create table warehouse.ProductStockHistory (
    id serial primary key,
    created timestamp default current_timestamp,
    refNo varchar(64),
    type varchar(128),
    content json
);

create index on warehouse.ProductStockHistory (refNo);


--changeset michaluk.michal:2.adding.inStock
alter table warehouse.ProductStockHistory add column inStock boolean default true;
alter table warehouse.ProductStockHistory add column unit varchar(64);

create index on warehouse.ProductStockHistory (inStock, refNo);
create index on warehouse.ProductStockHistory (inStock, unit);
drop index warehouse.productstockhistory_refno_idx;
