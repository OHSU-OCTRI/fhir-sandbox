-- Create table for entity Translation
DROP TABLE IF EXISTS `translation`;
CREATE TABLE `translation` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`version` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NOT NULL,
	`updated_by` varchar(320) DEFAULT NULL,
	`locale` varchar(255) DEFAULT NULL,
	`message_key` varchar(255) NOT NULL,
	`content` TEXT DEFAULT NULL,
	`description` TEXT DEFAULT NULL,
	`markup_allowed` bit(1) DEFAULT NULL,
	primary key(`id`),
	UNIQUE KEY `message_key_locale_uk` (`message_key`,`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
