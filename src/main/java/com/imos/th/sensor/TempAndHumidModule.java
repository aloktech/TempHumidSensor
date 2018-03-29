/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.th.sensor;

import static com.imos.th.sensor.TempAndHumidConstant.CELCIUS;
import static com.imos.th.sensor.TempAndHumidConstant.EMPTY;
import static com.imos.th.sensor.TempAndHumidConstant.HUMIDITY;
import static com.imos.th.sensor.TempAndHumidConstant.TEMPERATURE;
import static com.imos.th.sensor.TempAndHumidConstant.PERCENTAGE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Pintu
 */
@Log4j2
public class TempAndHumidModule {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Calendar cal = GregorianCalendar.getInstance();

    private final int tempLength = TEMPERATURE.length();
    private final int humidLength = HUMIDITY.length();

    private final List<String> command = new ArrayList<>();

    public void config() {
        command.add("sudo");
        command.add("python");
        command.add("/home/pi/Adafruit_Python_DHT/examples/AdafruitDHT.py");
        command.add("22");
        command.add("4");
    }

    public Optional<SensorData> callSensor() {
        Future<Optional<SensorData>> future = EXECUTOR.submit(() -> {
            String data = executeCommand(command);
            if (data.isEmpty()) {
                return Optional.empty();
            }
            SensorData jsonData = new SensorData();
            jsonData.setTemperature(Double.parseDouble(data.substring(tempLength, data.indexOf(CELCIUS))));
            jsonData.setHumidity(Double.parseDouble(data.substring(data.indexOf(HUMIDITY) + humidLength, data.indexOf(PERCENTAGE))));
            cal.setTimeInMillis(jsonData.getTime());
            jsonData.setTimeStr(cal.get(Calendar.HOUR_OF_DAY)
                    + "." + cal.get(Calendar.MINUTE));
            return Optional.of(jsonData);
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("{} ", e.getMessage());
        }
        return Optional.empty();
    }

    public String executeCommand(List<String> command) {
        String value = EMPTY;
        try {
            ProcessExecutor processExceutor = new ProcessExecutor(command);
            value = processExceutor.startExecution().getInputMsg();
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
        return value;
    }
}
