package com.paul.startclass.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The TimedStatistics class implements a sliding window that compiles results based on
 * number of SecondStatistic elements (with a maximum  = 60), independent of the total number of elements.
 * This encapsulates the statistics for a specific second.
 * This approach enables the retrieval of results in O(1) time complexity.
 *
 * Moreover, it is designed to provide thread-safe statistical computations over the last 60 seconds.
 * It utilizes the Compare-And-Swap (CAS) method with AtomicReference to manage concurrency, ensuring
 * that updates to the current second's statistics are atomic and thus prevent race conditions.
 *
 * @author  Azarenko Paul
 */
@Component
public class TimedStatistics {

    /**
     * Nested within the class is the inner class SecondStatistic, encapsulating the sum of 'x' and 'y' values,
     * as well as the count of updates within a specific second. The 'add' method is designed to be thread-safe,
     * incrementing sums and counts atomically.
     *
     * @author  Azarenko Paul
     */
    private static class SecondStatistic {
        private BigDecimal sumX = new BigDecimal("0");
        private long sumY;
        private int count;
        private final long curentSecond;

        public SecondStatistic(long curentSecond) {
            this.curentSecond = curentSecond;
        }

        public void add(BigDecimal x, long y, int countNew) {
            sumX = sumX.add(x);// += x;
            sumY += y;
            count += countNew;
        }

        public BigDecimal getAvgX() {
            return count > 0 ? sumX.divide(new BigDecimal(count), RoundingMode.HALF_UP) : new BigDecimal(0); //  sumX / count : 0;
        }

        public double getAvgY() {
            return count > 0 ? sumY / count : 0;
        }

    }

    private final Queue<SecondStatistic> last60Seconds = new LinkedList<>();
    private final AtomicReference<SecondStatistic> currentSecond = new AtomicReference<>(new SecondStatistic(System.currentTimeMillis() / 1000));
    private long currentSecondStartTimestamp = System.currentTimeMillis() / 1000;
    private final Lock lock = new ReentrantLock();



    /*
    * An instance of SecondStatistic is maintained for the current second and is updated in a lock-protected
    * section to ensure exclusive access when the second changes. As this happens, the statistics
    * for the current second are enqueued into last60Seconds, a queue holding the most recent 60 seconds'
    * worth of data, and a new SecondStatistic instance is initialized for the forthcoming second.
    *
    * Additionally, the task specifies displaying data for the last 60 seconds, but there was no clarification
    *  regarding the time zone to base this on. Therefore, the current time zone of the server was chosen, rather
    *  than the client's, for the convenience of testing.
    *
    */

    /**
     * @author  Azarenko Paul
     */
    public void update(BigDecimal x, long y, long timestamp) {
        lock.lock();
        try {
            long nowSecondTimestamp = System.currentTimeMillis() / 1000;
            if (nowSecondTimestamp != currentSecondStartTimestamp) {
                if (last60Seconds.size() >= 60) {
                    last60Seconds.poll();
                }
                last60Seconds.add(currentSecond.getAndSet(new SecondStatistic(nowSecondTimestamp)));
                currentSecondStartTimestamp = nowSecondTimestamp;

            }
            currentSecond.get().add(x, y, 1);
        } finally {
            lock.unlock();
        }
    }

    /*
     * The 'getStateAsString' method is employed to fetch a combined string representation of the statistics
     * for the last 60 seconds. It locks the entire object, calculates the combined statistics for the seconds
     * within the last 60-second window, and formats them into a string.
     *
     * This class not only ensures accuracy in a multi-threaded environment but also maintains efficiency
     * as the lock is only held during the critical sections of the update and retrieval operations,
     * thus minimizing the time other threads are blocked.
     */

    /**
     * @author  Azarenko Paul
     */
    public String getStateAsString() {
        lock.lock();
        try {
            long currentSecond1 = System.currentTimeMillis() / 1000;
            SecondStatistic combinedState = new SecondStatistic(currentSecond1);
            for (SecondStatistic stat : last60Seconds) {
                if(stat.curentSecond < currentSecond1 + 60) {
                    combinedState.add(stat.sumX, stat.sumY, stat.count);
                }
            }
            SecondStatistic currentSec = currentSecond.get();
            combinedState.add(currentSec.sumX, currentSec.sumY, currentSec.count);

            return String.format("%d,%.10f,%.10f,%d,%.3f",
                    combinedState.count, combinedState.sumX, combinedState.getAvgX(), combinedState.sumY, combinedState.getAvgY());
        } finally {
            lock.unlock();
        }
    }
}
