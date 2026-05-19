-- Drop the unique key constraint, allowing smart clients with the same client ID (RFS-258)
ALTER TABLE `smart_client`
    DROP INDEX `client_id`;