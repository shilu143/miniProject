package org.iitrpr.User;

import org.iitrpr.utils.fileWriterUtil;
import org.iitrpr.utils.CLI;
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

    protected String getFacultyName(String fid) {
        String query = String.format("SELECT name from faculty where LOWER(id) = LOWER('%s')", fid);
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
    protected boolean runQuery(String query, boolean response) {
        try {
            Statement stmt = connection.createStatement();
            if(response) {
                ResultSet rs = stmt.executeQuery(query);
                return rs.isBeforeFirst();
            }
            else {
                stmt.execute(query);
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


//    protected
    protected ArrayList<ArrayList<String>> getAllDept() {
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        try {
            String query = "SELECT * FROM DEPARTMENT";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                ArrayList<String> temp = new ArrayList<>();
                String deptid = rs.getString("deptid");
                if(!deptid.equalsIgnoreCase("acad")) {
                    temp.add(rs.getString("deptid"));
                    temp.add(rs.getString("deptname"));
                    data.add(temp);
                }
            }
            return data;
        } catch (SQLException e) {
            return null;
        }

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

    void showPersonalDetails(Integer usr) {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> options = new ArrayList<>();
            options.add("name");
            options.add("id");
            options.add("role");
            if(usr == DataStorage._STUDENT)
                options.add("batch");
            options.add("department");
            options.add("email");
            options.add("contact no.");
            ArrayList<String> data = fetchData();
            if(usr == DataStorage._STUDENT)
                data.add(3, id.substring(0,4));//extra
            cli.createVerticalTable("Personal Details", options, data);

            options = new ArrayList<>();
            options.add("Edit");
            options.add("Back");



            cli.createVSubmenu("SubMenu", null, options);
            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        editPersonalDetails();
                        outer = true;
                    }
                    case "2" -> {
                        //returns to previous method
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
    }



    protected void viewCourseCatalog() {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) showDeptCourse(dept.get(vl - 1).get(0));
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

    private void showDeptCourse(String deptid) {
        clearScreen();
        boolean outer;
        do {
            outer = false;
            CLI cli = new CLI();
            String query = String.format("SELECT * FROM course_catalog_%s", deptid);
            ArrayList<ArrayList<String>> data = fetchTable(query);
            ArrayList<String> options = new ArrayList<>();
            options.add("Course ID");
            options.add("Course Name");
            options.add("LTP");
            options.add("Prerequisites");
            options.add("Type");
            options.add("Batch Onwards");
            cli.recordPrint(deptid.toUpperCase() + " Courses",options, data, null, null);
            options = new ArrayList<>();
            if(role.equalsIgnoreCase("office")) {
                options.add("Add New Course");
                options.add("Edit Course");
            }
            else if(role.equalsIgnoreCase("faculty")) {
                options.add("Float Course");
            }
            options.add("Back");
            cli.createVSubmenu("Menu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine().trim();
                if(role.equalsIgnoreCase("office")) {
                    switch (inp) {
                        case "1" -> {
                            addNewCourseinCatalog(deptid);
                            runner = true;
                        }
                        case "2" -> {
                            editCourseCatalog(deptid);
                            runner = true;
                        }

                        case "3" -> {
                            //                    return back
                        }
                        default -> runner = true;
                    }
                }
                else {
                    switch (inp) {
                        case "1" -> {
                            floatCourse(deptid);
                            runner = true;
                        }
                        case "2" -> {
//                            return back
                        }
                        default -> runner = true;
                    }
                }
            } while(runner);
        }   while(outer);
    }

    protected abstract void floatCourse(String deptid);


    protected abstract void editCourseCatalog(String deptid);

    protected abstract void addNewCourseinCatalog(String deptid);





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

    protected ArrayList<ArrayList<String>> fetchTable(String query) {
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                ArrayList<String> temp = new ArrayList<>();
                ResultSetMetaData rsmd = rs.getMetaData();
                int n = rsmd.getColumnCount();
                for(int i = 1;i <= n; i++) {
                    String tp = rs.getString(i);
                    temp.add(tp == null ? "":tp);
                }
                data.add(temp);
            }
            return data;
        } catch (SQLException e) {
            return null;
//            throw new RuntimeException(e);
        }
    }

    protected ResultSet getResultSet(String query) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs;
        } catch (SQLException e) {
            return null;
//            throw new RuntimeException(e);
        }
    }

    abstract void showMenu();
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
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
    protected void logout() {
        isLoggedout = true;
    }
}
