package com.presence.processing.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SignalProcessor {

    @Value("${app.detection.window-size:10}")
    private int windowSize;

    @Value("${app.detection.movement-variance-threshold:4.0}")
    private double movementVarianceThreshold;

    @Value("${app.detection.ema-alpha:0.3}")
    private double emaAlpha;

    @Value("${app.detection.noise-spike-threshold:15}")
    private double noiseSpikeThreshold;

    private final Map<String, Deque<Double>> windows   = new ConcurrentHashMap<>();
    private final Map<String, Double>        emaValues  = new ConcurrentHashMap<>();

    public record ProcessingResult(
            double smoothedRssi,
            double variance,
            double average,
            boolean movementDetected,
            boolean noiseFiltered
    ) {}

    public ProcessingResult process(String deviceId, int rawRssi) {
        // Step 1: Noise filter
        double currentEma = emaValues.getOrDefault(deviceId, (double) rawRssi);
        boolean noiseFiltered = false;
        double inputRssi = rawRssi;

        if (Math.abs(rawRssi - currentEma) > noiseSpikeThreshold) {
            noiseFiltered = true;
            inputRssi = currentEma;
        }

        // Step 2: EMA
        double newEma = emaAlpha * inputRssi + (1 - emaAlpha) * currentEma;
        emaValues.put(deviceId, newEma);

        // Step 3: Sliding window
        Deque<Double> window = windows.computeIfAbsent(deviceId, k -> new ArrayDeque<>(windowSize));
        window.addLast(newEma);
        if (window.size() > windowSize) window.pollFirst();

        // Step 4: Statistics
        double average  = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = window.size() < 2 ? 0.0 :
                window.stream().mapToDouble(v -> Math.pow(v - average, 2)).average().orElse(0.0);

        // Step 5: Decision
        boolean movement = variance > movementVarianceThreshold;

        return new ProcessingResult(newEma, variance, average, movement, noiseFiltered);
    }

    public void clearDevice(String deviceId) {
        windows.remove(deviceId);
        emaValues.remove(deviceId);
    }

    public int getWindowFill(String deviceId) {
        return windows.getOrDefault(deviceId, new ArrayDeque<>()).size();
    }
}