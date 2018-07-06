CREATE DATABASE  IF NOT EXISTS `ontoschema` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `ontoschema`;
-- MySQL dump 10.13  Distrib 5.5.60, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: ontoschema
-- ------------------------------------------------------
-- Server version	5.5.60-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `XSD_type`
--

DROP TABLE IF EXISTS `XSD_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `XSD_type` (
  `XSD_URI` varchar(255) NOT NULL,
  `data_prop` varchar(255) NOT NULL,
  PRIMARY KEY (`XSD_URI`,`data_prop`),
  KEY `datatype_prop_idx` (`data_prop`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `class` (
  `class_URI` varchar(255) NOT NULL,
  `label` varchar(45) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `instance_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`class_URI`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset`
--

DROP TABLE IF EXISTS `dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dataset` (
  `dataset_name` varchar(255) NOT NULL,
  `version` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`dataset_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `endpoint`
--

DROP TABLE IF EXISTS `endpoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `endpoint` (
  `endpoint` varchar(255) NOT NULL,
  PRIMARY KEY (`endpoint`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hasDomain`
--

DROP TABLE IF EXISTS `hasDomain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hasDomain` (
  `class_URI` varchar(255) NOT NULL,
  `property_URI` varchar(255) NOT NULL,
  PRIMARY KEY (`class_URI`,`property_URI`),
  KEY `prop_idx` (`property_URI`),
  CONSTRAINT `domain_class` FOREIGN KEY (`class_URI`) REFERENCES `class` (`class_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `prop` FOREIGN KEY (`property_URI`) REFERENCES `property` (`property_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prefix`
--

DROP TABLE IF EXISTS `prefix`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prefix` (
  `prefix` varchar(255) NOT NULL,
  `count` int(11) DEFAULT NULL,
  PRIMARY KEY (`prefix`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `property`
--

DROP TABLE IF EXISTS `property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `property` (
  `property_URI` varchar(255) NOT NULL,
  `label` varchar(45) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `instance_count` int(20) DEFAULT NULL,
  `property_type` varchar(45) DEFAULT '',
  PRIMARY KEY (`property_URI`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subproperty`
--

DROP TABLE IF EXISTS `subproperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subproperty` (
  `property_URI` varchar(255) NOT NULL,
  `subproperty_URI` varchar(255) NOT NULL,
  PRIMARY KEY (`property_URI`,`subproperty_URI`),
  CONSTRAINT `has_parent_prop` FOREIGN KEY (`property_URI`) REFERENCES `property` (`property_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `superclass`
--

DROP TABLE IF EXISTS `superclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `superclass` (
  `class_URI` varchar(255) NOT NULL,
  `superclass_URI` varchar(255) NOT NULL,
  PRIMARY KEY (`class_URI`,`superclass_URI`),
  CONSTRAINT `has_parent_class` FOREIGN KEY (`class_URI`) REFERENCES `class` (`class_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `triple_type`
--

DROP TABLE IF EXISTS `triple_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `triple_type` (
  `class_URI` varchar(255) NOT NULL DEFAULT 'http://localhost/blank',
  `property_URI` varchar(255) NOT NULL,
  `property_triple_type` varchar(255) NOT NULL DEFAULT 'http://localhost/blank',
  `triple_type` varchar(45) NOT NULL DEFAULT '',
  `instance_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`class_URI`,`property_URI`,`property_triple_type`),
  KEY `triple_property_idx` (`property_URI`),
  CONSTRAINT `predicate` FOREIGN KEY (`property_URI`) REFERENCES `property` (`property_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `subject` FOREIGN KEY (`class_URI`) REFERENCES `class` (`class_URI`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'ontoschema'
--

--
-- Dumping routines for database 'ontoschema'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-07-01 22:53:08
