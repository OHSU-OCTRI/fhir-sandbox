-- RFS-278 Create table for entity SmartLaunchContext
DROP TABLE IF EXISTS `smart_launch_context`;
CREATE TABLE `smart_launch_context` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`version` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NOT NULL,
	`updated_by` varchar(320) DEFAULT NULL,
	`opaque_id` varchar(255) NOT NULL,
	`client_id` varchar(255) NOT NULL,
	`attributes` JSON DEFAULT NULL,
	primary key(`id`),
	unique key `opaque_id_client_id_uk` (`opaque_id`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
