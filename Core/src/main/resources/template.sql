CREATE TABLE if not exists `win` (
  `player` varchar(45) NOT NULL,
  `balance` bigint(19) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player`));

 CREATE TABLE if not exists `limit` (
   `player` varchar(45) NOT NULL,
   `time` bigint(19) NOT NULL DEFAULT '0',
   `balance` bigint(19) NOT NULL DEFAULT '0',
   PRIMARY KEY (`player`));