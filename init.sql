CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS rssi_readings (
    id         UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    device_id  VARCHAR(64) NOT NULL,
    rssi       INTEGER     NOT NULL,
    timestamp  BIGINT      NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS presence_events (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    device_id   VARCHAR(64) NOT NULL,
    status      VARCHAR(16) NOT NULL CHECK (status IN ('OCCUPIED','EMPTY','UNCERTAIN')),
    movement    BOOLEAN     NOT NULL DEFAULT FALSE,
    variance    DOUBLE PRECISION,
    avg_rssi    DOUBLE PRECISION,
    detected_at BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS movement_log (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    device_id   VARCHAR(64) NOT NULL,
    detected_at BIGINT      NOT NULL,
    variance    DOUBLE PRECISION,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rssi_device_ts   ON rssi_readings   (device_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_presence_device  ON presence_events (device_id, detected_at DESC);

INSERT INTO presence_events (device_id, status, movement, variance, avg_rssi, detected_at)
VALUES ('default', 'EMPTY', false, 0.0, -70.0, EXTRACT(EPOCH FROM NOW())::BIGINT)
ON CONFLICT DO NOTHING;