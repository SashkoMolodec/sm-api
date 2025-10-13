package com.sashkomusic.analyzer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
public class AudioAnalyzerTest {

    @Test
    void test() {
        MusicAnalyzer_v3 analyzer = new MusicAnalyzer_v3();
        try {
            AudioFeatures audioFeatures = analyzer.analyzeMP3("/Users/okravch/my/coding/sashko_music/sm-api/downloads/Weltschmerz.mp3");
            System.out.println(audioFeatures);

        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}