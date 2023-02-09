package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class AcademicOffice extends abstractUser{
    public AcademicOffice(Connection connection, String id, String role) {
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
                    case "2" -> showAllCourse();
                    case "3" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    @Override
    void showPersonalDetails() {
        clearScreen();
        CLI cli = new CLI();

        ArrayList<String> headers = new ArrayList<>();
        headers.add("name");
        headers.add("id");
        headers.add("role");
        headers.add("department");
        headers.add("email");
        headers.add("contact no.");
        ArrayList<String> data = fetchData();
        cli.createVerticalTable(headers, data);

        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        options.add("Logout");


        cli.createMenu(2, "SubMenu", null, options);

        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "0" -> {
                    //returns to previous method
                }
                case "1" -> logout();
                default -> runner = true;
            }
        } while(runner);
    }

    @Override
    void editPersonalDetails() {

    }

    @Override
    public void showAllCourse() {

    }

}
