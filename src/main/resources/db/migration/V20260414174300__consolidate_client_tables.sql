-- RFS-277 Consolidate Client Tables
DROP TABLE IF EXISTS `oauth2_client`;

DROP TABLE IF EXISTS `smart_client`;
CREATE TABLE `smart_client` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`version` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NOT NULL,
	`updated_by` varchar(320) DEFAULT NULL,
	`sandbox` bigint NOT NULL,
	`client_type` enum('CONFIDENTIAL', 'PUBLIC') NOT NULL,
	`client_id` varchar(255) NOT NULL UNIQUE,
	`name` varchar(255) NOT NULL,
	`description` varchar(1000) DEFAULT NULL,
	`launch_uri` varchar(1000) NOT NULL,
	`redirect_uris` TEXT,
	`post_logout_redirect_uris` TEXT,
	`scopes` TEXT,
	`client_secret` varchar(255) DEFAULT NULL,
	`client_secret_expires_at` datetime DEFAULT NULL,
	`client_authentication_methods` varchar(255) NOT NULL,
	`authorization_grant_types` varchar(255) NOT NULL,
	`client_settings` TEXT,
	`token_settings` TEXT,
	primary key(`id`),
	CONSTRAINT smart_client_sandbox_fk FOREIGN KEY (`sandbox`) REFERENCES `sandbox` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;