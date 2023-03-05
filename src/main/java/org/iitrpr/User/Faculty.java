package org.iitrpr.User;
import org.iitrpr.utils.CLI;

import java.sql.*;
import java.util.ArrayList;
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
            options.add("Edit Details");
            options.add("Show Courses");
            options.add("Logout");

            CLI cli = new CLI();
            cli.createMenu(4, "Main Menu", "Welcome to AIMS Portal", options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "0" -> showPersonalDetails();
                    case "1" -> editPersonalDetails();
//                    case "2" -> showAllCourse();
                    case "3" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    @Override
    void showPersonalDetails() {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> Options = new ArrayList<>();
            Options.add("name");
            Options.add("id");
            Options.add("role");
            Options.add("department");
            Options.add("email");
            Options.add("contact no.");
            ArrayList<String> data = fetchData();
            cli.createVerticalTable("Personal Details", Options, data);

            ArrayList<String> options = new ArrayList<>();
            options.add("Back");
            options.add("Edit");


            cli.createMenu(2, "SubMenu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "0" -> {
                        //returns to previous method
                    }
                    case "1" -> {
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


}
