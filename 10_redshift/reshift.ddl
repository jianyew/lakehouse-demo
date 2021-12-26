-- redshift 
-- account: demo
-- Password: Awsome2020
DROP TABLE IF EXISTS ctr_base;
CREATE TABLE ctr_base (
  unique_id CHAR(64),
  event_type VARCHAR(32),
  action_type VARCHAR(32),
  ts BIGINT,
  device_id VARCHAR(64),
  platform VARCHAR(64),
  ip_country_code VARCHAR(16),
  login_status BOOLEAN,
  page VARCHAR(64),
  page_obj_id VARCHAR(64),
  module VARCHAR(64),
  module_obj_id VARCHAR(64),
  module_list_index INT,
  item VARCHAR(64),
  item_obj_id VARCHAR(64),
  exp_group_id INT,
  exp_id INT
) SORTKEY(
  event_type
)
;