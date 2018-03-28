/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.th.sensor;

import com.alibaba.fastjson.JSON;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import static com.imos.th.sensor.TempAndHumidConstant.START;
import static com.imos.th.sensor.TempAndHumidConstant.END;
import static com.imos.th.sensor.TempAndHumidConstant.TEMP_HUMID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;

/**
 *
 * @author Pintu
 */
public class TempAndHumidServiceProvider {

    public static void main(String[] args) {
        TempAndHumidService service = TempAndHumidService.getInstance();
        service.config();
        service.executeService();
        service.saveToLocalDB();

        port(8090);
        options("/*", (req, res) -> {
            String accessControlRequestHeaders = req
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers",
                        accessControlRequestHeaders);
            }

            String accessControlRequestMethod = req
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods",
                        accessControlRequestMethod);
            }

            return "OK";
        });
        get("/testing", (req, res) -> {
            return "Hello World: " + LocalDateTime.now();
        });
        get("/tempHumid", (req, res) -> {
            long start = 0, end = 0;
            Collection<SensorData> data = new ArrayList<>();
            String message = TempAndHumidConstant.EMPTY;
            try {
                start = Long.parseLong(req.queryParams(START));
                end = Long.parseLong(req.queryParams(END));
                data = queryForData(start, end);
            } catch (NumberFormatException e) {
                message = "Invalid range: " + start + " " + end + " : " + LocalDateTime.now();
            }
            String resultMsg;
            if (data.isEmpty()) {
                res.status(400);
                resultMsg = "Invalid range: " + start + " " + end + " : " + LocalDateTime.now();
            } else {
                res.status(202);
                resultMsg = JSON.toJSONString(data);
            }
            return resultMsg;
        });

    }

    private static Collection<SensorData> queryForData(long start, long end) {
        Collection<SensorData> data;
        IMap<Long, SensorData> sensorData = TempAndHumidService.getInstance().getHazelCastInstance().getMap(TEMP_HUMID);
        Predicate criteriaQuery = Predicates.between("time", start, end);
        data = sensorData.values(criteriaQuery);
        List<SensorData> list = new ArrayList<>(data);
        Collections.sort(list, Comparator.comparingLong(SensorData::getTime));
        return list;
    }
}
