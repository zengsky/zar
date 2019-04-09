package com.zeng.zar;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.zeng.zar.model.User;

public class ZarTest {

    @Test
    public void test(){
        User user = User.load(1);
        assertNotNull(user);
        User user1 = User.find(1);
        assertNotNull(user1);
    }
    
    @Test
    public void testThread() throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(10);
        for(int i=0; i<10; i++){
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    User user = User.load(1);
                    System.out.println(user);
                    assertNotNull(user);
                    User user1 = User.find(1);
                    System.out.println(user1);
                    assertNotNull(user1);
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
    }
    
    @Test
    public void testLambda(){
        List<User> list = new ArrayList<>();
        Arrays.asList(1, 2, 3).forEach(n -> {
            User user = User.find(n);
            assertNotNull(user);
            list.add(user);
            User user1 = User.load(n);
            System.out.println(user1.getName());
            list.add(user1);
        });
        assertEquals(6, list.size());
        System.out.println(list);
    }
    
}
