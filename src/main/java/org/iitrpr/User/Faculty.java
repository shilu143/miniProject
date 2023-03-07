package org.iitrpr.User;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.iitrpr.utils.fileWriterUtil;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Faculty extends abstractUser {
    public Faculty(Connection connection, String id, String role) {
        super(connection, id, role);
    }

    @Override
    public void showMenu() {
        do {
            clearScreen();

            ArrayList<String> options = new ArrayList<>();
            options.add("Personal Details");
            options.add("Course Catalog");
            options.add("Course Offering");
            options.add("View Faculty Record");
            options.add("View Student Record");
            options.add("Show Current Event");
            options.add("Logout");

            CLI cli = new CLI();
            cli.createVSubmenu("Menu","Welcome to AIMS Portal", options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> showPersonalDetails(DataStorage._FACULTY);
                    case "2" -> viewCourseCatalog();
                    case "3" -> showCourseOffering();
                    case "4" -> showFacultyRecord();
                    case "5" -> viewStudentRecord();
                    case "6" -> currentEvent();
                    case "7" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    private void showFacultyRecord() {
        boolean outer;
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        do {
            outer = false;
            clearScreen();
            String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", id);
//            System.out.println(query);
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while(rs.next()) {
                    Array rsString = rs.getArray("session");
                    Integer[] session = null;
                    if (rsString != null) {
                        session = (Integer[]) rsString.getArray();
                    }
                    assert session != null;
                    query = String.format("""
                            SELECT courseid, coursename, (count(*) - 1) as count
                            FROM _%s
                            WHERE session = ARRAY[%d, %d]
                            GROUP BY courseid, coursename
                            """, id, session[0], session[1]);

                    ArrayList<ArrayList<String>> data = fetchTable(query);
                    options = new ArrayList<>();
                    options.add("CourseID");
                    options.add("CourseName");
                    options.add("Total Enrollment");
                    fetchEvent();
                    String header;
                    if(Arrays.equals(session, _CURR_SESSION)) {
                        header = String.format("(Current) Academic Session: %d-%d", session[0], session[1]);
                    }
                    else {
                        header = String.format("Academic Session: %d-%d", session[0], session[1]);
                    }
                    cli.recordPrint(header, options, data, null, null);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            options = new ArrayList<>();
            options.add("Drop Course");
            options.add("Upload Grades");
            options.add("Back");

            cli.createVSubmenu("SubMenu", null, options);
            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        if(dropCourse())
                            outer = true;
                        else
                            runner = true;
                    }

                    case "2" -> {
                        uploadGrades();
                        runner = true;
                    }
                    case "3" -> {
//                    return back
                    }
                    default -> {
                        runner = true;
                    }
                }
            } while(runner);
        } while(outer);
    }

    private boolean dropCourseUtil(boolean search, String courseId) {
        ArrayList<ArrayList<String>> allDept = getAllDept();
        if(search) {
            String query = String.format("""
                    SELECT *
                    FROM _%s
                    where lower(courseid) = lower('%s')
                        and lower(sid) = 'init' and
                        session = array[%d, %d]
                    """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
//                    System.out.println(query);
            return runQuery(query, true);
        }
        else {
            for (var dept : allDept) {
                for (int yr = 1; yr <= 4; yr++) {
                    String query = String.format("""
                            DELETE
                            FROM y%d_%s_offering
                            where lower(courseid) = lower('%s')
                            and lower(fid) = lower('%s')
                            """, yr, dept.get(0), courseId, id);
                    runQuery(query, false);
                }
            }
            String query = String.format("""
                   DELETE
                   FROM _%s
                   where lower(courseid) = lower('%s')
                   and session = array[%d, %d]
                    """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
            runQuery(query, false);
        }
        return true;
    }


    private boolean dropCourse() {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_FLOAT_START) {
            System.out.println(DataStorage.ANSI_RED + "Currently you can't drop course" + DataStorage.ANSI_RESET);
            return false;
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter courseId = ");
        String courseId = sc.nextLine();
        if(!dropCourseUtil(true, courseId)) {
            System.out.println(DataStorage.ANSI_RED + "you have not floated such course in the current semester" + DataStorage.ANSI_RESET);
            return false;
        }
        dropCourseUtil(false, courseId);
        return true;
    }

    private void uploadGrades() {
        fetchEvent();
        if(_EVENT != DataStorage._GRADE_SUBMISSION_START) {
            System.out.println(DataStorage.ANSI_RED + "Grade submission is not allowed currently" + DataStorage.ANSI_RESET);
            return;
        }
        System.out.print("Enter courseId = ");
        Scanner sc = new Scanner(System.in);
        String courseId = sc.nextLine();
        if(!dropCourseUtil(true, courseId)) {
            System.out.println(DataStorage.ANSI_RED + "No such course is floated by you in the current semester" + DataStorage.ANSI_RESET);
            return;
        }

        String query = String.format("""
                SELECT t1.sid, t2.name
                from _%s t1
                inner join student t2 on lower(t1.sid) = lower(t2.id)
                where lower(t1.courseid) = lower('%s')
                    and t1.session = array[%d, %d]
                    and t1.sid <> 'init'
                """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
//        System.out.println(query);
        ArrayList<ArrayList<String>> data = fetchTable(query);
        String filePath = fileWriterUtil.gradeSubmission(courseId, data);
        System.out.println("FILE PATH : " + filePath);
        System.out.println(DataStorage.ANSI_GREEN + "You can now upload grades on the above file" + DataStorage.ANSI_RESET);
        boolean validInput = false;
        while(!validInput) {
            System.out.print("Enter (Y/y) if you are done uploading grades of each student on that file : ");
            String inp = sc.nextLine();
            if(inp.equalsIgnoreCase("y")) {
                fetchEvent();
                if(_EVENT != DataStorage._GRADE_SUBMISSION_START) {
                    System.out.println(DataStorage.ANSI_RED + "Currently you are not allowed to upload grades" + DataStorage.ANSI_RESET);
                    return;
                }
                validInput = true;
                uploadGradesUtil(filePath, courseId);
            }
        }
        System.out.println(DataStorage.ANSI_GREEN + "Uploaded Grades for the course " + courseId + DataStorage.ANSI_RESET);
    }

    private void uploadGradesUtil(String filePath, String courseId) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.skip(1);
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                String sId = row[0];
                String grade = row[2];
                String query = String.format("""
                        UPDATE _%s
                        SET grade = lower('%s')
                        where lower(courseid) = lower('%s')
                            and session = array[%d, %d]
                        """, sId, grade, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
//                System.out.println(query);
                runQuery(query, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }


    void showCourseOffering() {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) showDeptOffering(dept.get(vl - 1).get(0));
                else if(vl == dept.size() + 1) {
//                    return to previous method
                }
                else {
                    runner = true;
                }
            } catch (NumberFormatException e) {
                runner = true;
            }
        } while(runner);
    }

    private void showDeptOffering(String deptId) {
        boolean validInput = false;
        int year = 0;
        while(!validInput) {
            System.out.print("Enter year (1, 2, 3, 4) = ");
            Scanner sc = new Scanner(System.in);
            String inp = sc.next();
            try{
                year = Integer.parseInt(inp);
                if(year < 1 || year > 4) {
                    System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
                }
                else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        clearScreen();
        fetchEvent();
        String tabName = String.format("y%d_%s_offering", year, deptId);
        String query = String.format("""
            SELECT table1.courseid, table1.coursename, table1.ltp, table1.prereq, table1.type, table1.cgcriteria, t3.name as Instructor 
                FROM (
                    SELECT t1.courseid, t2.coursename, t2.batch, t2.ltp, t2.type, t1.fid, t1.cgcriteria, t2.prereq
                    FROM %s t1
                    INNER JOIN course_catalog_%s t2
                    ON t2.courseid = t1.courseid
                ) table1
            INNER JOIN (
                SELECT courseid, MAX(batch) as max_value
                FROM course_catalog_cse table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) <> lower('e');
        """, tabName, deptId, _CURR_SESSION[0] - year + 1);
        ArrayList<ArrayList<String>> data = fetchTable(query);
        ArrayList<String> options  = new ArrayList<>();
        options.add("Course ID");
        options.add("Course Name");
        options.add("LTP");
        options.add("Prerequisites");
        options.add("Type");
        options.add("CG Criteria");
        options.add("Instructor");
        CLI cli = new CLI();
        cli.recordPrint("Core Courses", options, data, null, null);
        query = String.format("""
            SELECT table1.courseid, table1.coursename, table1.ltp, table1.prereq, table1.type, table1.cgcriteria, t3.name as Instructor 
                FROM (
                    SELECT t1.courseid, t2.coursename, t2.batch, t2.ltp, t2.type, t1.fid, t1.cgcriteria, t2.prereq
                    FROM %s t1
                    INNER JOIN course_catalog_%s t2
                    ON t2.courseid = t1.courseid
                ) table1
            INNER JOIN (
                SELECT courseid, MAX(batch) as max_value
                FROM course_catalog_cse table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) = lower('e');
        """, tabName, deptId, _CURR_SESSION[0] - year + 1);
        data = fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);
        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            String inp = sc.next();
            switch (inp) {
                case "1" -> {
//                    return back
                }
                default -> {
                    runner = true;
                }
            }
        } while(runner);
    }

    private void showDeptCourse(String deptid) {
        clearScreen();

        CLI cli = new CLI();
        String query = String.format("SELECT * FROM course_catalog_%s", deptid);
        ArrayList<ArrayList<String>> data = fetchTable(query);
        ArrayList<String> options = new ArrayList<>();
        options.add("Course ID");
        options.add("Course Name");
        options.add("LTP");
        options.add("Prerequisites");
        options.add("Type");
        options.add("Batch Onwards");
        cli.recordPrint(deptid.toUpperCase() + " Courses",options, data, null, null);
        options = new ArrayList<>();
//        options.add("Add New Course");
        options.add("Float a Course");
        options.add("Back");
        cli.createVSubmenu("Menu", null, options);

        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            switch (inp) {
                case "1" -> {
                    floatCourse(deptid);
                    runner = true;
                }
                case "2" -> {
                    //                    return back
                }
                default -> runner = true;
            }
        } while(runner);
    }

    @Override
    protected void editCourseCatalog(String deptid) {
//        return null;
    }

    @Override
    protected void addNewCourseinCatalog(String deptid) {
//        return null;
    }

    @Override
    public void floatCourse(String deptId) {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_FLOAT_START) {
            System.out.println(DataStorage.ANSI_RED + "Currently not allowed to float course" + DataStorage.ANSI_RESET);
            return;
        }
        System.out.print("Enter the course ID = ");
        Scanner sc = new Scanner(System.in);
        String courseId = sc.nextLine();
        String query = String.format("SELECT * FROM COURSE_CATALOG_%s where courseid = lower('%s')", deptId, courseId);
        if(!runQuery(query, true)) {
            System.out.println(DataStorage.ANSI_RED + "Sorry couldnot find the course with this id" + DataStorage.ANSI_RESET);
            return;
        }
//        System.out.println("Got the course");
        ArrayList<ArrayList<String>> allDept = getAllDept();
        for(var vl : allDept) {
            for(int yr = 1; yr <= 4; yr++) {
                query = String.format("SELECT * FROM y%d_%s_offering where courseid = lower('%s')", yr, vl.get(0), courseId);
                if(runQuery(query, true)) {
                    System.out.println("Sorry this course has already been floated");
                    return;
                }
            }
        }
        float cgCriteria = (float) -1;
        if(isValidInp("Do the course have any cg criteria(y/n) = ")) {
            boolean isvalid = false;
            while(!isvalid) {
                try {
                    System.out.print("Enter cg constraint ( <= 10.0) = ");
                    cgCriteria = Float.parseFloat(sc.nextLine());
                    if (cgCriteria < 0 || cgCriteria > 10)
                        System.out.println("Enter a valid input");
                    else
                        isvalid = true;
                }
                catch (NumberFormatException exception) {
                    System.out.println("Enter a valid input");
                }
            }
        }
        ArrayList<Boolean> yearsAllowed = new ArrayList<>();
        yearsAllowed.add(isValidInp("Is 1st year allowed(y/n) = "));
        yearsAllowed.add(isValidInp("Is 2nd year allowed(y/n) = "));
        yearsAllowed.add(isValidInp("Is 3rd year allowed(y/n) = "));
        yearsAllowed.add(isValidInp("Is 4th year allowed(y/n) = "));

        ArrayList<Boolean> deptAllowed = new ArrayList<>();
        for (ArrayList<String> strings : allDept) {
            query  = String.format("SELECT * FROM course_catalog_%s where courseid = lower('%s')", strings.get(0), courseId);
            deptAllowed.add(runQuery(query, true));
        }

        for(int i = 0; i < yearsAllowed.size(); i++) {
            for(int j = 0; j < deptAllowed.size(); j++) {
                if(yearsAllowed.get(i) && deptAllowed.get(j)) {
                    if(cgCriteria != -1) {
                        query = String.format("INSERT INTO y%d_%s_offering values('%s', %f, '%s')",
                                i + 1, allDept.get(j).get(0), courseId.toLowerCase(), cgCriteria, id);
                    }
                    else {
                        query = String.format("INSERT INTO y%d_%s_offering(courseid, fid) values('%s', '%s')",
                                i+1, allDept.get(j).get(0), courseId.toLowerCase(), id);
                    }
                    runQuery(query, false);
                }
            }
        }
        query = String.format("select coursename from course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        String courseName  = fetchTable(query).get(0).get(0);
        query = String.format("INSERT INTO _%s (courseid, coursename, sid, session) values('%s', '%s', 'init', array[%d, %d])",id, courseId, courseName, _CURR_SESSION[0], _CURR_SESSION[1]);
        runQuery(query, false);
        System.out.println(DataStorage.ANSI_GREEN + "Course Floated Successfully" + DataStorage.ANSI_RESET);
    }

    boolean isValidInp(String ques) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print(ques);
            String inp = sc.nextLine();
            if(inp.equalsIgnoreCase("y")) {
                return true;
            }
            else if(inp.equalsIgnoreCase("n")) {
                return false;
            }
        }
    }
}
