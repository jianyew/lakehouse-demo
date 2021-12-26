USE tdemo;

ALTER TABLE message ADD IF NOT EXISTS PARTITION (event_date = '${hiveconf:event_date}');

INSERT OVERWRITE TABLE ctr_base PARTITION (event_date = '${hiveconf:event_date}')
-- replace DISTINCT with some other approach to make it faster
SELECT DISTINCT MD5(logger.device_id||event_type||event_common.`timestamp`||event_common.spm) AS unique_id
       ,event_type
       ,action_type
       ,event_common.`timestamp` AS ts
       ,logger.device_id
       ,logger.platform
       ,completion.ip_country_code
       ,completion.login_status
       ,split(event_common.spm, '\\.')[0] AS page
       ,page.object_id
       ,split(event_common.spm, '\\.')[1] AS module
       ,module.object_id
       ,module.list_index
       ,split(event_common.spm, '\\.')[2] AS item
       ,item.object_id
       ,exp.group_id AS exp_group_id
       ,exp.experiment_id AS exp_id
  FROM message LATERAL VIEW explode(event_common.experiments) lv AS exp
 WHERE event_date = '${hiveconf:event_date}'
   AND (duration > 1000 or duration is NULL)
;

-- [{a:b, c:d}, {a:e, c:f}] --> {b:d, e:f}