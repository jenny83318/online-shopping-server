CREATE TABLE `JOL_CustomerInfo` (
	`account` char(45) NOT NULL UNIQUE,
	`password` char(45) NOT NULL,
	`email` char(100) NOT NULL UNIQUE,
	`phone` char(45) NOT NULL,
	`address` char(150) NOT NULL,
	`status` BOOLEAN NOT NULL,
	`payment` char NOT NULL,
	PRIMARY KEY (`account`)
);

CREATE TABLE `JOL_ProductInfo` (
	`prodId` int(11) NOT NULL AUTO_INCREMENT,
	`name` char(50) NOT NULL,
	`descript` char(100),
	`img` blob NOT NULL,
	`createDt` DATETIME NOT NULL,
	`updateDt` DATETIME NOT NULL,
	`price` int NOT NULL,
	`cost` int,
	`qty` int,
	`category` char(20) NOT NULL,
	`size` char(50) NOT NULL,
	PRIMARY KEY (`prodId`)
);

CREATE TABLE `JOL_OrderInfo` (
	`orderNo` int NOT NULL AUTO_INCREMENT UNIQUE,
	`account` char(45) NOT NULL UNIQUE,
	`prodId` int NOT NULL,
	`qty` int NOT NULL,
	`price` int NOT NULL,
	`orderTime` DATETIME NOT NULL,
	`shipNo` char(20) NOT NULL,
	`status` char(20) NOT NULL,
	`updateDt` DATETIME NOT NULL,
	PRIMARY KEY (`orderNo`)
);

CREATE TABLE `JOL_CartInfo` (
	`cartId` int NOT NULL AUTO_INCREMENT,
	`prodId` int NOT NULL,
	`account` char(45) NOT NULL,
	`qty` int NOT NULL,
	`size` char(10) NOT NULL,
	`updateDt` DATETIME NOT NULL,
	PRIMARY KEY (`cartId`)
);

CREATE TABLE `JOL_Setting` (
	`type` char(45) NOT NULL,
	`keyName` char(45) NOT NULL,
	`value` char(50) NOT NULL,
	`description` char(100) NOT NULL,
	`crUser` char(45) NOT NULL,
	`crDt` DATETIME NOT NULL,
	`upUser` char(45) NOT NULL,
	`upDt` DATETIME NOT NULL,
	PRIMARY KEY (`type`,`keyName`)
);

CREATE TABLE `JOL_Employee` (
	`empNo` int NOT NULL AUTO_INCREMENT UNIQUE,
	`account` char(45) NOT NULL UNIQUE,
	`name` char(45) NOT NULL,
	`auth` int NOT NULL,
	`crUser` char(45) NOT NULL,
	`crDt` DATETIME NOT NULL,
	`upUser` char(45) NOT NULL,
	`upDt` DATETIME NOT NULL,
	`phone` char(20) NOT NULL,
	`email` char(50) NOT NULL,
	`address` char(150) NOT NULL,
	`password` char(45) NOT NULL,
	PRIMARY KEY (`empNo`)
);

ALTER TABLE `JOL_OrderInfo` ADD CONSTRAINT `JOL_OrderInfo_fk0` FOREIGN KEY (`account`) REFERENCES `JOL_CustomerInfo`(`account`);

ALTER TABLE `JOL_OrderInfo` ADD CONSTRAINT `JOL_OrderInfo_fk1` FOREIGN KEY (`prodId`) REFERENCES `JOL_ProductInfo`(`prodId`);

ALTER TABLE `JOL_CartInfo` ADD CONSTRAINT `JOL_CartInfo_fk0` FOREIGN KEY (`prodId`) REFERENCES `JOL_ProductInfo`(`prodId`);






