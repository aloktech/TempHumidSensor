/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.imos.th.sensor.SensorData;
import com.imos.th.sensor.TempAndHumidService;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pintu
 */
public class TempAndHumidHazelcastTest {

    private static TempAndHumidService service;
    private static HazelcastInstance instance;

    @BeforeAll
    public static void setUp() {
        service = TempAndHumidService.getInstance();
        service.config();
        instance = service.getHazelCastInstance();
    }

    @AfterAll
    public static void shutDown() {
        instance.shutdown();
    }

    @AfterEach
    public void clear() {
        IMap<Long, SensorData> dataMap = instance.getMap("tempHumid");
        dataMap.clear();
        instance.shutdown();
    }

    @DisplayName("Test One")
    @Test
    public void testing1() {
        IMap<Long, SensorData> dataMap = instance.getMap("tempHumid");
        Assertions.assertTrue(dataMap.isEmpty());
    }

    @DisplayName("Test Two")
    @Test
    public void testing2() {
        IMap<Long, SensorData> dataMap = instance.getMap("tempHumid");
        Assertions.assertTrue(dataMap.isEmpty());
        dataMap.put(System.currentTimeMillis(), new SensorData());
        Assertions.assertFalse(dataMap.isEmpty());
    }

    @DisplayName("Test Three")
    @Test
    public void testing3() {
        IMap<Long, SensorData> dataMap = instance.getMap("tempHumid");
        Assertions.assertTrue(dataMap.isEmpty());
        long time = System.currentTimeMillis();
        dataMap.put(time, new SensorData());
        Assertions.assertFalse(dataMap.isEmpty());
        Predicate criteriaQuery = Predicates.equal("time", time);
        Collection<SensorData> data = dataMap.values(criteriaQuery);
        Assertions.assertFalse(data.isEmpty());
        System.out.println(data);
    }

    @DisplayName("Test Four")
    @Test
    public void testing4() {
        IMap<Long, SensorData> dataMap = instance.getMap("tempHumid");
        Assertions.assertTrue(dataMap.isEmpty());
        long time1 = System.currentTimeMillis();
        dataMap.put(System.currentTimeMillis(), new SensorData());
        Assertions.assertFalse(dataMap.isEmpty());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dataMap.put(System.currentTimeMillis(), new SensorData());
                long time2 = System.currentTimeMillis();
                Predicate criteriaQuery = Predicates.between("time", time1, time2);
                Collection<SensorData> data = dataMap.values(criteriaQuery);
                Assertions.assertFalse(data.isEmpty());
                Assertions.assertTrue(data.size() == 2);
                System.out.println(data);
            }
        }, 10000);
    }

}
