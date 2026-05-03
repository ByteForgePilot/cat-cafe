-- ============================================
-- 猫咖点单系统 - 数据库初始化脚本
-- MySQL 8.0+
-- 命名规范: 字段名统一使用 camelCase（小驼峰）
-- 表名统一使用小写单数（orders/likes 用复数避免保留字冲突）
-- ============================================

CREATE DATABASE IF NOT EXISTS cat_cafe
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cat_cafe;

-- ============================================
-- 1. user 用户表
-- 用户类型 userType 区分顾客(0)和管理员(1)，不另建子表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `userId`       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `userPassword` VARCHAR(255)     NOT NULL                 COMMENT '密码',
    `userName`     VARCHAR(50)      NOT NULL                 COMMENT '用户名',
    `userType`     TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '用户类型: 0=顾客, 1=管理员',
    `gender`       TINYINT UNSIGNED DEFAULT NULL             COMMENT '性别: 1=男, 2=女, NULL=未设置',
    `birthday`     DATE             DEFAULT NULL             COMMENT '生日',
    `registerTime` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`userId`),
    UNIQUE KEY `uk_userName` (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. catinformation 猫咪信息表
-- 所有字段均 NOT NULL，保证猫咪信息完整性
-- ============================================
CREATE TABLE IF NOT EXISTS `catinformation` (
    `catId`       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '猫咪ID',
    `catName`     VARCHAR(50)      NOT NULL                 COMMENT '猫咪名字',
    `breed`       VARCHAR(50)      NOT NULL                 COMMENT '品种',
    `birthday`    DATE             NOT NULL                 COMMENT '生日',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1       COMMENT '状态: 0=休息中, 1=在岗中',
    `personality` TEXT             NOT NULL                 COMMENT '性格描述',
    `photoUrl`    VARCHAR(255)     NOT NULL                 COMMENT '照片URL',
    `notes`       TEXT             NOT NULL                 COMMENT '备注',
    PRIMARY KEY (`catId`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪信息表';

-- ============================================
-- 3. product 商品表
--
-- 【设计说明】
-- status: 上架(1)=对外展示可购买, 下架(0)=隐藏但保留数据
-- 有历史订单的商品不能直接删除(外键保护)，应先下架再视情况删除
-- description 唯一可空字段
-- ============================================
CREATE TABLE IF NOT EXISTS `product` (
    `productId`     BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '商品ID',
    `productName`   VARCHAR(100)     NOT NULL                 COMMENT '商品名称',
    `category`      TINYINT UNSIGNED NOT NULL                 COMMENT '分类: 0=服务, 1=餐饮, 2=猫咪用品',
    `price`         DECIMAL(10,2)    NOT NULL                 COMMENT '价格',
    `stockQuantity` INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '库存数量',
    `imageUrl`      VARCHAR(255)     NOT NULL                 COMMENT '图片URL',
    `description`   TEXT             DEFAULT NULL             COMMENT '描述',
    `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1       COMMENT '状态: 0=已下架, 1=在售',
    `createTime`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`productId`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 4. orders 订单表
--
-- 【设计说明 - 多商品下单策略】
-- 一个订单记录只对应一个商品（productId + productQuantity）。
-- 前端允许一次下单多个商品时，处理方式:
--   1. 前端生成一个 batchNo（如时间戳+用户ID）
--   2. 每个商品拆成一条 orders 记录，写入相同的 batchNo
--   3. 查询时 WHERE batchNo = 'xxx' 聚合为一个购物车展示
--   4. 取消/支付时按 batchNo 批量更新 orderStatus
-- 单商品下单时 batchNo 为 NULL，和原来逻辑完全兼容。
-- userPhone / userName 是下单时的快照，用户后续修改资料不影响历史订单
-- ============================================
CREATE TABLE IF NOT EXISTS `orders` (
    `orderId`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    `batchNo`         VARCHAR(32)      DEFAULT NULL             COMMENT '批次号，同一批下单的多条记录共用',
    `userId`          BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID',
    `productId`       BIGINT UNSIGNED  NOT NULL                 COMMENT '商品ID',
    `productQuantity` INT UNSIGNED     NOT NULL DEFAULT 1       COMMENT '商品数量',
    `totalAmount`     DECIMAL(10,2)    NOT NULL                 COMMENT '订单总金额',
    `orderStatus`     TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '订单状态: 0=未支付, 1=已支付, 2=待拿取, 3=已完成, 4=已取消',
    `paymentMethod`   TINYINT UNSIGNED DEFAULT NULL             COMMENT '支付方式: 0=微信, 1=支付宝, 2=现金',
    `orderTime`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `paymentTime`     DATETIME         DEFAULT NULL             COMMENT '支付时间',
    `completionTime`  DATETIME         DEFAULT NULL             COMMENT '完成时间',
    `userPhone`       VARCHAR(20)      NOT NULL                 COMMENT '下单时手机号(快照)',
    `userName`        VARCHAR(50)      NOT NULL                 COMMENT '下单时用户名(快照)',
    `orderNote`       VARCHAR(500)     DEFAULT NULL             COMMENT '订单备注',
    PRIMARY KEY (`orderId`),
    KEY `idx_batchNo` (`batchNo`),
    KEY `idx_userId` (`userId`),
    KEY `idx_productId` (`productId`),
    KEY `idx_orderStatus` (`orderStatus`),
    KEY `idx_orderTime` (`orderTime`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_product` FOREIGN KEY (`productId`) REFERENCES `product` (`productId`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 5. comment 评论表
--
-- 【设计说明】
-- targetType 区分评论对象: 0=商品, 1=猫咪，targetId 为对应表的ID
-- 这样一张表同时支持商品评论和猫咪评论，和 likes 表的设计保持一致
-- auditStatus: 评论需要审核后才展示，防止不良内容
-- ============================================
CREATE TABLE IF NOT EXISTS `comment` (
    `commentId`   BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
    `targetType`  TINYINT UNSIGNED NOT NULL                 COMMENT '评论对象: 0=商品, 1=猫咪',
    `targetId`    BIGINT UNSIGNED  NOT NULL                 COMMENT '目标ID(商品或猫咪的ID)',
    `userId`      BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID',
    `content`     TEXT             NOT NULL                 COMMENT '评论内容',
    `publishTime` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `auditStatus` TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    PRIMARY KEY (`commentId`),
    KEY `idx_target` (`targetType`, `targetId`),
    KEY `idx_userId` (`userId`),
    KEY `idx_auditStatus` (`auditStatus`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 6. likes 点赞表
--
-- 【设计说明】
-- linkUrl: 查看"我的点赞"时，用于跳转到被点赞对象的详情页
-- uk_user_like 联合唯一索引: 同一用户不能对同一对象重复点赞
-- objectId 为软引用(无外键)，因为可能指向 product/comment/catinformation 三张表
-- ============================================
CREATE TABLE IF NOT EXISTS `likes` (
    `likeId`     BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '点赞ID',
    `likeType`   TINYINT UNSIGNED NOT NULL                 COMMENT '点赞类型: 0=商品, 1=评论, 2=猫咪',
    `objectId`   BIGINT UNSIGNED  NOT NULL                 COMMENT '被点赞对象ID',
    `userId`     BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID',
    `linkUrl`    VARCHAR(255)     DEFAULT NULL             COMMENT '跳转链接(查看点赞时跳转到对应物品)',
    `createTime` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`likeId`),
    UNIQUE KEY `uk_user_like` (`userId`, `likeType`, `objectId`),
    KEY `idx_likeType_object` (`likeType`, `objectId`),
    CONSTRAINT `fk_likes_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
