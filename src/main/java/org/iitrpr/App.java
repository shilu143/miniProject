package org.iitrpr;

import org.iitrpr.User.*;
import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.*;
import io.github.cdimascio.dotenv.Dotenv;


public class App {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";







    boolean SUCCESS = true;
    boolean FAILURE = false;
    public boolean authenticate(Connection connection, String username, String pass) {
        String QUERY = String.format("SELECT * FROM _USER " +
                "WHERE LOWER(id) = LOWER('%s')", username);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY);
            String id = null;
            String role = null;
            String hashedpass = null;

            while (rs.next()) {
                id = rs.getString("id");
                role = rs.getString("role");
                hashedpass = rs.getString("hashedpass");
            }
            if (id != null && BCrypt.checkpw(pass, hashedpass)) {
                if(role.equalsIgnoreCase("student")) {
                    Student student = new Student(connection, id);
                    student.showMenu();
                }
                else if(role.equalsIgnoreCase("faculty")) {
                    Faculty faculty = new Faculty(connection, id);
                    faculty.showMenu();
                }
                else if(role.equalsIgnoreCase("office")) {
                    AcademicOffice acad = new AcademicOffice(connection, id);
                    acad.showMenu();
                }
                else {
                    System.out.println(ANSI_RED + "No Such role exist" + ANSI_RESET);
                }
                return SUCCESS;
            } else {
                System.out.println(ANSI_RED + "\nInvalid username or password\n" + ANSI_RESET);
                return FAILURE;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        App app = new App();
        Dotenv dotenv = Dotenv.configure().load();

        String USER = dotenv.get("DB_USER");
        String PASS = dotenv.get("DB_PASS");

        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/aimsdb";
        try {
            connection = DriverManager.getConnection(url, USER, PASS);
            if (connection != null) {
                System.out.println("Database Connection Successful");
            } else {
                System.out.println("Database Connection Failed");
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your ID : ");
        String username = sc.nextLine();
        System.out.print("Enter your password : ");
        String password = sc.nextLine();
        while (!app.authenticate(connection, username, password)) {
            System.out.print("Enter your ID : ");
            username = sc.nextLine();
            System.out.print("Enter your password : ");
            password = sc.nextLine();
        }
//        boolean loggedout = false;
//        while (!loggedout) {
//            System.out.print("> ");
//            String inp = sc.nextLine();
//            if(inp.equals("q") || inp.equals("quit")) {
//                loggedout = true;
//            }
//            else if(inp.equals("clear")) {
//                System.out.print("\033[H\033[2J");
//                System.out.flush();
//            }
//        }

        System.out.println("bye");
        sc.close();
    }
}