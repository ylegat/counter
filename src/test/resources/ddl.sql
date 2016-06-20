create table event(
  id INT PRIMARY KEY,
  aggregate_id INT,
  version INT,
  event VARCHAR(256)
);

alter table event add constraint u_event unique(aggregate_id, version);