-- Add column for SandboxStatus (RFS-284)
ALTER TABLE `sandbox`
    ADD COLUMN `status` ENUM('CREATED', 'INITIALIZING', 'READY', 'ERROR') NOT NULL;

-- Backfill status values
UPDATE `sandbox` SET `status` = 'READY';