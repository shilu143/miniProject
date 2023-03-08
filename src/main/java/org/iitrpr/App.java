package org.iitrpr;

import org.iitrpr.User.*;
import java.sql.*;
import java.util.Scanner;
import io.github.cdimascio.dotenv.Dotenv;

import static java.lang.System.exit;


public class App {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    boolean SUCCESS = true;
    boolean FAILURE = false;
    public boolean authenticate(Connection connection, String username, String pass, Scanner sc) {
        String QUERY = String.format("SELECT * FROM _USER " +
                "WHERE LOWER(id) = LOWER('%s')", username);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY);
            String id = null;
            String role = null;
            String actualPass = null;

            if (rs.next()) {
                id = rs.getString("id");
                role = rs.getString("role");
                actualPass = rs.getString("pass");
            }
            if (id != null && actualPass.equals(pass)) {
                if(role.equalsIgnoreCase("student")) {
                    Student student = new Student(connection, id, role);
                    student.showMenu(sc);
                }
                else if(role.equalsIgnoreCase("faculty")) {
                    Faculty faculty = new Faculty(connection, id, role);
                    faculty.showMenu(sc);
                }
                else if(role.equalsIgnoreCase("office")) {
                    AcademicOffice acad = new AcademicOffice(connection, id, role);
                    acad.showMenu(sc);
                }
                else {
                    return FAILURE;
                }
                return SUCCESS;
            } else {
                System.out.println(ANSI_RED + "\nInvalid username or password\n" + ANSI_RESET);
                return FAILURE;
            }
        } catch (SQLException e) {
            return FAILURE;
        }
    }

    public static void main(String[] args) {
        App app = new App();
        Dotenv dotenv = Dotenv.configure().load();

        String USER = dotenv.get("DB_USER");
        String PASS = dotenv.get("DB_PASS");
        String dbName = "aimsdb";
        Connection connection = null;
        String url = String.format("jdbc:postgresql://localhost:5432/%s", dbName);
        try {
            connection = DriverManager.getConnection(url, USER, PASS);
            if (connection != null) {
                System.out.println("Database Connection Successful");
            } else {
                System.out.println("Database Connection Failed");
                exit(0);
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            exit(0);
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your ID : ");
        String username = sc.nextLine().trim();
        System.out.print("Enter your password : ");
        String password = sc.nextLine().trim();
        while (!app.authenticate(connection, username, password, sc)) {
            System.out.print("Enter your ID : ");
            username = sc.nextLine().trim();
            System.out.print("Enter your password : ");
            password = sc.nextLine().trim();
        }

        System.out.println("bye");
        sc.close();
    }
}