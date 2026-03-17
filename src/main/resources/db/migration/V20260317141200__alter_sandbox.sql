ALTER TABLE `sandbox`
    ADD COLUMN `server_partition_id` bigint NOT NULL AFTER `owner`,
    RENAME COLUMN `uuid` TO `server_partition_name`,
    ADD UNIQUE INDEX `uk_server_partition_id` (`server_partition_id`),
    ADD UNIQUE INDEX `uk_server_partition_name` (`server_partition_name`);