-- Create table for entity Sandbox
DROP TABLE IF EXISTS `sandbox`;
CREATE TABLE `sandbox` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`version` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NOT NULL,
	`updated_by` varchar(320) DEFAULT NULL,
	`owner` bigint NOT NULL,
	`description` varchar(255) NOT NULL,
	`uuid` varchar(255) NOT NULL,
	primary key(`id`),
	CONSTRAINT sandbox_owner_fk FOREIGN KEY (`owner`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

