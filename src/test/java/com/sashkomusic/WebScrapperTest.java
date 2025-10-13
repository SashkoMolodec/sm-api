package com.sashkomusic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class WebScrapperTest {

    static class Track {
        String number;
        String artist;
        String title;
        String label;
        String catalogNumber;

        @Override
        public String toString() {
            return String.format("%s. %s - %s [%s %s]", number, artist, title, label, catalogNumber);
        }
    }

    @Test
    void test() {
        String url = "https://www.mixesdb.com/w/2000-09-29_-_Jeff_Mills_@_The_Liquid_Room,_Tokyo";

        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").get();

            // Select the ordered list that contains the tracklist
            Elements tracks = doc.select("div.mw-parser-output > ol > li");
            List<Track> trackList = new ArrayList<>();

            for (int i = 0; i < tracks.size(); i++) {
                Element trackElement = tracks.get(i);
                String trackText = trackElement.text();

                // Skip tracks marked as "?"
                if (trackText.equals("?")) {
                    continue;
                }

                Track track = parseTrackInfo(i + 1, trackText);
                if (track != null) {
                    trackList.add(track);
                }
            }

            // Print results
            System.out.println("Total tracks found: " + trackList.size());
            System.out.println("\nTracklist:");
            for (Track track : trackList) {
                System.out.println(track);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Track parseTrackInfo(int number, String trackText) {
        // Example format: "Jeff Mills - Landscape (Urbana) [Axis - AX 022]"
        Track track = new Track();
        track.number = String.valueOf(number);

        try {
            // Split artist and rest
            String[] parts = trackText.split(" - ", 2);
            if (parts.length < 2) return null;

            track.artist = parts[0].trim();

            // Handle the title and label part
            String remainder = parts[1];
            int labelStart = remainder.lastIndexOf("[");
            if (labelStart > 0) {
                // Extract title (everything before the label)
                track.title = remainder.substring(0, labelStart).trim();

                // Extract label info
                String labelInfo = remainder.substring(labelStart + 1, remainder.length() - 1);
                String[] labelParts = labelInfo.split(" - ", 2);
                track.label = labelParts[0].trim();
                if (labelParts.length > 1) {
                    track.catalogNumber = labelParts[1].trim();
                }
            } else {
                track.title = remainder.trim();
                track.label = "";
                track.catalogNumber = "";
            }

            return track;
        } catch (Exception e) {
            // If parsing fails, return null
            return null;
        }
    }

    @Test
    void downloadVideo() {
        String videoUrl = "https://www.youtube.com/watch?v=myO-8Cp4b2A&list=RDmyO-8Cp4b2A&start_radio=1"; // Replace with your desired video URL

        String projectDir = System.getProperty("user.dir");
        String outputDirectory = projectDir + File.separator + "downloads";

        // Create downloads directory if it doesn't exist
        File downloadsDir = new File(outputDirectory);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "yt-dlp",
                    "-t", "mp3",
                    "-o", outputDirectory + "/%(title)s.%(ext)s",
                    "--no-playlist",
                    videoUrl
            );

            Process process = processBuilder.start();
            System.out.println(process.waitFor());
            // Read output from the process (optional, for debugging/monitoring)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Video downloaded successfully!");
            } else {
                System.err.println("Error downloading video. yt-dlp exited with code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

