package org.iitrpr.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

public class Student extends abstractUser {
    public Student(Connection connection, String id, String role) {
        super(connection, id, role);
    }

    @Override
    public void showMenu() {
        do {
            clearScreen();

            ArrayList<String> options = new ArrayList<>();
            options.add("Personal Details");
            options.add("Student Record");
            options.add("Course Offering");
            options.add("Show Current Event");
            options.add("Check Graduation");
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
                    case "1" -> showPersonalDetails();
                    case "2" -> studentRecord();
                    case "3" -> showCourseOffering();
                    case "4" -> showCurrentEvent();
                    case "5" -> isGraduated();
                    case "6" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    private void isGraduated() {
    }

    private void showCurrentEvent() {
    }

    private void showCourseOffering() {
    }

    private void studentRecord() {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();
            String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", id);
            float cumulativeEarnedCreated = 0;
            float cumulativeSGPA = 0;
            int totalSemester = 0;
            float cumulativeTotalGP = 0;
            try {
                //Event fetching
                fetchEvent();

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
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

                    if(Arrays.equals(_CURR_SESSION, session) && _EVENT < DataStorage._GRADE_SUBMISSION_END) {
                        status = DataStorage._RUNNING;
                    }

                    query = String.format("SELECT * FROM _%s " +
                            "WHERE session = ARRAY[%d, %d]", id, session[0], session[1]);

                    String header = String.format("Academic Session: %d-%d", session[0], session[1]);
                    stmt = connection.createStatement();
                    ResultSet rs2 = stmt.executeQuery(query);
                    float earnedCredits = 0;
                    float registeredCredits = 0;
                    float SGPA = 0;
                    float CGPA = 0;
                    float totalGP = 0;
                    while(rs2.next()) {
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
                        int credits = ltp[0] + ltp[2]/2;
                        ltpc =String.format("(%d-%d-%d-%d)",ltp[0], ltp[1], ltp[2], credits);



                        registeredCredits += credits;
                        if(status == DataStorage._COMPLETED) {
                            int gp = dataStorage.GradePointMap.get(grade);
                            if (gp != 0) {
                                earnedCredits += credits;
                                cumulativeEarnedCreated += credits;
                            }
                            totalGP += (credits * gp);
                        }

                        temp.add(courseId);
                        temp.add(courseName);
                        temp.add(ltpc);
                        temp.add(status == DataStorage._COMPLETED ? "Completed" : "Running");
                        temp.add(status == DataStorage._COMPLETED ? grade : "N/A");
                        data.add(temp);
                    }
                    if(status == DataStorage._COMPLETED) {
                        totalSemester++;
                        cumulativeTotalGP += totalGP;
                        SGPA = (totalGP / earnedCredits);
                        cumulativeSGPA += SGPA;
                    }
                    CGPA = (cumulativeTotalGP / cumulativeEarnedCreated);

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
            options.add("Course Drop");
            cli.createVSubmenu("SubMenu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        //returns to previous method
                    }
                    case "2" -> {
                        if(courseDrop()) {
                            outer = true;
                        }
                        else {
                            inner = true;
                        }
                    }
                    default -> inner = true;
                }
            } while (inner);
        } while(outer);
    }

    private boolean courseDrop() {
        fetchEvent();
        Integer[] session = new Integer[2];
        if(_EVENT == DataStorage._COURSE_REG_START) {
            System.out.print("Enter the CourseID : ");
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine().trim();

            String query = String.format("SELECT * FROM _%s WHERE courseid = LOWER('%s')", id, input);
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (!rs.isBeforeFirst() ) {
                    System.out.println("There is no such course that you have registered ");
                }
                while(rs.next()) {
                    String courseid = rs.getString("courseid");
                    Array rsString = rs.getArray("session");
                    if (rsString != null) {
                        session = (Integer[]) rsString.getArray();
                    }
                    fetchEvent();
                    if(Arrays.equals(session, _CURR_SESSION)) {
                        if(_EVENT == DataStorage._COURSE_REG_START) {
                            query = String.format("DELETE FROM _%s WHERE courseid = LOWER('%s')", id, courseid);
                            stmt = connection.createStatement();
                            stmt.execute(query);
                            System.out.println("Course Dropped Successfully");
                            return true;
                        }
                        else {
                            System.out.println("Course register/drop event has ended");
                        }
                    }
                    else {
                        System.out.println("Sorry, you can't drop this course");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else if(_EVENT >= DataStorage._COURSE_FLOAT_END) {
            System.out.println("Sorry, Course Register/Drop Event has Ended");
        }
        else {
            System.out.println("Sorry, Course Register/Drop Event has not yet Started");
        }
        return false;
    }

    @Override
    void showPersonalDetails() {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> headers = new ArrayList<>();
            headers.add("name");
            headers.add("id");
            headers.add("role");
            headers.add("batch");
            headers.add("department");
            headers.add("email");
            headers.add("contact no.");
            ArrayList<String> data = fetchData();
            data.add(3, id.substring(0,4));
            cli.createVerticalTable(headers, data);

            ArrayList<String> options = new ArrayList<>();
            options.add("Back");
            options.add("Edit");


//            cli.createMenu(2, "SubMenu", null, options);
            cli.createVSubmenu("SubMenu", null, options);
            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        //returns to previous method
                    }
                    case "2" -> {
                        editPersonalDetails();
                        outer = true;
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
    }

    @Override
    void editPersonalDetails() {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> headers = new ArrayList<>();
            headers.add("name");
            headers.add("id");
            headers.add("role");
            headers.add("batch");
            headers.add("department");
            headers.add("email");
            headers.add("contact no.");
            ArrayList<String> data = fetchData();
            data.add(3, id.substring(0,4));
            cli.createVerticalTable(headers, data);

            ArrayList<String> options = new ArrayList<>();
            options.add("Back");
            options.add("Edit");


//            cli.createMenu(2, "SubMenu", null, options);
            cli.createVSubmenu("SubMenu", null, options);
            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        //returns to previous method
                    }
                    case "2" -> {
                        editPersonalDetails();
                        outer = true;
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
        System.out.print("Enter your new Contact number = ");
        Scanner sc = new Scanner(System.in);
        String newContact = sc.nextLine();
        String query = String.format(
                "UPDATE %s " +
                        "SET contact = '%s' " +
                        "WHERE " +
                        "id = '%s'", role, newContact, id);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void showAllCourse() {
    }
}
