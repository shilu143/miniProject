package org.iitrpr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestingStuffs {
    public static void generateTranscript(String id, String data) {
        // Get the user's home directory
        String home = System.getProperty("user.home");

        // Determine the subdirectory for the user's download directory based on the operating system
        String os = System.getProperty("os.name").toLowerCase();
        String downloadDir;
        if (os.contains("win")) {
            downloadDir = "\\Documents\\AimsPortal\\";
        } else if (os.contains("mac")) {
            downloadDir = "/Documents/AimsPortal/";
        } else {
            downloadDir = "/Documents/AimsPortal/";
        }

        // Create the full path to the download directory
        String fullPath = home + downloadDir + "Transcripts/";
        File folder = new File(fullPath);
        folder.mkdirs();
        String fileName = String.format("%s_Transcript", id);
        // Create the File object for the new file
        File file = new File(fullPath + fileName + ".txt");

        // Check if the file already exists
        if (file.exists()) {
            // If the file already exists, create a new file with a different name
            int i = 1;
            do {
                file = new File(fullPath + fileName+"(" + i + ").txt");
                i++;
            } while (file.exists());
        }



        try {
            // Create the new file
            boolean success = file.createNewFile();
            if (success) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            // Write data to the file using the FileWriter object.
            writer.write(data);

            // Close the FileWriter object to ensure that any data in its buffer is flushed to the file and any system resources used by the object are released.
            writer.close();

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
