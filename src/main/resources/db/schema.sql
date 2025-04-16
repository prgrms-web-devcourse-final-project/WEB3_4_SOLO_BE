-- product_subscriptions 테이블의 외래 키 제약 조건 삭제
ALTER TABLE product_subscriptions DROP FOREIGN KEY IF EXISTS FKnqjv01564yb5hfpf4r2vjsgiu;
ALTER TABLE product_subscriptions DROP FOREIGN KEY IF EXISTS FKk11kgr9jhumn66wi5oexhna23;
ALTER TABLE product_subscriptions DROP FOREIGN KEY IF EXISTS FKi2o0vg6djvevgqsln8uioc5bp;

-- product_subscriptions 테이블 삭제
DROP TABLE IF EXISTS product_subscriptions; 