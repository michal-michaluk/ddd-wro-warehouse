
create schema warehouse authorization postgres;

create table warehouse.ProductStockHistory (
    id serial primary key,
    created timestamp default current_timestamp,
    refNo varchar(64),
    type varchar(128),
    content json
);

create index on warehouse.ProductStockHistory (refNo);
