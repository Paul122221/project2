package com.paul.startclass.utils.timedstatictics;

import java.math.BigDecimal;

/**
 * Statistics Preparation Interface
 * @author  Azarenko Paul
 */
public interface StatisticsServiceInterface {
    /**
     *
     * Method for adding elements to statistics:
     * @param x for the x parameter is a floating-point number.
     * @param y for y is an integer.
     * @param timestamp is the creation time of the element.
     */
    void update(BigDecimal x, long y, long timestamp);

    /**
     * Method for retrieving statistics in String format, Example:
     * "100000,33323.0000000000,0.3332300000,23542423400000,235424234.000"
     * Where the first number is the number of elements, the second is the sum of y, the third is the average of y,
     * the fourth is the sum of x, and the fifth is the average of x
     * @return String
     */
    String getStateAsString();
}
