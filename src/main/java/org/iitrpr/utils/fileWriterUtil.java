package org.iitrpr.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class fileWriterUtil {
    public static void generateTranscript(String id, String data) {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        String downloadDir;
        if (os.contains("win")) {
            downloadDir = "\\Documents\\AimsPortal\\";
        } else if (os.contains("mac")) {
            downloadDir = "/Documents/AimsPortal/";
        } else {
            downloadDir = "/Documents/AimsPortal/";
        }

        String fullPath = home + downloadDir + "Transcripts/";
        File folder = new File(fullPath);
        folder.mkdirs();
        String fileName = String.format("%s_Transcript", id);
        File file = new File(fullPath + fileName + ".txt");

        if (file.exists()) {
            int i = 1;
            do {
                file = new File(fullPath + fileName+"(" + i + ").txt");
                i++;
            } while (file.exists());
        }



        try {
            boolean success = file.createNewFile();
            if (success) {
            } else {
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String gradeSubmission(String courseId, ArrayList<ArrayList<String>> data) {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        String downloadDir;
        if (os.contains("win")) {
            downloadDir = "\\Documents\\AimsPortal\\";
        } else {
            downloadDir = "/Documents/AimsPortal/";
        }

        String fullPath = home + downloadDir + "GradeSubmission/";
        File folder = new File(fullPath);
        folder.mkdirs();
        String fileName = String.format("%s_Grades", courseId);
        File file = new File(fullPath + fileName + ".csv");

        if (file.exists()) {
            int i = 1;
            do {
                file = new File(fullPath + fileName + "(" + i + ").csv");
                i++;
            } while (file.exists());
        }

        try {
            boolean success = file.createNewFile();
            if (success) {
            } else {
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
