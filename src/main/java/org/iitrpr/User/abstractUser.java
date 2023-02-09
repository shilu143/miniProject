package org.iitrpr.User;

import java.sql.*;
import java.util.*;

abstract class abstractUser {
    Map<String, String> deptMap = new HashMap<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    Connection connection;
    String id;
    Boolean isLoggedout;
    String role;
    abstractUser(Connection connection, String id, String role) {
        this.connection = connection;
        this.id = id;
        this.role = role;
        isLoggedout = false;
        System.out.println(ANSI_CYAN + "\n\nAIMS PORTAL WELCOMES U ;)\n" + ANSI_RESET);
    }

    protected ArrayList<String> fetchData() {
        PreparedStatement st = null;
        try {
            String query = String.format("SELECT * FROM %s as s " +
                    "INNER JOIN DEPARTMENT as d " +
                    "ON s.deptid = d.deptid " +
                    "WHERE LOWER(id) = LOWER('%s')", role, id);

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String name = null;
            String dept = null;
            String email = null;
            String contact = null;
            while(rs.next()) {
                name = rs.getString("name");
                dept = rs.getString("deptname");
                email = rs.getString("email");
                contact = rs.getString("contact");
            }

            assert name != null;
            ArrayList<String> result = new ArrayList<>();
            result.add(name.toUpperCase());
            result.add(id.toUpperCase());
            result.add(role.toUpperCase());
            result.add(dept);
            result.add(email);
            result.add(contact);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    abstract void showMenu();
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    void showPersonalDetails() {

    };
    abstract void editPersonalDetails();
    abstract void showAllCourse();
    protected void logout() {
        isLoggedout = true;
    }

}
