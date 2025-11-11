/*
package com.sashkomusic.analyzer;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MusicAnalyzer_v3 {

    // Constants for audio processing
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 2048;
    private static final int OVERLAP = 1024;

    // Temp directory for converted files
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public AudioFeatures analyzeMP3(String mp3Path) throws Exception {
        File audioFile = new File(mp3Path);

        // First, ensure we can read the file
        if (!audioFile.exists()) {
            throw new IOException("File not found: " + mp3Path);
        }

        // Convert to WAV if needed
        File wavFile = convertToWav(audioFile);

        try {
            // Analyze the WAV file
            return analyzeWavFile(wavFile);
        } finally {
            // Clean up temp file if it was created
            if (!wavFile.equals(audioFile)) {
                wavFile.delete();
            }
        }
    }

    // Convert using FFmpeg (external process)
    private boolean convertUsingFFmpeg(File inputFile, File outputFile) {
        try {
            // Build ffmpeg command - convert to mono for better beat detection
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.getAbsolutePath(),
                    "-acodec", "pcm_s16le",
                    "-ar", String.valueOf(SAMPLE_RATE),
                    "-ac", "1",  // Convert to mono for better beat detection
                    "-y", // overwrite output file
                    outputFile.getAbsolutePath()
            );

            // Run the process
            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0 && outputFile.exists() && outputFile.length() > 0;

        } catch (Exception e) {
            System.err.println("FFmpeg conversion failed: " + e.getMessage());
            return false;
        }
    }

    // Convert audio file to WAV format
    private File convertToWav(File inputFile) throws Exception {
        String fileName = inputFile.getName().toLowerCase();

        // If already WAV, return as is
        if (fileName.endsWith(".wav")) {
            return inputFile;
        }

        // Create temp WAV file
        String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        File wavFile = new File(TEMP_DIR, baseName + "_temp.wav");

        System.out.println("Converting " + inputFile.getName() + " to WAV format...");

        // Using external ffmpeg (more reliable but requires ffmpeg)
        if (convertUsingFFmpeg(inputFile, wavFile)) {
            System.out.println("Conversion successful using FFmpeg");
            return wavFile;
        }

        throw new UnsupportedAudioFileException("Could not convert file to WAV format: " + inputFile.getName());
    }

    // Analyze WAV file
    private AudioFeatures analyzeWavFile(File wavFile) throws Exception {
        AudioFeatures features = new AudioFeatures();

        // First pass: Dedicated BPM detection with optimized settings
        System.out.println("Performing BPM detection...");
        double bpm = detectBPMWithMultipleMethods(wavFile);
        features.bpm = bpm;

        // Second pass: Other features
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(wavFile, BUFFER_SIZE, OVERLAP);
        analyzeOtherFeatures(dispatcher, features);

        return features;
    }

    // Improved BPM detection using multiple methods
    private double detectBPMWithMultipleMethods(File wavFile) throws Exception {
        List<Double> bpmEstimates = new ArrayList<>();

        // Method 1: Percussion onset detection with different sensitivities
        double bpm1 = detectBPMWithPercussionOnsets(wavFile, 30, 8);
        double bpm2 = detectBPMWithPercussionOnsets(wavFile, 40, 12);

        // Method 2: Complex onset detection
        double bpm3 = detectBPMWithComplexOnsets(wavFile);

        // Method 3: Energy-based detection
        double bpm4 = detectBPMWithEnergyPeaks(wavFile);

        // Add valid estimates
        if (bpm1 > 0) bpmEstimates.add(bpm1);
        if (bpm2 > 0) bpmEstimates.add(bpm2);
        if (bpm3 > 0) bpmEstimates.add(bpm3);
        if (bpm4 > 0) bpmEstimates.add(bpm4);

        System.out.println("BPM Estimates: " + bpmEstimates);

        // Use the most common BPM range
        return selectBestBPM(bpmEstimates);
    }

    // Percussion onset detection with specific parameters
    private double detectBPMWithPercussionOnsets(File wavFile, double threshold, double sensitivity) throws Exception {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(wavFile, BUFFER_SIZE, OVERLAP);
        List<Double> onsetTimes = new ArrayList<>();

        OnsetHandler onsetHandler = (time, salience) -> onsetTimes.add(time);
        PercussionOnsetDetector detector = new PercussionOnsetDetector(
                SAMPLE_RATE, BUFFER_SIZE, onsetHandler, threshold, sensitivity
        );
        dispatcher.addAudioProcessor(detector);
        dispatcher.run();

        return calculateImprovedBPM(onsetTimes);
    }

    // Complex onset detection
    private double detectBPMWithComplexOnsets(File wavFile) throws Exception {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(wavFile, BUFFER_SIZE, OVERLAP);
        List<Double> onsetTimes = new ArrayList<>();

        OnsetHandler onsetHandler = (time, salience) -> onsetTimes.add(time);
        ComplexOnsetDetector detector = new ComplexOnsetDetector(BUFFER_SIZE);
        detector.setHandler(onsetHandler);
        dispatcher.addAudioProcessor(detector);
        dispatcher.run();

        return calculateImprovedBPM(onsetTimes);
    }

    // Energy-based peak detection
    private double detectBPMWithEnergyPeaks(File wavFile) throws Exception {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(wavFile, BUFFER_SIZE, OVERLAP);
        List<Double> peakTimes = new ArrayList<>();
        List<Double> energyValues = new ArrayList<>();

        dispatcher.addAudioProcessor(new AudioProcessor() {
            private double lastPeakTime = 0;
            private double threshold = 0;
            private int windowCount = 0;

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] buffer = audioEvent.getFloatBuffer();
                double energy = calculateRMS(buffer);
                energyValues.add(energy);

                // Adaptive threshold
                if (windowCount++ > 10) {
                    double avgEnergy = energyValues.stream()
                            .skip(Math.max(0, energyValues.size() - 50))
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0);
                    threshold = avgEnergy * 1.5;
                }

                // Peak detection
                double currentTime = audioEvent.getTimeStamp();
                if (energy > threshold && (currentTime - lastPeakTime) > 0.1) {
                    peakTimes.add(currentTime);
                    lastPeakTime = currentTime;
                }

                return true;
            }

            @Override
            public void processingFinished() {
            }
        });

        dispatcher.run();
        return calculateImprovedBPM(peakTimes);
    }

    // Improved BPM calculation with histogram approach
    private double calculateImprovedBPM(List<Double> onsetTimes) {
        if (onsetTimes.size() < 10) return 0;

        // Calculate all intervals
        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < onsetTimes.size(); i++) {
            double interval = onsetTimes.get(i) - onsetTimes.get(i - 1);
            if (interval > 0.2 && interval < 2.0) { // 30-300 BPM range
                intervals.add(interval);
            }
        }

        if (intervals.isEmpty()) return 0;

        // Convert to BPM candidates
        List<Double> bpmCandidates = intervals.stream()
                .map(interval -> 60.0 / interval)
                .collect(Collectors.toList());

        // Create BPM histogram
        Map<Integer, Integer> bpmHistogram = new HashMap<>();
        for (double bpm : bpmCandidates) {
            int bpmBin = ((int) (bpm / 2)) * 2; // 2 BPM bins
            bpmHistogram.merge(bpmBin, 1, Integer::sum);
        }

        // Find the most common BPM range
        int maxCount = 0;
        int bestBPM = 0;
        for (Map.Entry<Integer, Integer> entry : bpmHistogram.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestBPM = entry.getKey();
            }
        }

        // Check for tempo doubling/halving
        return adjustForTechnoTempo(bestBPM, bpmHistogram);
    }

    // Adjust BPM for common techno tempo ranges
    private double adjustForTechnoTempo(int detectedBPM, Map<Integer, Integer> histogram) {
        // Techno is typically 120-150 BPM
        if (detectedBPM >= 60 && detectedBPM <= 80) {
            // Check if double tempo exists in histogram
            int doubleBPM = detectedBPM * 2;
            if (histogram.containsKey(doubleBPM) || histogram.containsKey(doubleBPM + 2) || histogram.containsKey(doubleBPM - 2)) {
                return doubleBPM;
            }
        } else if (detectedBPM >= 240 && detectedBPM <= 300) {
            // We might be detecting every sub-beat
            return detectedBPM / 2.0;
        }

        return detectedBPM;
    }

    // Select best BPM from multiple estimates
    private double selectBestBPM(List<Double> estimates) {
        if (estimates.isEmpty()) return 0;

        // Group similar BPMs (within 5% of each other)
        Map<Double, Integer> bpmGroups = new HashMap<>();
        for (double bpm : estimates) {
            boolean foundGroup = false;
            for (Double groupBPM : new ArrayList<>(bpmGroups.keySet())) {
                if (Math.abs(bpm - groupBPM) / groupBPM < 0.05) {
                    bpmGroups.put(groupBPM, bpmGroups.get(groupBPM) + 1);
                    foundGroup = true;
                    break;
                }
            }
            if (!foundGroup) {
                bpmGroups.put(bpm, 1);
            }
        }

        // Return the most common BPM
        return bpmGroups.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0.0);
    }

    // Analyze other features (pitch, energy, spectral)
    private void analyzeOtherFeatures(AudioDispatcher dispatcher, AudioFeatures features) {
        // Lists to collect data
        List<Float> pitches = new ArrayList<>();
        List<Double> rmsValues = new ArrayList<>();
        List<Double> spectralCentroids = new ArrayList<>();
        List<Double> spectralFlux = new ArrayList<>();
        List<Double> beatStrengths = new ArrayList<>();

        // Add pitch detector
        PitchDetectionHandler pitchHandler = (result, audioEvent) -> {
            if (result.isPitched()) {
                pitches.add(result.getPitch());
            }
        };
        dispatcher.addAudioProcessor(new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                SAMPLE_RATE, BUFFER_SIZE, pitchHandler
        ));

        // Add combined RMS and spectral analyzer
        dispatcher.addAudioProcessor(new AudioProcessor() {
            FFT fft = new FFT(BUFFER_SIZE);
            float[] previousSpectrum = new float[BUFFER_SIZE / 2];
            boolean firstFrame = true;

            // For beat strength calculation
            List<Double> recentEnergies = new ArrayList<>();
            double beatInterval = 60.0 / features.bpm; // Use detected BPM
            int framesPerBeat = (int) (beatInterval * SAMPLE_RATE / (BUFFER_SIZE - OVERLAP));

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] buffer = audioEvent.getFloatBuffer();

                // Calculate RMS
                double rms = calculateRMS(buffer);
                rmsValues.add(rms);
                recentEnergies.add(rms);

                // Keep only recent energies (2 beats worth)
                if (recentEnergies.size() > framesPerBeat * 2) {
                    recentEnergies.remove(0);
                }

                // Calculate beat strength as energy variance
                if (recentEnergies.size() >= framesPerBeat) {
                    double beatStrength = calculateVariance(recentEnergies);
                    beatStrengths.add(beatStrength);
                }

                // Prepare for FFT
                float[] fftBuffer = buffer.clone();
                float[] spectrum = new float[fftBuffer.length / 2];

                // Apply FFT
                fft.forwardTransform(fftBuffer);
                fft.modulus(fftBuffer, spectrum);

                // Calculate spectral features
                double centroid = calculateSpectralCentroid(spectrum, SAMPLE_RATE);
                spectralCentroids.add(centroid);

                if (!firstFrame) {
                    double flux = calculateSpectralFlux(spectrum, previousSpectrum);
                    spectralFlux.add(flux);
                }
                firstFrame = false;

                System.arraycopy(spectrum, 0, previousSpectrum, 0, spectrum.length);

                return true;
            }

            @Override
            public void processingFinished() {
            }
        });

        // Run the dispatcher
        System.out.println("Analyzing other audio features...");
        dispatcher.run();

        // Calculate final features
        features.avgPitch = pitches.stream()
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(0);
        features.pitchVariance = calculateVariance(pitches);

        features.avgEnergy = rmsValues.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        features.energyVariance = calculateVariance(rmsValues);

        features.spectralCentroid = spectralCentroids.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        features.spectralFlux = spectralFlux.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        // Improved beat strength calculation
        features.beatStrength = beatStrengths.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        System.out.println("Analysis complete!");
    }

    // Calculate RMS
    private double calculateRMS(float[] buffer) {
        double sum = 0;
        for (float sample : buffer) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / buffer.length);
    }

    // Calculate spectral centroid
    private double calculateSpectralCentroid(float[] spectrum, float sampleRate) {
        double weightedSum = 0;
        double magnitudeSum = 0;

        for (int i = 0; i < spectrum.length; i++) {
            double frequency = (i * sampleRate) / (2.0 * spectrum.length);
            weightedSum += frequency * spectrum[i];
            magnitudeSum += spectrum[i];
        }

        return magnitudeSum > 0 ? weightedSum / magnitudeSum : 0;
    }

    // Calculate spectral flux
    private double calculateSpectralFlux(float[] spectrum, float[] previousSpectrum) {
        double flux = 0;
        for (int i = 0; i < Math.min(spectrum.length, previousSpectrum.length); i++) {
            double diff = spectrum[i] - previousSpectrum[i];
            flux += diff > 0 ? diff * diff : 0;
        }
        return Math.sqrt(flux);
    }

    // Calculate variance
    private double calculateVariance(List<? extends Number> values) {
        if (values.isEmpty()) return 0;

        double mean = values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0);

        return values.stream()
                .mapToDouble(n -> Math.pow(n.doubleValue() - mean, 2))
                .average()
                .orElse(0);
    }
}
*/
