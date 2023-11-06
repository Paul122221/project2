package com.paul.startclass;

import com.paul.startclass.utils.timedstatictics.TimedStatistics;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TimedStatisticsTest {

    private TimedStatistics timedStatistics;

    @Before
    public void setUp() {
        timedStatistics = new TimedStatistics();
    }

    @Test
    public void testUpdate() {
        // Act
        timedStatistics.update(new BigDecimal("0.33323"),235424234,123123);
        // Assert
        assertEquals("1,0.3332300000,0.3332300000,235424234,235424234.000", timedStatistics.getStateAsString(),
                "Count should match number of updates after single update");
    }


    @Test
    public void testUpdateAHundredThousand() {
        // Act
        BigDecimal x = new BigDecimal("0.33323");
        for(int i = 0; i < 100000; i++){
            timedStatistics.update(x,235424234,123123);
        }
        // Assert
        assertEquals("100000,33323.0000000000,0.3332300000,23542423400000,235424234.000", timedStatistics.getStateAsString(),
                "Count should match number of updates after single update");
    }


    @Test
    public void testConcurrentUpdatesAndGetState() throws InterruptedException {
        // Create a thread pool with 10 threads
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // Use a CountDownLatch to synchronize the threads
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    BigDecimal x = new BigDecimal("5.0");
                    long y = 50;
                    long timestamp = System.currentTimeMillis() / 1000;

                    timedStatistics.update(x, y, timestamp);

                    // Signal that this thread is done
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Wait for all threads to finish
        latch.await();

        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected
        // Since 10 threads each added (5.0, 50, 1), the count should be 10, sums 50, 500, averages 5, 50
        assertEquals("10,50.0000000000,5.0000000000,500,50.000", state);

        // Shutdown the thread pool
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }


    @Test
    public void testSingleUpdateAndGetState() {
        BigDecimal x = new BigDecimal("10.5");
        long y = 100;
        long timestamp = System.currentTimeMillis() / 1000;

        timedStatistics.update(x, y, timestamp);

        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected
        assertEquals("1,10.5000000000,10.5000000000,100,100.000", state);
    }



    @Test
    public void testEdgeCaseSixtySeconds() {
        // Update statistics for exactly 60 different seconds
        for (int i = 0; i < 60; i++) {
            BigDecimal x = new BigDecimal("10.5");
            long y = 100;
            long timestamp = System.currentTimeMillis() / 1000 + i;
            timedStatistics.update(x, y, timestamp);
        }

        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected
        assertEquals("60,630.0000000000,10.5000000000,6000,100.000", state);
    }


    @Test
    public void testEmptyStatistics() {
        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected for an empty statistics object
        assertEquals("0,0.0000000000,0.0000000000,0,0.000", state);
    }



    @Test
    public void testPrecision() {
        BigDecimal x = new BigDecimal("0.000000001");
        long y = 1;
        long timestamp = System.currentTimeMillis() / 1000;

        timedStatistics.update(x, y, timestamp);

        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected for precise values
        assertEquals("1,0.0000000010,0.0000000010,1,1.000", state);
    }



    @Test
    public void testBoundaryTimestamp() {
        // Update statistics for two different timestamps that fall within the same second
        BigDecimal x1 = new BigDecimal("10.5");
        long y1 = 100;
        long timestamp1 = System.currentTimeMillis() / 1000;
        timedStatistics.update(x1, y1, timestamp1);

        BigDecimal x2 = new BigDecimal("15.7");
        long y2 = 150;
        long timestamp2 = timestamp1 + 1;
        timedStatistics.update(x2, y2, timestamp2);

        String state = timedStatistics.getStateAsString();

        // Verify that the state string is as expected
        assertEquals("2,26.2000000000,13.1000000000,250,125.000", state);
    }

}
