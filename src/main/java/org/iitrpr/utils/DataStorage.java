package org.iitrpr.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class DataStorage {
    public final HashMap<String, Integer> GradePointMap = new HashMap<String, Integer>();
    public final ArrayList<String> EventHash = new ArrayList<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final int _SEMESTER_START = 0;
    public static final int _COURSE_FLOAT_START = 1;
    public static final int _COURSE_FLOAT_END = 2;
    public static final int _COURSE_REG_START = 3;
    public static final int _COURSE_REG_END = 4;
    public static final int _GRADE_SUBMISSION_START = 5;
    public static final int _GRADE_SUBMISSION_END = 6;
    public static final int _SEMESTER_END = 7;
    public static final int _RUNNING = 0;
    public static final int _COMPLETED = 1;
    public static final int _STUDENT = 0;
    public static final int _FACULTY = 1;
    public static final int _OFFICE = 2;
    public DataStorage() {
        GradePointMap.put("A", 10);
        GradePointMap.put("a", 10);
        GradePointMap.put("A-", 9);
        GradePointMap.put("a-", 9);
        GradePointMap.put("B", 8);
        GradePointMap.put("b", 8);
        GradePointMap.put("B-", 7);
        GradePointMap.put("b-", 7);
        GradePointMap.put("C", 6);
        GradePointMap.put("c", 6);
        GradePointMap.put("C-", 5);
        GradePointMap.put("c-", 5);
        GradePointMap.put("D", 4);
        GradePointMap.put("d", 4);
        GradePointMap.put("E", 2);
        GradePointMap.put("e", 2);
        GradePointMap.put("F", 0);
        GradePointMap.put("f", 0);


        EventHash.add("Semester has Started");
        EventHash.add("Course Float Event has Started");
        EventHash.add("Course Float Event has Ended");
        EventHash.add("Course Registration has Started");
        EventHash.add("Course Registration has Ended");
        EventHash.add("Grade Submission Event has Started");
        EventHash.add("Grade Submission Event has Ended");
        EventHash.add("Semester Ended");
    }
}
