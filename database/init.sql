
-- users table
CREATE TABLE `users` (
	`id` VARCHAR(36) NOT NULL,
	`name` VARCHAR(255) NOT NULL,
	`login` VARCHAR(100) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`active` BOOLEAN NOT NULL DEFAULT true,
	PRIMARY KEY (`id`)
);

-- refresh_token table
CREATE TABLE `refresh_tokens` (
	`jti` VARCHAR(36) NOT NULL,
	`token` TEXT NOT NULL,
	`expiry` DATETIME NOT NULL,
	PRIMARY KEY (`jti`)
);
