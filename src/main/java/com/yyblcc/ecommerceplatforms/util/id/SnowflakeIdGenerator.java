package com.yyblcc.ecommerceplatforms.util.id;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * 雪花算法ID生成器
 * 64位ID结构：0-时间戳(41位)-机器ID(10位)-序列号(12位)
 */
@Component
@Slf4j
public class SnowflakeIdGenerator {

    // 开始时间戳 (2024-01-01 00:00:00)
    private static final long START_TIMESTAMP = 1704067200000L;

    // 机器ID所占位数
    private static final long WORKER_ID_BITS = 10L;

    // 序列号所占位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    // 序列号最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 机器ID左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    // 时间戳左移位数
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    // 机器ID
    private long workerId;

    // 序列号
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 随机数生成器
    private final Random random = new Random();

    /**
     * 初始化机器ID
     */
    @PostConstruct
    public void init() {
        try {
            // 尝试根据网卡MAC地址生成机器ID
            workerId = generateWorkerIdByMac();
        } catch (Exception e) {
            log.warn("Failed to generate worker id by MAC address, using random worker id", e);
            // 如果失败，使用随机数
            workerId = random.nextLong() % (MAX_WORKER_ID + 1);
        }
        
        if (workerId < 0) {
            workerId = -workerId;
        }
        
        log.info("SnowflakeIdGenerator initialized with workerId: {}", workerId);
    }

    /**
     * 根据网卡MAC地址生成机器ID
     */
    private long generateWorkerIdByMac() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null && mac.length > 0) {
                long id = 0;
                for (byte b : mac) {
                    id |= ((long) (b & 0xFF)) << 8 * (mac.length - 1);
                }
                return id & MAX_WORKER_ID;
            }
        }
        // 如果没有找到网卡，使用主机名的哈希值
        String hostName = InetAddress.getLocalHost().getHostName();
        return hostName.hashCode() & MAX_WORKER_ID;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 如果当前时间戳小于上次生成ID的时间戳，说明时钟回拨
        if (timestamp < lastTimestamp) {
            log.warn("Clock moved backwards. Refusing to generate id for {} milliseconds", lastTimestamp - timestamp);
            // 等待到上次时间戳之后
            timestamp = lastTimestamp;
        }

        // 如果是同一时间戳，则序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 如果序列号达到最大值，则等待下一个时间戳
            if (sequence == 0) {
                timestamp = nextTimestamp(lastTimestamp);
            }
        } else {
            // 新的时间戳，重置序列号
            sequence = 0L;
        }

        // 更新上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 组合ID：时间戳部分 + 机器ID部分 + 序列号部分
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) 
                | (workerId << WORKER_ID_SHIFT) 
                | sequence;
    }

    /**
     * 获取下一个时间戳
     */
    private long nextTimestamp(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}