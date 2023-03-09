package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;
import org.iitrpr.utils.fileWriterUtil;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AcademicOffice extends Commons{
    public AcademicOffice(Connection connection, String id, String role) {
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
            options.add("View Student Record");
            options.add("Show Current Event");
            options.add("Check Graduation");
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
                    case "1" -> showPersonalDetails(DataStorage._OFFICE, sc);
                    case "2" -> viewCourseCatalog(sc);
                    case "3" -> showCourseOffering(sc);
                    case "4" -> studentRecordCumCgpaCalc(false, sc);
                    case "5" -> currentEvent(sc);
                    case "6" -> graduationCheck(sc);
                    case "7" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    @Override
    boolean eventMenu(Scanner sc) {
        boolean outer = false;
        ArrayList<String> options = new ArrayList<>();
        options.add("Create New Event");
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
                    createNewEvent(sc);
                    outer = true;
                }
                case "2" -> {
//                    back
                }
                default -> runner = true;
            }
        } while (runner);
        return outer;
    }

    @Override
    void courseCatalogUtilMenu(String deptId, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Add New Course");
        options.add("Edit Course");
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
                    addNewCourseinCatalog(deptId, sc);
                    runner = true;
                }
                case "2" -> {
                    editCourseCatalog(deptId, sc);
                    runner = true;
                }

                case "3" -> {
                    //                    return back
                }
                default -> runner = true;
            }
        } while (runner);
    }

    public void addNewCourseinCatalog(String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry currently you can't edit the course Catalog");
            return;
        }
