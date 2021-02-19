-- MySQL dump 10.16  Distrib 10.1.47-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: sbdb
-- ------------------------------------------------------
-- Server version	10.1.47-MariaDB-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alliance`
--

DROP TABLE IF EXISTS `alliance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alliance` (
  `idAlliance` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(30) NOT NULL,
  `name` varchar(30) NOT NULL,
  PRIMARY KEY (`idAlliance`),
  UNIQUE KEY `UK_h7jfng3csi7xy8d1r3dqe07lo` (`code`),
  UNIQUE KEY `UK_7nuq4ufi5qsmpn1u6i8n2nxot` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alliance`
--

LOCK TABLES `alliance` WRITE;
/*!40000 ALTER TABLE `alliance` DISABLE KEYS */;
INSERT INTO `alliance` VALUES (1,'A','Argonauten'),(2,'111er','111er');
/*!40000 ALTER TABLE `alliance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `building`
--

DROP TABLE IF EXISTS `building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `building` (
  `idBuilding` int(11) NOT NULL AUTO_INCREMENT,
  `baseValue` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `increasingFactorPerLevel` decimal(19,2) DEFAULT NULL,
  `name` varchar(30) DEFAULT NULL,
  `resourceType` varchar(255) DEFAULT NULL,
  `idCosts` int(11) DEFAULT NULL,
  `idResearch` int(11) NOT NULL,
  PRIMARY KEY (`idBuilding`),
  KEY `FK5vart3g8xv4gkgagwxxwyiuqi` (`idCosts`),
  KEY `FKbp0gn3eiexsa5p6s20md9yfi7` (`idResearch`),
  CONSTRAINT `FK5vart3g8xv4gkgagwxxwyiuqi` FOREIGN KEY (`idCosts`) REFERENCES `resourceDeposit` (`idResourceDeposit`),
  CONSTRAINT `FKbp0gn3eiexsa5p6s20md9yfi7` FOREIGN KEY (`idResearch`) REFERENCES `research` (`idResearch`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `building`
--

LOCK TABLES `building` WRITE;
/*!40000 ALTER TABLE `building` DISABLE KEYS */;
INSERT INTO `building` VALUES (1,10,'The construction yard construct constructions.',0.20,'Construction Yard','CONSTRUCTION',21,1),(2,10,'The construction yard construct orbital constructions.',0.20,'Orbitals Construction Yard','ORBITALCONSTRUCTION',22,2),(3,10,'The lab investigates researches.',0.20,'Research Laboratories','RESEARCH',23,3),(4,10,'The market makes money.',0.20,'Market place','CREDITS',24,4),(5,10,'Metals for progress.',0.20,'Metal works','METALORE',25,5),(6,10,'Better metals for more progress.',0.20,'Special orbital ores','MERCURIUM',26,6),(7,10,'The clock works creates time.',0.20,'Asynchronous Investigations','HYPERONIUM',27,7);
/*!40000 ALTER TABLE `building` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `construction`
--

DROP TABLE IF EXISTS `construction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `construction` (
  `idConstruction` int(11) NOT NULL AUTO_INCREMENT,
  `level` int(11) NOT NULL,
  `idBuilding` int(11) NOT NULL,
  `idPlanet` int(11) NOT NULL,
  PRIMARY KEY (`idConstruction`),
  UNIQUE KEY `UK8c4oqqvxa4xpl5rmgoafhpc69` (`idPlanet`,`idBuilding`),
  KEY `FKlkteuncyf95jg9hhq28yefrcl` (`idBuilding`),
  CONSTRAINT `FKg139setxu2ng9hj6h7sgpyb9s` FOREIGN KEY (`idPlanet`) REFERENCES `planet` (`idPlanet`),
  CONSTRAINT `FKlkteuncyf95jg9hhq28yefrcl` FOREIGN KEY (`idBuilding`) REFERENCES `building` (`idBuilding`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `construction`
--

LOCK TABLES `construction` WRITE;
/*!40000 ALTER TABLE `construction` DISABLE KEYS */;
INSERT INTO `construction` VALUES (1,1,1,1),(2,1,1,2);
/*!40000 ALTER TABLE `construction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fleet`
--

DROP TABLE IF EXISTS `fleet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fleet` (
  `idFleet` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `idPlanet` int(11) DEFAULT NULL,
  `idStarsystem` int(11) DEFAULT NULL,
  `idOwner` int(11) NOT NULL,
  `idResourceDeposit` int(11) DEFAULT NULL,
  PRIMARY KEY (`idFleet`),
  KEY `FKh6yguwrqsu1kah359o77c1b8h` (`idPlanet`),
  KEY `FK9p7bc2hmluuk682gxv6pfk1ve` (`idStarsystem`),
  KEY `FKjo66qwgl0a9bba5x7xq23fvok` (`idOwner`),
  KEY `FKckq55cmimjpois3mst803atuy` (`idResourceDeposit`),
  CONSTRAINT `FK9p7bc2hmluuk682gxv6pfk1ve` FOREIGN KEY (`idStarsystem`) REFERENCES `starsystem` (`idStarsystem`),
  CONSTRAINT `FKckq55cmimjpois3mst803atuy` FOREIGN KEY (`idResourceDeposit`) REFERENCES `resourceDeposit` (`idResourceDeposit`),
  CONSTRAINT `FKh6yguwrqsu1kah359o77c1b8h` FOREIGN KEY (`idPlanet`) REFERENCES `planet` (`idPlanet`),
  CONSTRAINT `FKjo66qwgl0a9bba5x7xq23fvok` FOREIGN KEY (`idOwner`) REFERENCES `user` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fleet`
--

LOCK TABLES `fleet` WRITE;
/*!40000 ALTER TABLE `fleet` DISABLE KEYS */;
/*!40000 ALTER TABLE `fleet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fleetcomposition`
--

DROP TABLE IF EXISTS `fleetcomposition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fleetcomposition` (
  `idFleet` int(11) NOT NULL,
  `amount` int(11) DEFAULT NULL,
  `idShipClass` int(11) NOT NULL,
  PRIMARY KEY (`idFleet`,`idShipClass`),
  KEY `FK5gqfc5h0bjidbw1g27dm6p5vn` (`idShipClass`),
  CONSTRAINT `FK5gqfc5h0bjidbw1g27dm6p5vn` FOREIGN KEY (`idShipClass`) REFERENCES `shipclass` (`idShipclass`),
  CONSTRAINT `FK8xjjuy4dvxqwloaaf4wge42qw` FOREIGN KEY (`idFleet`) REFERENCES `fleet` (`idFleet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fleetcomposition`
--

LOCK TABLES `fleetcomposition` WRITE;
/*!40000 ALTER TABLE `fleetcomposition` DISABLE KEYS */;
/*!40000 ALTER TABLE `fleetcomposition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hull`
--

DROP TABLE IF EXISTS `hull`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hull` (
  `idHull` int(11) NOT NULL AUTO_INCREMENT,
  `constructionCapacity` int(11) NOT NULL,
  `description` varchar(255) NOT NULL,
  `level` int(11) NOT NULL,
  `name` varchar(30) NOT NULL,
  `idCosts` int(11) DEFAULT NULL,
  `idResearch` int(11) NOT NULL,
  PRIMARY KEY (`idHull`),
  KEY `FK65udyybp7syxvga5evxn8olhc` (`idCosts`),
  KEY `FK4hpf1pawl0wynjx9kdg74opea` (`idResearch`),
  CONSTRAINT `FK4hpf1pawl0wynjx9kdg74opea` FOREIGN KEY (`idResearch`) REFERENCES `research` (`idResearch`),
  CONSTRAINT `FK65udyybp7syxvga5evxn8olhc` FOREIGN KEY (`idCosts`) REFERENCES `resourceDeposit` (`idResourceDeposit`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hull`
--

LOCK TABLES `hull` WRITE;
/*!40000 ALTER TABLE `hull` DISABLE KEYS */;
INSERT INTO `hull` VALUES (1,50,'The corvette hull',1,'Corvette vessel',34,14),(2,100,'The frigate hull',1,'Frigate vessel',35,15),(3,150,'The cruiser hull',1,'Cruiser vessel',36,16);
/*!40000 ALTER TABLE `hull` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `job` (
  `idJob` int(11) NOT NULL AUTO_INCREMENT,
  `amountShips` int(11) DEFAULT NULL,
  `resourceType` varchar(255) DEFAULT NULL,
  `targetLevel` int(11) DEFAULT NULL,
  `jobDoneAtZero` decimal(19,2) NOT NULL,
  `idBuilding` int(11) DEFAULT NULL,
  `idResearch` int(11) DEFAULT NULL,
  `idShipclass` int(11) DEFAULT NULL,
  `idFacility` int(11) DEFAULT NULL,
  `idOwner` int(11) NOT NULL,
  PRIMARY KEY (`idJob`),
  UNIQUE KEY `UK970jvv9t5arj4vbk44ygd9nrp` (`idFacility`),
  KEY `FK7otfjvk4vhy0gt0m3hnyam6au` (`idBuilding`),
  KEY `FKdno72guom99osq9f36eixsd87` (`idResearch`),
  KEY `FKir289ws5tvge6hdcbbwtpw0gv` (`idShipclass`),
  KEY `FK3urqlpl2jmbxlfk4q88i9i5tb` (`idOwner`),
  CONSTRAINT `FK3urqlpl2jmbxlfk4q88i9i5tb` FOREIGN KEY (`idOwner`) REFERENCES `user` (`idUser`),
  CONSTRAINT `FK4ewa76co5drr08nptgdmax8d6` FOREIGN KEY (`idFacility`) REFERENCES `construction` (`idConstruction`),
  CONSTRAINT `FK7otfjvk4vhy0gt0m3hnyam6au` FOREIGN KEY (`idBuilding`) REFERENCES `building` (`idBuilding`),
  CONSTRAINT `FKdno72guom99osq9f36eixsd87` FOREIGN KEY (`idResearch`) REFERENCES `research` (`idResearch`),
  CONSTRAINT `FKir289ws5tvge6hdcbbwtpw0gv` FOREIGN KEY (`idShipclass`) REFERENCES `shipclass` (`idShipclass`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job`
--

LOCK TABLES `job` WRITE;
/*!40000 ALTER TABLE `job` DISABLE KEYS */;
/*!40000 ALTER TABLE `job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module` (
  `idModule` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `level` int(11) NOT NULL,
  `moduleType` int(11) NOT NULL,
  `name` varchar(30) NOT NULL,
  `useCapacity` int(11) NOT NULL,
  `value` int(11) NOT NULL,
  `idCosts` int(11) DEFAULT NULL,
  `idResearch` int(11) NOT NULL,
  PRIMARY KEY (`idModule`),
  KEY `FKqxpwocsv3vwcws3g1yj7hpw8i` (`idCosts`),
  KEY `FK52hbj88ddt0mvoq1jv1rf5vk1` (`idResearch`),
  CONSTRAINT `FK52hbj88ddt0mvoq1jv1rf5vk1` FOREIGN KEY (`idResearch`) REFERENCES `research` (`idResearch`),
  CONSTRAINT `FKqxpwocsv3vwcws3g1yj7hpw8i` FOREIGN KEY (`idCosts`) REFERENCES `resourceDeposit` (`idResourceDeposit`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module`
--

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;
INSERT INTO `module` VALUES (1,'A laser',1,0,'Laser Mk I',5,10,28,8),(2,'An armor',1,1,'Armor Mk I',5,10,29,9),(3,'A shield',1,2,'Shield Mk I',5,10,30,10),(4,'A drive',1,3,'Speed Mk I',5,10,31,11),(5,'A FTL drive',1,4,'FTL Speed Mk I',5,10,32,12),(6,'A scanner',1,5,'Scanner Mk I',5,10,33,13);
/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modulecomposition`
--

DROP TABLE IF EXISTS `modulecomposition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modulecomposition` (
  `idShipclass` int(11) NOT NULL,
  `amount` int(11) DEFAULT NULL,
  `idModule` int(11) NOT NULL,
  PRIMARY KEY (`idShipclass`,`idModule`),
  KEY `FKcudmav236bb3nh619ye8gcp9p` (`idModule`),
  CONSTRAINT `FKcudmav236bb3nh619ye8gcp9p` FOREIGN KEY (`idModule`) REFERENCES `module` (`idModule`),
  CONSTRAINT `FKnclvtmoo3ftvkkaf45fpr8fpc` FOREIGN KEY (`idShipclass`) REFERENCES `shipclass` (`idShipclass`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modulecomposition`
--

LOCK TABLES `modulecomposition` WRITE;
/*!40000 ALTER TABLE `modulecomposition` DISABLE KEYS */;
INSERT INTO `modulecomposition` VALUES (1,1,1),(1,1,2),(1,1,3),(1,1,4),(1,1,5),(1,1,6),(2,2,1),(2,2,2),(2,2,3),(2,2,4),(2,2,5),(2,1,6),(3,3,1),(3,3,2),(3,3,3),(3,2,4),(3,2,5),(3,1,6),(4,1,1),(4,1,2),(4,1,3),(4,1,4),(4,1,5),(4,1,6),(5,2,1),(5,2,2),(5,2,3),(5,2,4),(5,2,5),(5,1,6),(6,3,1),(6,3,2),(6,3,3),(6,2,4),(6,2,5),(6,1,6);
/*!40000 ALTER TABLE `modulecomposition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `move`
--

DROP TABLE IF EXISTS `move`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `move` (
  `idMove` int(11) NOT NULL AUTO_INCREMENT,
  `moveDoneAtZero` int(11) NOT NULL,
  `idFleet` int(11) NOT NULL,
  `idUser` int(11) NOT NULL,
  `startIdPlanet` int(11) DEFAULT NULL,
  `startIdStarsystem` int(11) DEFAULT NULL,
  `targetIdPlanet` int(11) DEFAULT NULL,
  `targetIdStarsystem` int(11) DEFAULT NULL,
  PRIMARY KEY (`idMove`),
  KEY `FKg65nht3m74odamnrqiv1cdyl6` (`idFleet`),
  KEY `FKm0l3o2yx8pq8hu2bww8maoa98` (`idUser`),
  KEY `FKa1bs79m293x3ok5ose0jli0r9` (`startIdPlanet`),
  KEY `FKnyt7l8mioxwrvahwb9p86kbq0` (`startIdStarsystem`),
  KEY `FKfhqgwhapcw4i2ydno4u1qlq77` (`targetIdPlanet`),
  KEY `FK7ttvnhp04l6htir72n61xkffj` (`targetIdStarsystem`),
  CONSTRAINT `FK7ttvnhp04l6htir72n61xkffj` FOREIGN KEY (`targetIdStarsystem`) REFERENCES `starsystem` (`idStarsystem`),
  CONSTRAINT `FKa1bs79m293x3ok5ose0jli0r9` FOREIGN KEY (`startIdPlanet`) REFERENCES `planet` (`idPlanet`),
  CONSTRAINT `FKfhqgwhapcw4i2ydno4u1qlq77` FOREIGN KEY (`targetIdPlanet`) REFERENCES `planet` (`idPlanet`),
  CONSTRAINT `FKg65nht3m74odamnrqiv1cdyl6` FOREIGN KEY (`idFleet`) REFERENCES `fleet` (`idFleet`),
  CONSTRAINT `FKm0l3o2yx8pq8hu2bww8maoa98` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`),
  CONSTRAINT `FKnyt7l8mioxwrvahwb9p86kbq0` FOREIGN KEY (`startIdStarsystem`) REFERENCES `starsystem` (`idStarsystem`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `move`
--

LOCK TABLES `move` WRITE;
/*!40000 ALTER TABLE `move` DISABLE KEYS */;
/*!40000 ALTER TABLE `move` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `planet`
--

DROP TABLE IF EXISTS `planet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `planet` (
  `idPlanet` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `xCoordinate` int(11) NOT NULL,
  `yCoordinate` int(11) NOT NULL,
  `idOwner` int(11) DEFAULT NULL,
  `idRescourcedeposits` int(11) DEFAULT NULL,
  `idRescourcefactors` int(11) DEFAULT NULL,
  `idStarsystem` int(11) DEFAULT NULL,
  PRIMARY KEY (`idPlanet`),
  UNIQUE KEY `UKdv40vo9ta4ir5vsolqropht2r` (`idStarsystem`,`idPlanet`,`xCoordinate`,`yCoordinate`),
  KEY `FKobjb6jgxji3jrrgoxy9r30uyc` (`idOwner`),
  KEY `FKefjg37nip5q3p67hxedb485n4` (`idRescourcedeposits`),
  KEY `FKk3ha0bjd77n9thg57b6u1tue0` (`idRescourcefactors`),
  CONSTRAINT `FKefjg37nip5q3p67hxedb485n4` FOREIGN KEY (`idRescourcedeposits`) REFERENCES `resourceDeposit` (`idResourceDeposit`),
  CONSTRAINT `FKk3ha0bjd77n9thg57b6u1tue0` FOREIGN KEY (`idRescourcefactors`) REFERENCES `resourceDeposit` (`idResourceDeposit`),
  CONSTRAINT `FKn5q9kybjning6d8qag1rfutvr` FOREIGN KEY (`idStarsystem`) REFERENCES `starsystem` (`idStarsystem`),
  CONSTRAINT `FKobjb6jgxji3jrrgoxy9r30uyc` FOREIGN KEY (`idOwner`) REFERENCES `user` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `planet`
--

LOCK TABLES `planet` WRITE;
/*!40000 ALTER TABLE `planet` DISABLE KEYS */;
INSERT INTO `planet` VALUES (1,'Argonauten HQ',1,1,1,1,2,1),(2,'111er HQ',2,2,2,3,4,2);
/*!40000 ALTER TABLE `planet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rescources`
--

DROP TABLE IF EXISTS `rescources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rescources` (
  `idResourceDeposit` int(11) NOT NULL,
  `amount` decimal(19,2) DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  PRIMARY KEY (`idResourceDeposit`,`type`),
  CONSTRAINT `FK1g6ky1b4jtewtsbt384qpc5qi` FOREIGN KEY (`idResourceDeposit`) REFERENCES `resourceDeposit` (`idResourceDeposit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rescources`
--

LOCK TABLES `rescources` WRITE;
/*!40000 ALTER TABLE `rescources` DISABLE KEYS */;
INSERT INTO `rescources` VALUES (1,1500.00,'CONSTRUCTION'),(1,1400.00,'CREDITS'),(1,1300.00,'HYPERONIUM'),(1,3800.00,'MERCURIUM'),(1,4500.00,'METALORE'),(1,4300.00,'ORBITALCONSTRUCTION'),(1,3600.00,'RESEARCH'),(2,120.00,'CONSTRUCTION'),(2,71.00,'CREDITS'),(2,140.00,'HYPERONIUM'),(2,120.00,'MERCURIUM'),(2,55.00,'METALORE'),(2,170.00,'ORBITALCONSTRUCTION'),(2,97.00,'RESEARCH'),(3,3600.00,'CONSTRUCTION'),(3,2700.00,'CREDITS'),(3,2000.00,'HYPERONIUM'),(3,1000.00,'MERCURIUM'),(3,1900.00,'METALORE'),(3,3500.00,'ORBITALCONSTRUCTION'),(3,4200.00,'RESEARCH'),(4,110.00,'CONSTRUCTION'),(4,150.00,'CREDITS'),(4,160.00,'HYPERONIUM'),(4,130.00,'MERCURIUM'),(4,150.00,'METALORE'),(4,160.00,'ORBITALCONSTRUCTION'),(4,180.00,'RESEARCH'),(5,45.00,'CONSTRUCTION'),(5,36.00,'CREDITS'),(5,38.00,'HYPERONIUM'),(5,13.00,'MERCURIUM'),(5,34.00,'METALORE'),(5,22.00,'ORBITALCONSTRUCTION'),(5,15.00,'RESEARCH'),(6,27.00,'CONSTRUCTION'),(6,42.00,'CREDITS'),(6,15.00,'HYPERONIUM'),(6,34.00,'MERCURIUM'),(6,40.00,'METALORE'),(6,33.00,'ORBITALCONSTRUCTION'),(6,49.00,'RESEARCH'),(7,36.00,'CONSTRUCTION'),(7,41.00,'CREDITS'),(7,26.00,'HYPERONIUM'),(7,48.00,'MERCURIUM'),(7,22.00,'METALORE'),(7,20.00,'ORBITALCONSTRUCTION'),(7,32.00,'RESEARCH'),(8,31.00,'CONSTRUCTION'),(8,45.00,'CREDITS'),(8,47.00,'HYPERONIUM'),(8,45.00,'MERCURIUM'),(8,45.00,'METALORE'),(8,17.00,'ORBITALCONSTRUCTION'),(8,49.00,'RESEARCH'),(9,43.00,'CONSTRUCTION'),(9,49.00,'CREDITS'),(9,25.00,'HYPERONIUM'),(9,13.00,'MERCURIUM'),(9,35.00,'METALORE'),(9,17.00,'ORBITALCONSTRUCTION'),(9,37.00,'RESEARCH'),(10,41.00,'CONSTRUCTION'),(10,13.00,'CREDITS'),(10,26.00,'HYPERONIUM'),(10,50.00,'MERCURIUM'),(10,30.00,'METALORE'),(10,42.00,'ORBITALCONSTRUCTION'),(10,20.00,'RESEARCH'),(11,37.00,'CONSTRUCTION'),(11,10.00,'CREDITS'),(11,23.00,'HYPERONIUM'),(11,22.00,'MERCURIUM'),(11,37.00,'METALORE'),(11,38.00,'ORBITALCONSTRUCTION'),(11,12.00,'RESEARCH'),(12,23.00,'CONSTRUCTION'),(12,48.00,'CREDITS'),(12,47.00,'HYPERONIUM'),(12,32.00,'MERCURIUM'),(12,45.00,'METALORE'),(12,27.00,'ORBITALCONSTRUCTION'),(12,16.00,'RESEARCH'),(13,18.00,'CONSTRUCTION'),(13,34.00,'CREDITS'),(13,34.00,'HYPERONIUM'),(13,30.00,'MERCURIUM'),(13,47.00,'METALORE'),(13,25.00,'ORBITALCONSTRUCTION'),(13,15.00,'RESEARCH'),(14,15.00,'CONSTRUCTION'),(14,31.00,'CREDITS'),(14,27.00,'HYPERONIUM'),(14,37.00,'MERCURIUM'),(14,49.00,'METALORE'),(14,23.00,'ORBITALCONSTRUCTION'),(14,33.00,'RESEARCH'),(15,43.00,'CONSTRUCTION'),(15,11.00,'CREDITS'),(15,33.00,'HYPERONIUM'),(15,45.00,'MERCURIUM'),(15,30.00,'METALORE'),(15,21.00,'ORBITALCONSTRUCTION'),(15,29.00,'RESEARCH'),(16,45.00,'CONSTRUCTION'),(16,46.00,'CREDITS'),(16,14.00,'HYPERONIUM'),(16,46.00,'MERCURIUM'),(16,29.00,'METALORE'),(16,17.00,'ORBITALCONSTRUCTION'),(16,27.00,'RESEARCH'),(17,19.00,'CONSTRUCTION'),(17,12.00,'CREDITS'),(17,34.00,'HYPERONIUM'),(17,45.00,'MERCURIUM'),(17,22.00,'METALORE'),(17,25.00,'ORBITALCONSTRUCTION'),(17,23.00,'RESEARCH'),(18,33.00,'CONSTRUCTION'),(18,13.00,'CREDITS'),(18,23.00,'HYPERONIUM'),(18,14.00,'MERCURIUM'),(18,22.00,'METALORE'),(18,17.00,'ORBITALCONSTRUCTION'),(18,19.00,'RESEARCH'),(19,11.00,'CONSTRUCTION'),(19,44.00,'CREDITS'),(19,14.00,'HYPERONIUM'),(19,45.00,'MERCURIUM'),(19,46.00,'METALORE'),(19,22.00,'ORBITALCONSTRUCTION'),(19,31.00,'RESEARCH'),(20,20.00,'CONSTRUCTION'),(20,31.00,'CREDITS'),(20,15.00,'HYPERONIUM'),(20,25.00,'MERCURIUM'),(20,44.00,'METALORE'),(20,15.00,'ORBITALCONSTRUCTION'),(20,48.00,'RESEARCH'),(21,32.00,'CONSTRUCTION'),(21,36.00,'CREDITS'),(21,47.00,'HYPERONIUM'),(21,34.00,'MERCURIUM'),(21,34.00,'METALORE'),(21,16.00,'ORBITALCONSTRUCTION'),(21,24.00,'RESEARCH'),(22,26.00,'CONSTRUCTION'),(22,43.00,'CREDITS'),(22,21.00,'HYPERONIUM'),(22,20.00,'MERCURIUM'),(22,23.00,'METALORE'),(22,19.00,'ORBITALCONSTRUCTION'),(22,39.00,'RESEARCH'),(23,38.00,'CONSTRUCTION'),(23,34.00,'CREDITS'),(23,45.00,'HYPERONIUM'),(23,39.00,'MERCURIUM'),(23,40.00,'METALORE'),(23,14.00,'ORBITALCONSTRUCTION'),(23,10.00,'RESEARCH'),(24,45.00,'CONSTRUCTION'),(24,34.00,'CREDITS'),(24,46.00,'HYPERONIUM'),(24,22.00,'MERCURIUM'),(24,26.00,'METALORE'),(24,39.00,'ORBITALCONSTRUCTION'),(24,23.00,'RESEARCH'),(25,13.00,'CONSTRUCTION'),(25,47.00,'CREDITS'),(25,30.00,'HYPERONIUM'),(25,43.00,'MERCURIUM'),(25,40.00,'METALORE'),(25,16.00,'ORBITALCONSTRUCTION'),(25,28.00,'RESEARCH'),(26,16.00,'CONSTRUCTION'),(26,17.00,'CREDITS'),(26,38.00,'HYPERONIUM'),(26,38.00,'MERCURIUM'),(26,16.00,'METALORE'),(26,49.00,'ORBITALCONSTRUCTION'),(26,46.00,'RESEARCH'),(27,41.00,'CONSTRUCTION'),(27,44.00,'CREDITS'),(27,41.00,'HYPERONIUM'),(27,50.00,'MERCURIUM'),(27,20.00,'METALORE'),(27,16.00,'ORBITALCONSTRUCTION'),(27,23.00,'RESEARCH'),(28,17.00,'CONSTRUCTION'),(28,44.00,'CREDITS'),(28,11.00,'HYPERONIUM'),(28,15.00,'MERCURIUM'),(28,24.00,'METALORE'),(28,24.00,'ORBITALCONSTRUCTION'),(28,33.00,'RESEARCH'),(29,33.00,'CONSTRUCTION'),(29,14.00,'CREDITS'),(29,23.00,'HYPERONIUM'),(29,47.00,'MERCURIUM'),(29,20.00,'METALORE'),(29,50.00,'ORBITALCONSTRUCTION'),(29,28.00,'RESEARCH'),(30,47.00,'CONSTRUCTION'),(30,14.00,'CREDITS'),(30,37.00,'HYPERONIUM'),(30,37.00,'MERCURIUM'),(30,11.00,'METALORE'),(30,19.00,'ORBITALCONSTRUCTION'),(30,34.00,'RESEARCH'),(31,44.00,'CONSTRUCTION'),(31,32.00,'CREDITS'),(31,22.00,'HYPERONIUM'),(31,20.00,'MERCURIUM'),(31,20.00,'METALORE'),(31,40.00,'ORBITALCONSTRUCTION'),(31,20.00,'RESEARCH'),(32,42.00,'CONSTRUCTION'),(32,40.00,'CREDITS'),(32,37.00,'HYPERONIUM'),(32,25.00,'MERCURIUM'),(32,22.00,'METALORE'),(32,43.00,'ORBITALCONSTRUCTION'),(32,14.00,'RESEARCH'),(33,11.00,'CONSTRUCTION'),(33,20.00,'CREDITS'),(33,29.00,'HYPERONIUM'),(33,33.00,'MERCURIUM'),(33,22.00,'METALORE'),(33,49.00,'ORBITALCONSTRUCTION'),(33,45.00,'RESEARCH'),(34,27.00,'CONSTRUCTION'),(34,41.00,'CREDITS'),(34,13.00,'HYPERONIUM'),(34,16.00,'MERCURIUM'),(34,14.00,'METALORE'),(34,28.00,'ORBITALCONSTRUCTION'),(34,16.00,'RESEARCH'),(35,17.00,'CONSTRUCTION'),(35,18.00,'CREDITS'),(35,11.00,'HYPERONIUM'),(35,18.00,'MERCURIUM'),(35,34.00,'METALORE'),(35,13.00,'ORBITALCONSTRUCTION'),(35,43.00,'RESEARCH'),(36,45.00,'CONSTRUCTION'),(36,41.00,'CREDITS'),(36,27.00,'HYPERONIUM'),(36,37.00,'MERCURIUM'),(36,37.00,'METALORE'),(36,15.00,'ORBITALCONSTRUCTION'),(36,36.00,'RESEARCH'),(37,50.00,'CONSTRUCTION'),(37,49.00,'CREDITS'),(37,48.00,'HYPERONIUM'),(37,29.00,'MERCURIUM'),(37,27.00,'METALORE'),(37,16.00,'ORBITALCONSTRUCTION'),(37,45.00,'RESEARCH'),(38,27.00,'CONSTRUCTION'),(38,30.00,'CREDITS'),(38,18.00,'HYPERONIUM'),(38,11.00,'MERCURIUM'),(38,22.00,'METALORE'),(38,10.00,'ORBITALCONSTRUCTION'),(38,13.00,'RESEARCH'),(39,20.00,'CONSTRUCTION'),(39,15.00,'CREDITS'),(39,46.00,'HYPERONIUM'),(39,38.00,'MERCURIUM'),(39,41.00,'METALORE'),(39,45.00,'ORBITALCONSTRUCTION'),(39,20.00,'RESEARCH'),(40,19.00,'CONSTRUCTION'),(40,37.00,'CREDITS'),(40,35.00,'HYPERONIUM'),(40,12.00,'MERCURIUM'),(40,12.00,'METALORE'),(40,48.00,'ORBITALCONSTRUCTION'),(40,14.00,'RESEARCH'),(41,27.00,'CONSTRUCTION'),(41,36.00,'CREDITS'),(41,10.00,'HYPERONIUM'),(41,18.00,'MERCURIUM'),(41,40.00,'METALORE'),(41,35.00,'ORBITALCONSTRUCTION'),(41,23.00,'RESEARCH'),(42,12.00,'CONSTRUCTION'),(42,16.00,'CREDITS'),(42,20.00,'HYPERONIUM'),(42,22.00,'MERCURIUM'),(42,47.00,'METALORE'),(42,16.00,'ORBITALCONSTRUCTION'),(42,14.00,'RESEARCH');
/*!40000 ALTER TABLE `rescources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `research`
--

DROP TABLE IF EXISTS `research`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `research` (
  `idResearch` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `levelCap` int(11) NOT NULL,
  `name` varchar(30) DEFAULT NULL,
  `idCosts` int(11) DEFAULT NULL,
  `unlockedThrough` int(11) DEFAULT NULL,
  PRIMARY KEY (`idResearch`),
  KEY `FKni50te130dndarqgicsq3svhb` (`idCosts`),
  KEY `FKch37eb44iv0ls442yu7usvvtp` (`unlockedThrough`),
  CONSTRAINT `FKch37eb44iv0ls442yu7usvvtp` FOREIGN KEY (`unlockedThrough`) REFERENCES `research` (`idResearch`),
  CONSTRAINT `FKni50te130dndarqgicsq3svhb` FOREIGN KEY (`idCosts`) REFERENCES `resourceDeposit` (`idResourceDeposit`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `research`
--

LOCK TABLES `research` WRITE;
/*!40000 ALTER TABLE `research` DISABLE KEYS */;
INSERT INTO `research` VALUES (1,'The construction yard research researches the construction yard.',1,'Construction Yard',5,NULL),(2,'The orbitals Construction Yard research researches the orbitals construction yard.',1,'Orbitals Construction Yard',6,NULL),(3,'The laboratories research researches laboratories.',1,'Laboratories',7,NULL),(4,'The Market place research researches Market places.',1,'Market place',8,NULL),(5,'The Metal works research researches Metal works.',1,'Metal works',9,NULL),(6,'The Special orbital ores research researches Special orbital ores.',1,'Special orbital ores',10,5),(7,'The Asynchronous Investigations research researches Asynchronous Investigations.',1,'Asynchronous Investigations',11,6),(8,'The Laser research researches Lasers.',1,'Laser',12,NULL),(9,'The Armor research researches Armors.',1,'Armor',13,NULL),(10,'The Shield research researches Shields.',1,'Shield',14,NULL),(11,'The Speed research researches sublight propulsion.',1,'Speed',15,NULL),(12,'The FTL Speed research researches FTL propulsion.',1,'FTL Speed',16,NULL),(13,'The Scanner research researches Scanners.',1,'Scanner',17,NULL),(14,'The Corvette research researches Corvettes.',1,'Corvette',18,NULL),(15,'The Frigate research researches Frigates.',1,'Frigate',19,NULL),(16,'The Cruiser research researches Cruisers.',1,'Cruiser',20,15);
/*!40000 ALTER TABLE `research` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resourceDeposit`
--

DROP TABLE IF EXISTS `resourceDeposit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resourceDeposit` (
  `idResourceDeposit` int(11) NOT NULL AUTO_INCREMENT,
  `subType` varchar(255) NOT NULL,
  PRIMARY KEY (`idResourceDeposit`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resourceDeposit`
--

LOCK TABLES `resourceDeposit` WRITE;
/*!40000 ALTER TABLE `resourceDeposit` DISABLE KEYS */;
INSERT INTO `resourceDeposit` VALUES (1,'DEPOSITS'),(2,'MININGFACTORS'),(3,'DEPOSITS'),(4,'MININGFACTORS'),(5,'COSTS'),(6,'COSTS'),(7,'COSTS'),(8,'COSTS'),(9,'COSTS'),(10,'COSTS'),(11,'COSTS'),(12,'COSTS'),(13,'COSTS'),(14,'COSTS'),(15,'COSTS'),(16,'COSTS'),(17,'COSTS'),(18,'COSTS'),(19,'COSTS'),(20,'COSTS'),(21,'COSTS'),(22,'COSTS'),(23,'COSTS'),(24,'COSTS'),(25,'COSTS'),(26,'COSTS'),(27,'COSTS'),(28,'COSTS'),(29,'COSTS'),(30,'COSTS'),(31,'COSTS'),(32,'COSTS'),(33,'COSTS'),(34,'COSTS'),(35,'COSTS'),(36,'COSTS'),(37,'COSTS'),(38,'COSTS'),(39,'COSTS'),(40,'COSTS'),(41,'COSTS'),(42,'COSTS');
/*!40000 ALTER TABLE `resourceDeposit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipclass`
--

DROP TABLE IF EXISTS `shipclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipclass` (
  `idShipclass` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `raceType` varchar(255) NOT NULL,
  `idCosts` int(11) DEFAULT NULL,
  `idHull` int(11) NOT NULL,
  `idOwner` int(11) NOT NULL,
  PRIMARY KEY (`idShipclass`),
  UNIQUE KEY `UKhgcmmw0vvkvg6511jjpuw6bws` (`idOwner`,`name`),
  KEY `FK1ruka89wdh2fw4x3e3kasjg7l` (`idCosts`),
  KEY `FKb1t7hnfcn96sywd3vyqv9xdyc` (`idHull`),
  CONSTRAINT `FK1ruka89wdh2fw4x3e3kasjg7l` FOREIGN KEY (`idCosts`) REFERENCES `resourceDeposit` (`idResourceDeposit`),
  CONSTRAINT `FKb1t7hnfcn96sywd3vyqv9xdyc` FOREIGN KEY (`idHull`) REFERENCES `hull` (`idHull`),
  CONSTRAINT `FKpx7092ewe0n8g9hu56dhqp7ip` FOREIGN KEY (`idOwner`) REFERENCES `user` (`idUser`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipclass`
--

LOCK TABLES `shipclass` WRITE;
/*!40000 ALTER TABLE `shipclass` DISABLE KEYS */;
INSERT INTO `shipclass` VALUES (1,'Argonauts corvette','HUMAN',37,1,1),(2,'Argonauts frigate','HUMAN',38,2,1),(3,'Argonauts cruiser','HUMAN',39,3,1),(4,'111er corvette','KANDORIAN',40,1,2),(5,'111er frigate','KANDORIAN',41,2,2),(6,'111er cruiser','KANDORIAN',42,3,2);
/*!40000 ALTER TABLE `shipclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `starsystem`
--

DROP TABLE IF EXISTS `starsystem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `starsystem` (
  `idStarsystem` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `xCoordinate` int(11) NOT NULL,
  `yCoordinate` int(11) NOT NULL,
  PRIMARY KEY (`idStarsystem`),
  UNIQUE KEY `UKt4lv9qo63hlsg9mbs7mddql8h` (`xCoordinate`,`yCoordinate`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `starsystem`
--

LOCK TABLES `starsystem` WRITE;
/*!40000 ALTER TABLE `starsystem` DISABLE KEYS */;
INSERT INTO `starsystem` VALUES (1,'Argonaut',1,1),(2,'111',2,2);
/*!40000 ALTER TABLE `starsystem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tick`
--

DROP TABLE IF EXISTS `tick`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tick` (
  `idTick` int(11) NOT NULL AUTO_INCREMENT,
  `tickEnds` datetime(6) DEFAULT NULL,
  `tickStarts` datetime(6) NOT NULL,
  PRIMARY KEY (`idTick`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tick`
--

LOCK TABLES `tick` WRITE;
/*!40000 ALTER TABLE `tick` DISABLE KEYS */;
INSERT INTO `tick` VALUES (1,'2021-02-20 15:10:37.000000','2021-02-20 15:10:37.000000');
/*!40000 ALTER TABLE `tick` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unlockedResearch`
--

DROP TABLE IF EXISTS `unlockedResearch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unlockedResearch` (
  `idUser` int(11) NOT NULL,
  `level` int(11) DEFAULT NULL,
  `idResearch` int(11) NOT NULL,
  PRIMARY KEY (`idUser`,`idResearch`),
  KEY `FKc4x693khs2f17y0jjfb625o51` (`idResearch`),
  CONSTRAINT `FKc4x693khs2f17y0jjfb625o51` FOREIGN KEY (`idResearch`) REFERENCES `research` (`idResearch`),
  CONSTRAINT `FKigikopnlfckk76o2yo3utm5s9` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unlockedResearch`
--

LOCK TABLES `unlockedResearch` WRITE;
/*!40000 ALTER TABLE `unlockedResearch` DISABLE KEYS */;
INSERT INTO `unlockedResearch` VALUES (1,1,1),(1,1,2),(1,1,3),(1,1,4),(1,1,5),(2,1,1),(2,1,2),(2,1,3),(2,1,4),(2,1,5);
/*!40000 ALTER TABLE `unlockedResearch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `idUser` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `raceType` varchar(255) NOT NULL,
  `username` varchar(30) NOT NULL,
  `idAlliance` int(11) DEFAULT NULL,
  PRIMARY KEY (`idUser`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`),
  KEY `FKd0120p7tkvssh9r8hldenpw1w` (`idAlliance`),
  CONSTRAINT `FKd0120p7tkvssh9r8hldenpw1w` FOREIGN KEY (`idAlliance`) REFERENCES `alliance` (`idAlliance`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'mail','test','HUMAN','Flashkid',1),(2,'mail','test','KANDORIAN','Yufiel',2);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-02-20 15:10:54
