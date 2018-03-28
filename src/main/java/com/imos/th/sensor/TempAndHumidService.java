package com.imos.th.sensor;

import com.alibaba.fastjson.JSON;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import static com.imos.th.sensor.TempAndHumidConstant.TEMP_HUMID;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Pintu
 */
@Log4j2
public class TempAndHumidService {

    private static final TempAndHumidService INSTANCE = new TempAndHumidService();

    private TempAndHumidModule module;

    @Getter
    private HazelcastInstance hazelCastInstance;

    private TempAndHumidService() {
    }

    public static final TempAndHumidService getInstance() {
        return INSTANCE;
    }

    public void config() {
        hazelCastInstance = Hazelcast.newHazelcastInstance();
        log.info("Configure Hazelcast");
        module = new TempAndHumidModule();
        module.config();
        log.info("Configure TempAndHumidModule");
    }

    public void executeService() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Optional<SensorData> optional = module.callSensor();
                if (optional.isPresent()) {
                    SensorData data = optional.get();
                    IMap<Long, SensorData> sensorData = hazelCastInstance.getMap(TEMP_HUMID);
                    sensorData.put(data.getTime(), data);
                    log.info(data);
                } else {
                    log.warn("Not data from sensor");
                }
            }
        }, 0, 120000);
    }

    public void saveToLocalDB() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            long lastTime = System.currentTimeMillis();

            @Override
            public void run() {
                String fileName = LocalDate.now().toString();
                log.info("File is saved: " + LocalDateTime.now());
                Collection<SensorData> data;
                IMap<Long, SensorData> sensorData = hazelCastInstance.getMap(TEMP_HUMID);
                log.info("Data count: " + sensorData.size());
                long time = System.currentTimeMillis();
                Predicate criteriaQuery = Predicates.between("time", lastTime, time);
                data = sensorData.values(criteriaQuery);
                try (FileWriter fileWriter = new FileWriter("/home/pi/NetBeansProjects/TempHumidSensor/" + fileName + ".txt", true);
                        BufferedWriter writer = new BufferedWriter(fileWriter)) {
                    List<SensorData> list = new ArrayList<>(data);
                    Collections.sort(list, Comparator.comparingLong(SensorData::getTime));
                    for (SensorData d : list) {
                        writer.append(JSON.toJSONString(d));
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TempAndHumidService.class.getName()).log(Level.SEVERE, null, ex);
                }
                lastTime = time;
            }
        }, 0, 600000);
    }

}
