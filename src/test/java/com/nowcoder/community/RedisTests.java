package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"name","katniss");
        redisTemplate.opsForHash().put(redisKey,"age",24);

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"name"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"age"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets(){
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey,"111","222","333","444","555");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 70);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 60);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"悟空"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"八戒"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
    }

    @Test
    public void testKeys(){
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);

    }

    // 多次访问同一个Key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);

        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // 编程式事务
    @Test
    public void testTransaction(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();

                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"lisi");
                operations.opsForSet().add(redisKey,"wangwu");

                System.out.println(operations.opsForSet().members(redisKey));
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
