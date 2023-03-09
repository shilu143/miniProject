package org.iitrpr.User;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;
import org.iitrpr.utils.fileWriterUtil;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Faculty extends Commons {
    public Faculty(Connection connection, String id, String role) {
        super(connection, id, role);
    }

    @Override
    public void showMenu(Scanner sc) {
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
            String body = String.format("Welcome to AIMS Portal (%s)", role.toUpperCase());
            cli.createVSubmenu("Menu",body, options);

//            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> showPersonalDetails(DataStorage._FACULTY, sc);
                    case "2" -> viewCourseCatalog(sc);
                    case "3" -> showCourseOffering(sc);
                    case "4" -> showFacultyRecord(sc);
                    case "5" -> studentRecordCumCgpaCalc(false, sc);
                    case "6" -> currentEvent(sc);
                    case "7" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    @Override
    boolean eventMenu(Scanner sc) {
        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        CLI cli = new CLI();
        cli.createVSubmenu("SubMenu", null, options);
//            Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "1" -> {
//                    back
                }
                default -> runner = true;
            }
        } while (runner);
        return false;
    }

    @Override
    boolean studentRecordMenu(String sId, StringBuilder TRANSCRIPT, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String input = sc.nextLine();
            switch (input) {
                case "1" -> {
//                    return back
                }
                default -> {
                    runner = true;
                }
            }
        } while(runner);
        return false;
    }

    @Override
    void deptCourseOfferingMenu(int year, String deptId, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
//            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            String inp = sc.nextLine();
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

    private void showFacultyRecord(Scanner sc) {
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

                    ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
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
//            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        if(dropCourse(sc))
                            outer = true;
                        else
                            runner = true;
                    }

                    case "2" -> {
                        uploadGrades(sc);
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

    protected boolean dropCourse(Scanner sc) {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_FLOAT_START) {
            System.out.println(DataStorage.ANSI_RED + "Currently you can't drop course" + DataStorage.ANSI_RESET);
            return false;
        }
//        Scanner sc = new Scanner(System.in);
        System.out.print("Enter courseId = ");
        String courseId = sc.nextLine();
        if(!dropCourseUtil(true, courseId)) {
            System.out.println(DataStorage.ANSI_RED + "you have not floated such course in the current semester" + DataStorage.ANSI_RESET);
            return false;
        }
        dropCourseUtil(false, courseId);
        return true;
    }

    private boolean dropCourseUtil(boolean search, String courseId) {
        ArrayList<ArrayList<String>> allDept = dUtil.getAllDept();
        if(search) {
            String query = String.format("""
                    SELECT *
                    FROM _%s
                    where lower(courseid) = lower('%s')
                        and lower(sid) = 'init' and
                        session = array[%d, %d]
                    """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
//                    System.out.println(query);
            return dUtil.runQuery(query, true);
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
                    dUtil.runQuery(query, false);
                }
            }
            String query = String.format("""
                   DELETE
                   FROM _%s
                   where lower(courseid) = lower('%s')
                   and session = array[%d, %d]
                    """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
            dUtil.runQuery(query, false);
        }
        return true;
    }

    private void uploadGrades(Scanner sc) {
        fetchEvent();
        if(_EVENT != DataStorage._GRADE_SUBMISSION_START) {
            System.out.println(DataStorage.ANSI_RED + "Grade submission is not allowed currently" + DataStorage.ANSI_RESET);
            return;
        }
        System.out.print("Enter courseId = ");
//        Scanner sc = new Scanner(System.in);
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
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
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
                dUtil.runQuery(query, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void courseCatalogUtilMenu(String deptid, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Float Course");
        options.add("Back");
        cli.createVSubmenu("Menu", null, options);

        //        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "1" -> {
                    floatCourse(deptid, sc);
                    runner = true;
                }
                case "2" -> {
//                            return back
                }
                default -> runner = true;
            }
        } while (runner);
    }

    public void floatCourse(String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_FLOAT_START) {
            System.out.println(DataStorage.ANSI_RED + "Currently not allowed to float course" + DataStorage.ANSI_RESET);
            return;
        }
        System.out.print("Enter the course ID = ");
//        Scanner sc = new Scanner(System.in);
        String courseId = sc.nextLine();
        String query = String.format("SELECT * FROM COURSE_CATALOG_%s where courseid = lower('%s')", deptId, courseId);
        if(!dUtil.runQuery(query, true)) {
            System.out.println(DataStorage.ANSI_RED + "Sorry couldnot find the course with this id" + DataStorage.ANSI_RESET);
            return;
        }
//        System.out.println("Got the course");
        ArrayList<ArrayList<String>> allDept = dUtil.getAllDept();
        for(var vl : allDept) {
            for(int yr = 1; yr <= 4; yr++) {
                query = String.format("SELECT * FROM y%d_%s_offering where courseid = lower('%s')", yr, vl.get(0), courseId);
                if(dUtil.runQuery(query, true)) {
                    System.out.println("Sorry this course has already been floated");
                    return;
                }
            }
        }
        float cgCriteria = (float) -1;
        if(isValidInp("Do the course have any cg criteria(y/n) = ", sc)) {
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
        yearsAllowed.add(isValidInp("Is 1st year allowed(y/n) = ", sc));
        yearsAllowed.add(isValidInp("Is 2nd year allowed(y/n) = ", sc));
        yearsAllowed.add(isValidInp("Is 3rd year allowed(y/n) = ", sc));
        yearsAllowed.add(isValidInp("Is 4th year allowed(y/n) = ", sc));

        ArrayList<Boolean> deptAllowed = new ArrayList<>();
        for (ArrayList<String> strings : allDept) {
            query  = String.format("SELECT * FROM course_catalog_%s where courseid = lower('%s')", strings.get(0), courseId);
            deptAllowed.add(dUtil.runQuery(query, true));
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
                    dUtil.runQuery(query, false);
                }
            }
        }
        query = String.format("select coursename from course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        String courseName  = dUtil.fetchTable(query).get(0).get(0);
        query = String.format("INSERT INTO _%s (courseid, coursename, sid, session) values('%s', '%s', 'init', array[%d, %d])",id, courseId, courseName, _CURR_SESSION[0], _CURR_SESSION[1]);
        dUtil.runQuery(query, false);
        System.out.println(DataStorage.ANSI_GREEN + "Course Floated Successfully" + DataStorage.ANSI_RESET);
    }
}
