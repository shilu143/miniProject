package org.iitrpr.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.iitrpr.utils.CLI;

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
            options.add("Is Graduated");
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
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
                    Array rsString = rs.getArray("session");

                    ArrayList<String> options = new ArrayList<>();
                    options.add("Course ID");
                    options.add("Course Name");
                    options.add("(L-T-P-S-C)");
                    options.add("Grade");

                    ArrayList<ArrayList<String>> data = new ArrayList<>();
                    Integer[] session = null;
                    if (rsString != null) {
                        session = (Integer[]) rsString.getArray();
                    }
                    assert session != null;
                    query = String.format("SELECT * FROM _%s " +
                            "WHERE session = ARRAY[%d, %d]", id, session[0], session[1]);

                    String header = String.format("Academic Session: %d-%d", session[0], session[1]);
                    stmt = connection.createStatement();
                    ResultSet rs2 = stmt.executeQuery(query);
                    float earnedCredits = 0;
                    float SGPA = 0;
                    float CGPA = 0;
                    while(rs2.next()) {
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add(rs2.getString("courseid"));
                        temp.add(rs2.getString("coursename"));
                        rsString = rs2.getArray("ltp");
                        Integer[] ltp = null;
                        if (rsString != null) {
                            ltp = (Integer[]) rsString.getArray();
                        }
                        assert ltp != null;
                        int cr = ltp[0] + ltp[2]/2;
                        int sc = 2*ltp[0]+ltp[2]/2-ltp[1];
                        earnedCredits += cr;
                        String ltpsc =String.format("(%d-%d-%d-%d-%d)",ltp[0], ltp[1], ltp[2], sc, cr);
                        temp.add(ltpsc);
                        temp.add(rs2.getString("grade"));
                        data.add(temp);
                    }
                    ArrayList<String> footerOptions = new ArrayList<>();
                    ArrayList<String> footerData = new ArrayList<>();
                    footerOptions.add("Earned Credits");
                    footerData.add(String.valueOf(earnedCredits));
                    footerOptions.add("SGPA");
                    footerData.add(String.valueOf(SGPA));
                    footerOptions.add("CGPA");
                    footerData.add(String.valueOf(CGPA));
                    cli.recordPrint(header, options, data, footerOptions, footerData);
                }
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
//                        editPersonalDetails();
                        outer = true;
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
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
