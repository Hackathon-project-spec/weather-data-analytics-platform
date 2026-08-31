package in.gov.moes.sih26069.ingestion.service;

import in.gov.moes.sih26069.common.dto.StationDTO;
import in.gov.moes.sih26069.common.event.TelemetryEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TelemetrySimulatorEngine {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulatorEngine.class);

    @Autowired
    private StationCatalogService catalogService;

    @Autowired(required = false)
    private KafkaTemplate<String, TelemetryEvent> kafkaTemplate;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicInteger targetEventsPerSecond = new AtomicInteger(20); // Default 20 events/sec
    private final AtomicLong totalEventsGenerated = new AtomicLong(0);

    private ScheduledExecutorService executorService;
    private final Random random = new Random();

    @PostConstruct
    public void start() {
        executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "telemetry-sim-worker");
            t.setDaemon(true);
            return t;
        });

        // Run simulation pulse every 100ms
        executorService.scheduleAtFixedRate(this::generatePulse, 1000, 100, TimeUnit.MILLISECONDS);
        log.info("Telemetry Simulator initialized at {} events/sec", targetEventsPerSecond.get());
    }

    @PreDestroy
    public void stop() {
        isRunning.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public void setRunning(boolean running) {
        this.isRunning.set(running);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setTargetRate(int rateEventsPerSec) {
        int clamped = Math.max(1, Math.min(rateEventsPerSec, 2000));
        this.targetEventsPerSecond.set(clamped);
        log.info("Simulator rate adjusted to {} events/sec", clamped);
    }

    public int getTargetRate() {
        return targetEventsPerSecond.get();
    }

    public long getTotalEventsGenerated() {
        return totalEventsGenerated.get();
    }

    private void generatePulse() {
        if (!isRunning.get()) return;

        List<StationDTO> stations = catalogService.getAllStations();
        if (stations.isEmpty()) return;

        int eventsPerPulse = Math.max(1, targetEventsPerSecond.get() / 10);

        for (int i = 0; i < eventsPerPulse; i++) {
            StationDTO stn = stations.get(random.nextInt(stations.size()));

            // Gaussian micro-fluctuations
            double temp = Math.round((stn.getCurrentTemperature() + (random.nextDouble() - 0.5) * 0.4) * 10.0) / 10.0;
            double rain = Math.max(0.0, Math.round((stn.getCurrentRainfallMm() + (random.nextDouble() - 0.5) * 0.2) * 10.0) / 10.0);
            double humidity = Math.min(100.0, Math.max(20.0, Math.round(stn.getCurrentHumidity() + (random.nextDouble() - 0.5) * 0.6)));
            double wind = Math.max(0.0, Math.round((stn.getCurrentWindSpeedKmh() + (random.nextDouble() - 0.5) * 0.8) * 10.0) / 10.0);
            double pressure = Math.round((stn.getCurrentPressure() + (random.nextDouble() - 0.5) * 0.2) * 10.0) / 10.0;

            TelemetryEvent event = new TelemetryEvent();
            event.setStationId(stn.getId());
            event.setStationCode(stn.getCode());
            event.setStationName(stn.getName());
            event.setState(stn.getState());
            event.setDistrict(stn.getDistrict());
            event.setLatitude(stn.getLatitude() + (random.nextDouble() - 0.5) * 0.005);
            event.setLongitude(stn.getLongitude() + (random.nextDouble() - 0.5) * 0.005);
            event.setTemperature(temp);
            event.setHumidity(humidity);
            event.setPressure(pressure);
            event.setPrecipitationMm(rain);
            event.setWindSpeedKmh(wind);
            event.setWindDirection(random.nextDouble() * 360.0);
            event.setSolarRadiation(Math.max(0.0, 450 + (random.nextDouble() - 0.5) * 50));
            event.setAqi(40 + random.nextInt(35));
            event.setSimulated(true);
            event.setTimestamp(Instant.now());

            catalogService.updateStationMetrics(event);
            totalEventsGenerated.incrementAndGet();

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("weather.raw.telemetry", event.getStationId(), event);
                } catch (Exception e) {
                    log.trace("Kafka send error: {}", e.getMessage());
                }
            }
        }
    }
}
