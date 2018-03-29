/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pintu
 */
@Disabled
public class TempAndHumidRestTest {

    static Client client;
    static WebTarget target;

    @BeforeAll
    public static void setUp() {
        client = ClientBuilder.newClient();
    }

    @BeforeEach
    public void beforeTest() {
        target = client.target("http://192.168.1.3:8090");
    }

    @Test
    @Disabled("true")
    public void testing1() {
        target = client.target("http://192.168.1.3:8090");
        target = target.path("testing");
        Response response = target.request().get();
        Assertions.assertEquals(200, response.getStatus());
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    @Test
    @Disabled
    public void testing2() {
        target = client.target("http://192.168.1.3:8090");
        target = target.path("tempHumid");
        target = target.queryParam("start", System.currentTimeMillis());
        System.out.println(new Date());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date());
                target = target.queryParam("end", System.currentTimeMillis());
                Response response = target.request().get();
                Assertions.assertEquals(200, response.getStatus());
                System.out.println(response.getStatus());
                System.out.println(response.readEntity(String.class));
            }
        }, 120000);
    }
}
