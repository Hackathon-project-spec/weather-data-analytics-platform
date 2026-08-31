-- SIH26069: National Weather Big Data Analytics Platform
-- ClickHouse OLAP Schema Initialization

CREATE DATABASE IF NOT EXISTS weather_db;

CREATE TABLE IF NOT EXISTS weather_db.raw_telemetry (
    timestamp DateTime64(3, 'Asia/Kolkata'),
    station_id LowCardinality(String),
    state LowCardinality(String),
    district LowCardinality(String),
    latitude Float64,
    longitude Float64,
    temperature Float32,
    humidity Float32,
    pressure Float32,
    precipitation_mm Float32,
    wind_speed_kmh Float32,
    wind_direction Float32,
    solar_radiation Float32,
    aqi UInt16
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (state, district, station_id, timestamp)
TTL toDate(timestamp) + INTERVAL 365 DAY;

CREATE TABLE IF NOT EXISTS weather_db.hourly_aggregates (
    date Date,
    hour UInt8,
    station_id LowCardinality(String),
    state LowCardinality(String),
    district LowCardinality(String),
    avg_temperature Float32,
    max_temperature Float32,
    min_temperature Float32,
    avg_humidity Float32,
    avg_pressure Float32,
    total_precipitation_mm Float32,
    max_wind_speed_kmh Float32,
    reading_count UInt32
) ENGINE = SummingMergeTree((total_precipitation_mm, reading_count))
PARTITION BY toYYYYMM(date)
ORDER BY (state, district, station_id, date, hour);

CREATE TABLE IF NOT EXISTS weather_db.social_mentions (
    timestamp DateTime64(3, 'Asia/Kolkata'),
    platform LowCardinality(String),
    post_id String,
    state LowCardinality(String),
    district LowCardinality(String),
    disaster_category LowCardinality(String),
    sentiment_score Float32,
    text String
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (disaster_category, state, district, timestamp)
TTL toDate(timestamp) + INTERVAL 90 DAY;