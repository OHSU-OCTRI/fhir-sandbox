-- Create table for entity SmartClient
DROP TABLE IF EXISTS `smart_client`;
CREATE TABLE `smart_client` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`version` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NOT NULL,
	`updated_by` varchar(320) DEFAULT NULL,
	`sandbox` bigint NOT NULL,
	`client_type` enum('CONFIDENTIAL', 'PUBLIC') NOT NULL,
	`client_id` varchar(255) NOT NULL,
	`description` varchar(255) NOT NULL,
	`launch_uri` varchar(255) NOT NULL,
	`redirect_uris` varchar(255) NOT NULL,
	`scopes` varchar(255) NOT NULL,
	primary key(`id`),
	CONSTRAINT smart_client_sandbox_fk FOREIGN KEY (`sandbox`) REFERENCES `sandbox` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

