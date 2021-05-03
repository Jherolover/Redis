package com.elite.Redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class RedisController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //
    @RequestMapping("/hello")
    public String hello(){
        return "hello,Springboot!";
    }
    //存值 取值
    @RequestMapping("/redis")
    public String getRedis(@RequestParam("name") String name){
        String s = null;
        System.out.println(name);
        stringRedisTemplate.opsForValue().set(name,"user1");
        String json =  stringRedisTemplate.opsForValue().get(name);
        System.out.println(json);
        return json;
    }
    /**
     * 扣减库存
     * 1.并发请求的时候会出现超卖的情况  必须考虑并发请求
     * 设置key值，资源必须同步，设置分布式锁
     * 设置key值的过期时间
     */
    @RequestMapping("/deduct_stock")
    public String deductStock(){
        String lockKey = "lockKey";
        String clientid = UUID.randomUUID().toString();
        try {
            //设置一个key值
            //Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, clientid);
            //设置过期时间
            //stringRedisTemplate.expire(lockKey,10,TimeUnit.SECONDS);
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, clientid,30,TimeUnit.SECONDS);
            if (!result) {
                return "error_code";
            }
            //获取库存数
            int stocknum = Integer.parseInt(stringRedisTemplate.opsForValue().get("stoknum"));
            if (stocknum > 0) {
                int realStock = stocknum - 1;
                stringRedisTemplate.opsForValue().set("stoknum", realStock + "");
                System.out.println("扣减成功,剩余库存" + realStock);
            } else {
                System.out.println("扣减失败，库存不足");
            }
        }finally {
            if(clientid.equals(stringRedisTemplate.opsForValue().get(lockKey))){
                stringRedisTemplate.delete(clientid);
            }
        }
        return "end";
    }
}
