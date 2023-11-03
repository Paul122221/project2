package com.paul.startclass;

import com.paul.startclass.utils.TimedStatistics;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
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
}
