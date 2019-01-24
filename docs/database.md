```
# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.6.40)
# Database: investment
# Generation Time: 2019-01-24 10:02:29 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table article
# ------------------------------------------------------------

DROP TABLE IF EXISTS `article`;

CREATE TABLE `article` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(50) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `content` text,
  `create_on` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;

INSERT INTO `article` (`id`, `title`, `image`, `content`, `create_on`)
VALUES
	(5,'最后罗列几点操盘经验（干货）','','<div><div>一：购买强势的品种！</div><br><div>二：股票，日内短线，重要的止损点：当天已出现的最低点；昨天的最低点；最低技术支撑点；近几天的最低支撑点；当天中已出现的从最低到最高50%的中间点；股市总体指数的新低点。</div><br><div>三：此外操盘笔记对每个人都很重要，以下附上他操盘笔记概略：</div><br><div>开市前：获取最新的金融信息，在本子上记录相关的股票名称和数据以便开市后操盘时参考。关注日本，欧洲，美国的重大新闻，哪些公司会在今天发布报告。有重要信息就在笔记本上简略记录，大多只看标题。</div><br><div>开盘后：“一幅图等于一千个词语”。抓紧记录值得日后复盘的操盘交易。</div><br><div>收市后：“假如你已经出手失利，请不要连学习的教训一起丢掉”。总结一下当日的交易看看有什么新经验教训值得今后参考。并为第二天的操盘做准备。</div></div>',1548233669);

/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table invest
# ------------------------------------------------------------

DROP TABLE IF EXISTS `invest`;

CREATE TABLE `invest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(6) NOT NULL,
  `type` int(10) DEFAULT NULL COMMENT '交易类型1买入2卖出',
  `price_buy` decimal(6,2) DEFAULT NULL,
  `price_cost` decimal(6,2) DEFAULT NULL,
  `transaction_time` int(11) DEFAULT NULL,
  `sell_drop_rate` decimal(4,2) DEFAULT NULL,
  `sell_up_rate` decimal(4,2) DEFAULT NULL,
  `strategy` varchar(200) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `create_on` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `invest` WRITE;
/*!40000 ALTER TABLE `invest` DISABLE KEYS */;

INSERT INTO `invest` (`id`, `code`, `type`, `price_buy`, `price_cost`, `transaction_time`, `sell_drop_rate`, `sell_up_rate`, `strategy`, `count`, `create_on`)
VALUES
	(1,'600703',1,10.48,10.53,1548124080,-10.00,30.00,'看好底部',100,1548242110);

/*!40000 ALTER TABLE `invest` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table invest_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `invest_log`;

CREATE TABLE `invest_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `invest_id` int(11) NOT NULL,
  `date` date DEFAULT NULL,
  `type` int(10) DEFAULT NULL COMMENT '交易类型1买入2卖出0无操作',
  `price` decimal(6,2) DEFAULT NULL,
  `rate` decimal(4,2) DEFAULT NULL,
  `comment` varchar(200) DEFAULT NULL,
  `create_on` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `invest_log` WRITE;
/*!40000 ALTER TABLE `invest_log` DISABLE KEYS */;

INSERT INTO `invest_log` (`id`, `invest_id`, `date`, `type`, `price`, `rate`, `comment`, `create_on`)
VALUES
	(1,1,'2019-01-21',1,10.48,0.00,'买入',1548242110),
	(4,1,'2019-01-22',0,10.28,-1.53,'震荡',1548295916),
	(5,1,'2019-01-23',0,10.13,-2.00,'买二胡',1548296173);

/*!40000 ALTER TABLE `invest_log` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table stock
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock`;

CREATE TABLE `stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(6) NOT NULL,
  `name` varchar(10) DEFAULT NULL,
  `join_price` double DEFAULT NULL,
  `create_on` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `stock` WRITE;
/*!40000 ALTER TABLE `stock` DISABLE KEYS */;

INSERT INTO `stock` (`id`, `code`, `name`, `join_price`, `create_on`)
VALUES
	(1,'600703','三安光电',10.28,1548233669),
	(2,'002041','登海种业',5.67,1548233669);

/*!40000 ALTER TABLE `stock` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


```