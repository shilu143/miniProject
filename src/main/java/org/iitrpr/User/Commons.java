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
            String inp = sc.nextLine();
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
            String inp = sc.nextLine();
            try{
                year = Integer.parseInt(inp);
                if(year < 1 || year > 4) {
                    failurePrint("Enter a valid input");
                }
                else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                failurePrint("Enter a valid input");
            }
        }
        showDeptOffering(deptId, year, sc);
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
//                System.out.println(value);
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



