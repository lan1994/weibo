package com.hx.util;
import org.apache.commons.collections.buffer.BlockingBuffer;
import redis.clients.jedis.*;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xinxulan on 2017/1/28.
 */

public class RedisTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("redis://localhost:6379/9");
        jedis.flushDB();
        jedis.set("kk","ss");
        System.out.print(jedis.get("kk"));

    }
}
