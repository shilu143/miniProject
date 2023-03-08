package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public abstract class Commons extends AbstractAll {

    Commons(Connection connection, String id, String role) {
        super(connection, id, role);
    }
    void viewStudentRecord(Scanner sc) {
        String sId = null;
//        Scanner sc = new Scanner(System.in);
        boolean isValid = false;
        while(!isValid) {
            System.out.print("Enter the Student ID = ");
            sId = sc.next();
            String query = String.format("select * from student where lower(id) = lower('%s')", sId);
            if(dUtil.runQuery(query, true)) {
                isValid = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "There is no student with that id" + DataStorage.ANSI_RESET);
            }
        }

        clearScreen();
        CLI cli = new CLI();
        String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", sId);
        float cumulativeEarnedCredits = 0;
        int totalSemester = 0;
        float cumulativeTotalGP = 0;
        try {
            //Event fetching
            fetchEvent();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int status = DataStorage._COMPLETED;
                Array rsString = rs.getArray("session");
                ArrayList<String> options = new ArrayList<>();
                options.add("Course ID");
                options.add("Course Name");
                options.add("(L-T-P-C)");
                options.add("Status");
                options.add("Grade");

                ArrayList<ArrayList<String>> data = new ArrayList<>();
                Integer[] session = null;
                if (rsString != null) {
                    session = (Integer[]) rsString.getArray();
                }
                assert session != null;

                if (Arrays.equals(_CURR_SESSION, session) && _EVENT < DataStorage._GRADE_SUBMISSION_END) {
                    status = DataStorage._RUNNING;
                }

                query = String.format("SELECT * FROM _%s " +
                        "WHERE session = ARRAY[%d, %d]", sId, session[0], session[1]);

                String header = String.format("Academic Session: %d-%d", session[0], session[1]);
                stmt = connection.createStatement();
                ResultSet rs2 = stmt.executeQuery(query);
                float earnedCredits = 0;
                float registeredCredits = 0;
                float SGPA = 0;
                float CGPA = 0;
                float totalGP = 0;
                while (rs2.next()) {
                    String courseId = rs2.getString("courseid")
                            .trim().
                            toUpperCase();
                    String courseName = rs2.getString("coursename").trim();
                    Integer[] ltp = null;
                    String ltpc = null;
                    String grade = rs2.getString("grade");


                    ArrayList<String> temp = new ArrayList<>();
                    rsString = rs2.getArray("ltp");
                    if (rsString != null) {
                        ltp = (Integer[]) rsString.getArray();
                    }
                    assert ltp != null;
                    int credits = ltp[0] + ltp[2] / 2;
                    ltpc = String.format("(%d-%d-%d-%d)", ltp[0], ltp[1], ltp[2], credits);


                    registeredCredits += credits;
                    if (status == DataStorage._COMPLETED) {
                        int gp = 0;
                        if(grade != null)
                            gp = dataStorage.GradePointMap.get(grade);
                        if (gp != 0) {
                            earnedCredits += credits;
                            cumulativeEarnedCredits += credits;
                        }
                        totalGP += (credits * gp);
                    }

                    temp.add(courseId);
                    temp.add(courseName);
                    temp.add(ltpc);
                    temp.add(status == DataStorage._COMPLETED ? "Completed" : "Running");
                    temp.add(grade!=null && status == DataStorage._COMPLETED ? grade : "N/A");
                    data.add(temp);
                }
                if (status == DataStorage._COMPLETED) {
                    totalSemester++;
                    cumulativeTotalGP += totalGP;
                    SGPA = (totalGP / registeredCredits);
                }
                if(cumulativeEarnedCredits == 0)
                    CGPA = 0;
                else
                    CGPA = (cumulativeTotalGP / cumulativeEarnedCredits);

                ArrayList<String> footerOptions = new ArrayList<>();
                ArrayList<String> footerData = new ArrayList<>();
                footerOptions.add("Registered Credits");
                footerData.add(String.valueOf(registeredCredits));
                footerOptions.add("Earned Credits");
                footerData.add(String.valueOf((status == DataStorage._COMPLETED) ? earnedCredits : "N/A"));
                footerOptions.add("SGPA");
                footerData.add(String.valueOf((status == DataStorage._COMPLETED) ? SGPA : "N/A"));
                footerOptions.add("CGPA");
                footerData.add(String.valueOf(CGPA));
                cli.recordPrint(header, options, data, footerOptions, footerData);
            }
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String input = sc.next();
            switch (input) {
                case "1" -> {
//                    return back
                }

                default -> {
                    runner = true;
                }
            }
        } while(runner);
    }

    void showCourseOffering(Scanner sc) {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = dUtil.getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) courseOfferingUtil(dept.get(vl - 1).get(0), sc);
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

    private void courseOfferingUtil(String deptId, Scanner sc) {
        boolean validInput = false;
        int year = 0;
        while(!validInput) {
            System.out.print("Enter year (1, 2, 3, 4) = ");
//            Scanner sc = new Scanner(System.in);
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
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
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
        data = dUtil.fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);


        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
//            Scanner sc = new Scanner(System.in);
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

    protected void viewCourseCatalog(Scanner sc) {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = dUtil.getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) viewCourseCatalogUtil(dept.get(vl - 1).get(0), sc);
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

    private void viewCourseCatalogUtil(String deptid, Scanner sc) {
        clearScreen();
        boolean outer;
        outer = false;
        CLI cli = new CLI();
        String query = String.format("SELECT * FROM course_catalog_%s", deptid);
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
        ArrayList<String> options = new ArrayList<>();
        options.add("Course ID");
        options.add("Course Name");
        options.add("LTP");
        options.add("Prerequisites");
        options.add("Type");
        options.add("Batch Onwards");
        cli.recordPrint(deptid.toUpperCase() + " Courses",options, data, null, null);
        courseCatalogUtilMenu(deptid, sc);
    }

    abstract  void courseCatalogUtilMenu(String deptId, Scanner sc);

    boolean isValidInp(String ques, Scanner sc) {
//        Scanner sc = new Scanner(System.in);
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

    public static boolean isIntegerArray(String input) {
        String[] values = input.split(",");
        for (String value : values) {
            try {
                Integer.parseInt(value.trim());
                System.out.println(value);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return values.length == 3;
    }

    public boolean checkPrereq(String input, String deptId) {
        String[] value = input.split(",");
        for(var val : value) {
            if(!dUtil.findCourse(val.trim(), deptId)) {
                System.out.println("Course " + val + " is not present in the catalog");
                return false;
            }
        }
        return true;
    }

    public boolean changeCourseType(String type, String deptId, String courseId) {
        type = type.trim();
        if(!type.equalsIgnoreCase("pc") && !type.equalsIgnoreCase("ec") && !type.equalsIgnoreCase("e")) {
            return false;
        }
        String query = String.format("SELECT * FROM course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                String courseName = rs.getString("coursename");
                Array ltp = rs.getArray("ltp");
                Array prereq = rs.getArray("prereq");
                int batch = _CURR_SESSION[0] + 1;
                query = String.format("insert into course_catalog_%s values(?,?,?,?,?,?)", deptId);
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, courseId);
                pstmt.setString(2, courseName);
                pstmt.setArray(3, ltp);
                pstmt.setArray(4, prereq);
                pstmt.setString(5, type);
                pstmt.setInt(6, batch);
                pstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}



