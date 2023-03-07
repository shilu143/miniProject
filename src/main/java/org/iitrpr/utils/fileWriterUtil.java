package org.iitrpr.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class fileWriterUtil {
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
//                System.out.println("File created successfully.");
            } else {
//                System.out.println("File already exists.");
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

//            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String gradeSubmission(String courseId, ArrayList<ArrayList<String>> data) {
        // Get the user's home directory
        String home = System.getProperty("user.home");

        // Determine the subdirectory for the user's download directory based on the operating system
        String os = System.getProperty("os.name").toLowerCase();
        String downloadDir;
        if (os.contains("win")) {
            downloadDir = "\\Documents\\AimsPortal\\";
        } else {
            downloadDir = "/Documents/AimsPortal/";
        }

        // Create the full path to the download directory
        String fullPath = home + downloadDir + "GradeSubmission/";
        File folder = new File(fullPath);
        folder.mkdirs();
        String fileName = String.format("%s_Grades", courseId);
        // Create the File object for the new file
        File file = new File(fullPath + fileName + ".csv");

        // Check if the file already exists
        if (file.exists()) {
            // If the file already exists, create a new file with a different name
            int i = 1;
            do {
                file = new File(fullPath + fileName + "(" + i + ").csv");
                i++;
            } while (file.exists());
        }

        try {
            // Create the new file
            boolean success = file.createNewFile();
            if (success) {
//                System.out.println(file.getName() + " file created successfully in " + fullPath);
            } else {
//                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }

        try {
            FileWriter csvWriter = new FileWriter(file.getAbsolutePath(), true);

            if (file.exists() && file.length() == 0) {
                csvWriter.append("SID,Name,Grade");
                csvWriter.append("\n");
            }

            for (var rowData : data) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath + file.getName();
    }

}
