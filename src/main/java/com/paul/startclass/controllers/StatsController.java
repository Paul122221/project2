package com.paul.startclass.controllers;


import com.paul.startclass.utils.TimedStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * The {@code StatsController} class is a Spring REST controller that provides HTTP endpoints
 * for interacting with statistics data within an application.
 *
 * This controller offers two primary functionalities:
 * 1. Retrieving statistics state as a string via a GET request.
 * 2. Recording new event data from a CSV input via a POST request.
 *
 * The class is annotated with {@code @RestController}, indicating it's ready for use
 * by Spring MVC to handle web requests. {@code @Autowired} is used to inject a
 * {@code TimedStatistics} component, ensuring that the controller can delegate
 * business logic operations related to statistics.
 *
 * @author  Azarenko Paul
 */
@RestController
public class StatsController {

    @Autowired
    public StatsController(TimedStatistics timedStatistics) {
        this.timedStatistics = timedStatistics;
    }

    TimedStatistics timedStatistics;

    /*
     * Implementation notes.
     * To retrieve the statistics for the last 60 seconds using TimedStatistics, we simply sum up the values
     * of the elements (SecondStatistic), which are fewer or equal than 60, depending on whether there were
     * seconds without requests. This method allows us to obtain data with a speed of O(1).
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getStats(){
        return ResponseEntity
                .ok()
                .header("Connection", "close")
                .body(timedStatistics.getStateAsString());
    }

    /*
     * Implementation notes.
     * Through the TimedStatistics class, it updates the data for
     * each separate second (SecondStatistic).
     */
    @PostMapping(value = "/event", consumes = "text/csv")
    public ResponseEntity<Void> recordEvent(@RequestBody String csvData) {

        String[] values = csvData.split(",");

        long timestamp = Long.parseLong(values[0]);
        BigDecimal x = new BigDecimal(values[1]);
        long y = Long.parseLong(values[2]);

        timedStatistics.update(x, y, timestamp);

        return ResponseEntity.noContent()
                .header("Connection", "close")
                .build();
    }

}




