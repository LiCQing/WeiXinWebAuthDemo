/*
Navicat MySQL Data Transfer

Source Server         : test
Source Server Version : 50520
Source Host           : localhost:3306
Source Database       : db_ads

Target Server Type    : MYSQL
Target Server Version : 50520
File Encoding         : 65001

Date: 2020-05-31 10:46:33
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `tb_user`
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `pass_word` varchar(255) DEFAULT NULL,
  `open_id` varchar(255) DEFAULT NULL,
  `user_status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES ('1', 'o_yRexKKEXFyYAXpncMwZgUiTEVw', '别晃我的可乐', null, '0');
INSERT INTO `tb_user` VALUES ('2', 'o_yRexKKEXFyYAXpncMwZgUiTEVw', '别晃我的可乐', null, '0');
INSERT INTO `tb_user` VALUES ('3', 'user1', '123456', 'o_yRexKKEXFyYAXpncMwZgUiTEVw', '0');
