-- SIH26069: National Weather Big Data Analytics Platform
-- PostgreSQL Initial Schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Stations Master Table
CREATE TABLE IF NOT EXISTS stations (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    state VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude_m DOUBLE PRECISION DEFAULT 0.0,
    station_type VARCHAR(32) NOT NULL DEFAULT 'AWS', -- 'AWS', 'RADAR', 'SATELLITE_PROBE'
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',    -- 'ACTIVE', 'MAINTENANCE', 'OFFLINE'
    last_ping_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_stations_state_district ON stations(state, district);
CREATE INDEX IF NOT EXISTS idx_stations_lat_lon ON stations(latitude, longitude);

-- Citizen Reports Table
CREATE TABLE IF NOT EXISTS citizen_reports (
    id VARCHAR(64) PRIMARY KEY,
    reporter_name VARCHAR(128) NOT NULL DEFAULT 'Anonymous Citizen',
    reporter_contact_hash VARCHAR(128),
    category VARCHAR(64) NOT NULL, -- 'FLOOD', 'HEAVY_RAIN', 'CYCLONE_WIND', 'HAILSTORM', 'HEATWAVE', 'LANDSLIDE', 'LIGHTNING'
    severity_level INT NOT NULL CHECK (severity_level BETWEEN 1 AND 5),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    state VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    description TEXT,
    media_url VARCHAR(512),
    verification_status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'VERIFYING', 'VERIFIED', 'SUSPICIOUS', 'DEBUNKED'
    confidence_score DOUBLE PRECISION DEFAULT 0.0,
    score_breakdown JSONB,
    verification_reasoning TEXT,
    matched_station_id VARCHAR(64),
    station_distance_km DOUBLE PRECISION,
    verification_latency_ms BIGINT DEFAULT 0,
    upvotes INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_reports_status ON citizen_reports(verification_status);
CREATE INDEX IF NOT EXISTS idx_reports_state_district ON citizen_reports(state, district);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON citizen_reports(created_at DESC);

-- Verification Audit Logs Table
CREATE TABLE IF NOT EXISTS verification_logs (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES citizen_reports(id) ON DELETE CASCADE,
    matched_station_id VARCHAR(64),
    distance_km DOUBLE PRECISION,
    time_delta_sec BIGINT,
    sensor_metrics JSONB,
    reported_category VARCHAR(64),
    computed_score DOUBLE PRECISION,
    final_status VARCHAR(32),
    evaluation_factors JSONB,
    verified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vlogs_report_id ON verification_logs(report_id);

-- Weather Disaster Alerts Table (CAP Compliant)
CREATE TABLE IF NOT EXISTS weather_alerts (
    id VARCHAR(64) PRIMARY KEY,
    identifier VARCHAR(128) NOT NULL UNIQUE,
    sender VARCHAR(128) NOT NULL DEFAULT 'MoES-IMD-Analytics-DSS',
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL DEFAULT 'Actual', -- 'Actual', 'Exercise', 'Test'
    msg_type VARCHAR(32) NOT NULL DEFAULT 'Alert', -- 'Alert', 'Update', 'Cancel'
    severity VARCHAR(32) NOT NULL, -- 'Extreme', 'Severe', 'Moderate', 'Minor'
    urgency VARCHAR(32) NOT NULL DEFAULT 'Immediate', -- 'Immediate', 'Expected', 'Future'
    certainty VARCHAR(32) NOT NULL DEFAULT 'Observed', -- 'Observed', 'Likely', 'Possible'
    event_category VARCHAR(64) NOT NULL, -- 'Flood', 'Cyclone', 'Heatwave', 'Thunderstorm'
    headline VARCHAR(256) NOT NULL,
    description TEXT NOT NULL,
    instruction TEXT,
    affected_state VARCHAR(64) NOT NULL,
    affected_district VARCHAR(64) NOT NULL,
    polygon_geojson JSONB,
    radius_km DOUBLE PRECISION,
    center_lat DOUBLE PRECISION,
    center_lon DOUBLE PRECISION,
    effective_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_alerts_active ON weather_alerts(is_active, expires_at);
CREATE INDEX IF NOT EXISTS idx_alerts_state_district ON weather_alerts(affected_state, affected_district);

-- AI Structured Events Ingestion Table
CREATE TABLE IF NOT EXISTS ai_events (
    id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    source VARCHAR(64) NOT NULL DEFAULT 'AI_ANALYSIS',
    city VARCHAR(128),
    state VARCHAR(64),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    severity VARCHAR(32) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    report_count INT DEFAULT 0,
    summary TEXT,
    operational_status VARCHAR(32) NOT NULL DEFAULT 'MONITORING',
    observed_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_events_status ON ai_events(operational_status);
CREATE INDEX IF NOT EXISTS idx_ai_events_type ON ai_events(event_type);
CREATE INDEX IF NOT EXISTS idx_ai_events_created_at ON ai_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_events_location ON ai_events(latitude, longitude);

-- Seed Initial 30+ Weather Stations across Major Indian States/Districts
INSERT INTO stations (id, code, name, state, district, latitude, longitude, altitude_m, station_type) VALUES
('stn-mum-01', 'BOM-COL', 'Mumbai Colaba AWS', 'Maharashtra', 'Mumbai City', 18.8997, 72.8153, 11.0, 'AWS'),
('stn-mum-02', 'BOM-SCZ', 'Mumbai Santacruz AWS', 'Maharashtra', 'Mumbai Suburban', 19.0896, 72.8656, 14.0, 'AWS'),
('stn-mum-03', 'BOM-THN', 'Thane City AWS', 'Maharashtra', 'Thane', 19.2183, 72.9781, 15.0, 'AWS'),
('stn-del-01', 'DEL-SFD', 'Delhi Safdarjung AWS', 'Delhi', 'New Delhi', 28.5843, 77.2065, 216.0, 'AWS'),
('stn-del-02', 'DEL-PAL', 'Delhi Palam AWS', 'Delhi', 'South West Delhi', 28.5630, 77.1200, 237.0, 'AWS'),
('stn-del-03', 'DEL-NOI', 'Noida Sector 62 AWS', 'Uttar Pradesh', 'Gautam Buddha Nagar', 28.6270, 77.3725, 200.0, 'AWS'),
('stn-del-04', 'DEL-GUR', 'Gurugram Cyber City AWS', 'Haryana', 'Gurugram', 28.4595, 77.0266, 219.0, 'AWS'),
('stn-blr-01', 'BLR-CTY', 'Bengaluru City AWS', 'Karnataka', 'Bengaluru Urban', 12.9716, 77.5946, 920.0, 'AWS'),
('stn-blr-02', 'BLR-KIAL', 'Bengaluru Airport AWS', 'Karnataka', 'Bengaluru Rural', 13.1986, 77.7066, 915.0, 'AWS'),
('stn-chn-01', 'MAA-MNG', 'Chennai Meenambakkam AWS', 'Tamil Nadu', 'Chennai', 12.9830, 80.1700, 16.0, 'AWS'),
('stn-chn-02', 'MAA-NUM', 'Chennai Nungambakkam AWS', 'Tamil Nadu', 'Chennai', 13.0600, 80.2400, 10.0, 'AWS'),
('stn-kol-01', 'CCU-ALR', 'Kolkata Alipore AWS', 'West Bengal', 'Kolkata', 22.5333, 88.3333, 6.0, 'AWS'),
('stn-kol-02', 'CCU-DUM', 'Kolkata Dum Dum AWS', 'West Bengal', 'North 24 Parganas', 22.6500, 88.4500, 5.0, 'AWS'),
('stn-hyd-01', 'HYD-BEG', 'Hyderabad Begumpet AWS', 'Telangana', 'Hyderabad', 17.4500, 78.4700, 531.0, 'AWS'),
('stn-hyd-02', 'HYD-RGI', 'Hyderabad Shamshabad AWS', 'Telangana', 'Rangareddy', 17.2403, 78.4294, 617.0, 'AWS'),
('stn-odi-01', 'BBI-CTY', 'Bhubaneswar AWS', 'Odisha', 'Khurda', 20.2961, 85.8245, 45.0, 'AWS'),
('stn-odi-02', 'BBI-PUR', 'Puri Coastal AWS', 'Odisha', 'Puri', 19.8135, 85.8312, 10.0, 'AWS'),
('stn-odi-03', 'BBI-PRD', 'Paradip Port AWS', 'Odisha', 'Jagatsinghpur', 20.3167, 86.6167, 4.0, 'AWS'),
('stn-odi-04', 'BBI-BAL', 'Balasore Radar Station', 'Odisha', 'Balasore', 21.4934, 86.9135, 19.0, 'RADAR'),
('stn-ahm-01', 'AMD-CTY', 'Ahmedabad City AWS', 'Gujarat', 'Ahmedabad', 23.0225, 72.5714, 53.0, 'AWS'),
('stn-pun-01', 'PNQ-SHV', 'Pune Shivajinagar AWS', 'Maharashtra', 'Pune', 18.5314, 73.8446, 560.0, 'AWS'),
('stn-jai-01', 'JAI-SNG', 'Jaipur Sanganer AWS', 'Rajasthan', 'Jaipur', 26.8200, 75.8000, 390.0, 'AWS'),
('stn-lko-01', 'LKO-AMA', 'Lucknow Amausi AWS', 'Uttar Pradesh', 'Lucknow', 26.7606, 80.8893, 123.0, 'AWS'),
('stn-pat-01', 'PAT-CTY', 'Patna Airport AWS', 'Bihar', 'Patna', 25.5941, 85.1376, 53.0, 'AWS'),
('stn-gau-01', 'GAU-BOR', 'Guwahati Borjhar AWS', 'Assam', 'Kamrup Metropolitan', 26.1061, 91.5859, 54.0, 'AWS'),
('stn-koc-01', 'COK-NED', 'Kochi Nedumbassery AWS', 'Kerala', 'Ernakulam', 10.1518, 76.3930, 8.0, 'AWS'),
('stn-viz-01', 'VTZ-CTY', 'Visakhapatnam AWS', 'Andhra Pradesh', 'Visakhapatnam', 17.6868, 83.2185, 4.0, 'AWS'),
('stn-bho-01', 'BHO-BAIR', 'Bhopal Bairagarh AWS', 'Madhya Pradesh', 'Bhopal', 23.2800, 77.3500, 523.0, 'AWS'),
('stn-shi-01', 'SLV-CTY', 'Shimla Ridge AWS', 'Himachal Pradesh', 'Shimla', 31.1048, 77.1734, 2205.0, 'AWS'),
('stn-sri-01', 'SXR-CTY', 'Srinagar Aerodrome AWS', 'Jammu and Kashmir', 'Srinagar', 34.0837, 74.7973, 1585.0, 'AWS')
ON CONFLICT (id) DO NOTHING;
