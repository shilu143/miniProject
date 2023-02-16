package org.iitrpr.User;

import org.iitrpr.utils.DataStorage;

import java.sql.*;
import java.util.*;

abstract class abstractUser {
    DataStorage dataStorage;

    Connection connection;
    String id;
    boolean isLoggedout;
    Integer[] _CURR_SESSION;
    int _EVENT;
    String role;
    abstractUser(Connection connection, String id, String role) {
        this.connection = connection;
        this.id = id;
        this.role = role;
        isLoggedout = false;
        dataStorage = new DataStorage();
        System.out.println(DataStorage.ANSI_CYAN + "\n\nAIMS PORTAL WELCOMES U ;)\n" + DataStorage.ANSI_RESET);
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
                name = rs.getString("name").trim();
                dept = rs.getString("deptname").trim();
                email = rs.getString("email").trim();
                contact = rs.getString("contact").trim();
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

    protected void fetchEvent() {
        String query = "SELECT * FROM EVENT";
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                _EVENT = rs.getInt("_EVENT");
                Array rsString = rs.getArray("_SESSION");
                if (rsString != null) {
                    _CURR_SESSION = (Integer[]) rsString.getArray();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    abstract void showMenu();
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    abstract void showPersonalDetails();
    abstract void editPersonalDetails();
    abstract void showAllCourse();
    protected void logout() {
        isLoggedout = true;
    }

}
