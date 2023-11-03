# Sliding Window Statistics Collector

## Overview
The `Sliding Window Statistics Collector` is a Java-based utility designed to gather and compute statistics over a sliding window of the last 60 seconds. This system is capable of processing and summarizing data for each second, offering real-time insights with efficient computational complexity.

## Features
- **Efficient Data Retrieval**: Achieves O(1) time complexity for retrieving statistics, ensuring fast and consistent performance.
- **Thread-Safe Updates**: Utilizes Compare-And-Swap (CAS) with `AtomicReference` for managing concurrency, ensuring atomic updates and preventing race conditions.
- **Accurate Second-by-Second Statistics**: Encapsulates statistics for each second through the inner class `SecondStatistic`.
- **Scalability**: Designed to handle a large volume of updates without performance degradation.

## Components
- `StatsController`: A REST controller providing HTTP endpoints for data interaction.
    - `GET /stats`: Retrieves the current statistics state as a string.
    - `POST /event`: Records new event data from CSV input.
- `TimedStatistics`: Core component implementing the sliding window logic, providing thread-safe statistical computations.
- `SecondStatistic`: Inner class responsible for capturing the sum of values and the count of updates for each second.

## Usage
To use the collector, simply make a GET request to `/stats` to retrieve the current statistics or POST to `/event` with CSV-formatted event data.

## Setup
1. Ensure you have Java and Maven installed.
2. Clone the repository and navigate to the project directory.
3. Run `mvn spring-boot:run` to start the application.

## Thread Safety
The `TimedStatistics` class is designed to be thread-safe, utilizing a `ReentrantLock` and `AtomicReference` to ensure atomic updates and prevent race conditions. This guarantees that statistical updates and retrievals are safely executed in a multi-threaded environment.


## Testing
The project includes a comprehensive suite of unit tests verifying the functionality of statistical updates and retrieval.

