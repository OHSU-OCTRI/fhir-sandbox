-- Add column for SandboxStatus, backfilling existing data (RFS-284)
ALTER TABLE `sandbox`
    ADD COLUMN `status` ENUM('CREATED', 'INITIALIZING', 'READY', 'ERROR') DEFAULT NULL;

UPDATE `sandbox` SET `status` = 'READY';

ALTER TABLE `sandbox`
    ADD COLUMN `status` ENUM('CREATED', 'INITIALIZING', 'READY', 'ERROR') NOT NULL;