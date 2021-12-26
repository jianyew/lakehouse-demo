COPY ctr_base
FROM 's3://aws-tdemo-deployment-test/processed_data/ctr_base/event_date=2020-02-23'
iam_role 'arn:aws:iam::964479626419:role/redshift-s3copy-role'
FORMAT AS parquet
;


SELECT a.page
       ,a.module
       ,1.0 * action/exposure
  FROM (SELECT page
               ,module
               ,count(DISTINCT unique_id) AS exposure
          FROM ctr_base
         WHERE event_type = 'exposure'
         GROUP BY 1,2
        ) a
  LEFT JOIN (SELECT page
                    ,module
                    -- ,item
                    ,count(DISTINCT unique_id) AS action
               FROM ctr_base
              WHERE event_type = 'action'
              GROUP BY 1,2
            ) b
         ON a.page = b.page
        AND a.module = b.module
;


case when exp_id = 12 and group_id = 34 then 'treatment'
when exp_id = 12 and group_id = -1 then 'control'
when exp_id !=12 then 'others'
end as exp
count(distinct unique_id)