//        Scanner sc = new Scanner(System.in);
        String courseId = null;
        String courseName = null;
        Integer[] ltp = new Integer[3];
        String[] prereq = null;
        String type = null;
        int batch = _CURR_SESSION[0];
        boolean validInput = false;
        while(!validInput) {
            System.out.print("Enter the course ID = ");
            courseId = sc.nextLine();
            String query = String.format("SELECT * FROM COURSE_CATALOG_%s where LOWER(courseid) = LOWER('%s')", deptId, courseId);
            if(dUtil.runQuery(query, true)) {
                System.out.println(DataStorage.ANSI_RED + "There is already a course with that id" + DataStorage.ANSI_RESET);
            }
            else {
                validInput = true;
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter Course name = ");
            String name = sc.nextLine();
            System.out.println(name);
            if(name != null) {
                courseName = name;
                validInput = true;
            }
        }
        validInput = false;
        while(!validInput) {
            System.out.print("Enter ltp structure (l,t,p) = ");
            String temp = sc.nextLine();
            if(isIntegerArray(temp)) {
                String[] ttt = temp.split(",");
                System.out.println(Arrays.toString(ttt));
                for(int i = 0; i < ttt.length; i++) ltp[i] = Integer.parseInt(ttt[i].trim());
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter prerequisites (comma separated courseId) = ");
            String tt = sc.nextLine();
            if(checkPrereq(tt, deptId)) {
                prereq = tt.split(",");
                for(int i = 0; i < prereq.length; i++) prereq[i] = prereq[i].trim();
                validInput = true;
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter the course type(pc or ec or e) = ");
            type = sc.nextLine().trim();
            if(type.equalsIgnoreCase("pc") || type.equalsIgnoreCase("ec") || type.equalsIgnoreCase("e")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "enter valid input" + DataStorage.ANSI_RESET);
            }
        }


        String query = String.format("insert into course_catalog_%s values(?,?,?,?,?,?)", deptId);
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, courseId);
            pstmt.setString(2, courseName);
            Array arr = connection.createArrayOf("INTEGER", ltp);
            pstmt.setArray(3, arr);
            arr = connection.createArrayOf("VARCHAR", prereq);
            pstmt.setArray(4, arr);
            pstmt.setString(5, type);
            pstmt.setInt(6, batch);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void editCourseCatalog(String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry you can't edit the course Catalog");
            return;
        }
//        Scanner sc = new Scanner(System.in);
        String courseId = null;
        boolean validInput = false;
        while(!validInput) {
            System.out.print("Enter the course ID = ");
            courseId = sc.nextLine();
            String query = String.format("SELECT * FROM COURSE_CATALOG_%s where LOWER(courseid) = LOWER('%s')", deptId, courseId);
            if(!dUtil.runQuery(query, true)) {
                System.out.println(DataStorage.ANSI_RED + "There is no such course in the catalog with this id" + DataStorage.ANSI_RESET);
            }
            else {
                validInput = true;
            }
        }
        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the course Name (y/n) = ");
            String input = sc.nextLine().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter Course name = ");
                    String name = sc.nextLine();
                    System.out.println(name);
                    if(name != null) {
                        String query = String.format("UPDATE COURSE_CATALOG_%s SET coursename = '%s' where lower(courseid) = lower('%s')", deptId, name, courseId);
                        dUtil.runQuery(query, false);
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the ltp structure (y/n) = ");
            String input = sc.nextLine().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter ltp (l,t,p) = ");
                    input = sc.nextLine();
                    System.out.println(input);
                    if(isIntegerArray(input)) {
                        String[] val = input.split(",");
                        System.out.println(Arrays.toString(val));
                        String query = String.format("UPDATE COURSE_CATALOG_%s SET ltp = array%s where lower(courseid) = lower('%s')",
                                deptId, Arrays.toString(val), courseId);
                        dUtil.runQuery(query, false);
                        validInput = true;
                    }
                    else {
                        System.out.println("not a valid structure");
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the prerequisites (y/n) = ");
            String input = sc.nextLine().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter prerequisites (comma separated courseId) = ");
                    input = sc.nextLine();
                    if(checkPrereq(input, deptId)) {
                        String[] val = input.split(",");
                        for(int i = 0; i < val.length; i++) val[i] = val[i].trim();
                        String query = String.format("UPDATE course_catalog_%s set prereq = ? where lower(courseid) = lower('%s')", deptId, courseId);
                        try {
                            PreparedStatement pstmt = connection.prepareStatement(query);
                            Array arr = connection.createArrayOf("VARCHAR", val);
                            pstmt.setArray(1, arr);
                            pstmt.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to change the course type (y/n) = ");
            String input = sc.nextLine().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter new Course Type (pc or ec or e) = ");
                    input = sc.nextLine();
                    if(changeCourseType(input, deptId, courseId)) {
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }
    }

    private void createNewEvent(Scanner sc) {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Start new Semester");
        options.add("Start Course Float Event");
        options.add("End Course Float Event");
        options.add("Start Course Registration Event");
        options.add("End Course Registration Event");
        options.add("Start Grade Submission Event");
        options.add("End Grade Submission Event");
        options.add("End the Semester");
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 &&  vl <= 8) {
                    int temp = vl - 2;
                    temp = (temp < 0 ? DataStorage._SEMESTER_END : temp);
                    if(_EVENT == temp) {
                        String query;
                        if(vl - 1 == DataStorage._SEMESTER_START) {
                            if(_CURR_SESSION[1] == 2) {
                                _CURR_SESSION[0]++;
                                _CURR_SESSION[1] = 1;
                            }
                            else {
                                _CURR_SESSION[1] = 2;
                            }
                            ArrayList<ArrayList<String>> Alldept = dUtil.getAllDept();
                            for(var dept : Alldept) {
                                for(int yr = 1; yr <= 4; yr++) {
                                    query = String.format("DELETE FROM y%d_%s_offering", yr, dept.get(0));
                                    dUtil.runQuery(query, false);
                                }
                            }
                            query = String.format("UPDATE EVENT SET _EVENT = 0, " +
                                    "_SESSION = ARRAY[%d, %d]", _CURR_SESSION[0], _CURR_SESSION[1]);
                        }
                        else {
                            query = String.format("UPDATE EVENT SET _EVENT = %d", vl - 1);
                        }
                        dUtil.runQuery(query, false);

                    }
                    else {
                        System.out.println(DataStorage.ANSI_RED + "Not allowed to create this Event" + DataStorage.ANSI_RESET);
                        runner = true;
                    }
                }
                else if (vl == 9) {
//                    BACK
                }
                else {
                    runner = true;
                }
            }
            catch (NumberFormatException exception){
                runner = true;
            }
        } while(runner);
    }

    @Override
    boolean studentRecordMenu(String sId, StringBuilder TRANSCRIPT, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Generate Transcript");
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String input = sc.nextLine();
            switch (input) {
                case "1" -> {
//                    generate transcript
                    fileWriterUtil.generateTranscript(sId, TRANSCRIPT.toString());
                    System.out.println(DataStorage.ANSI_GREEN + "Transcript Generated successfully in the documents folder" + DataStorage.ANSI_RESET);
                    runner = true;
                }
                case "2" -> {
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

}
