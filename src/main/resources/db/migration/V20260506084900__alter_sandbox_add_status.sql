ALTER TABLE `sandbox`
    ADD COLUMN `status` ENUM('INITIALIZING', 'READY', 'ERROR') DEFAULT NULL;

-- Backfill status values
UPDATE `sandbox` SET `status` = 'READY';