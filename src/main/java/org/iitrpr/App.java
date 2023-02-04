package org.iitrpr;

import org.iitrpr.utils.Table;
import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.*;
import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public void authenticate(Connection connection, String username, String pass) {
        String QUERY = String.format("SELECT * FROM AIMS_USER " +
                "WHERE LOWER(id) = LOWER('%s')", username);
        try {
            Statement stmt=connection.createStatement();
            ResultSet rs=stmt.executeQuery(QUERY);
            String hashedpass = null;
            String id = null;
            String name = null;
            String email = null;
            String role = null;
            String dept = null;
            while (rs.next()) {
                id = rs.getString("id");
                name = rs.getString("name");
                email = rs.getString("email");
                role = rs.getString("role");
                dept = rs.getString("dept");
                hashedpass = rs.getString("hashedpass");
            }
            if (BCrypt.checkpw(pass, hashedpass)) {
                System.out.println("AIMS Portal Welcomes u ;)");
                Table table = new Table();
                String[] headers = {"NAME", "ID", "ROLE", "DEPT.", "EMAIL"};
                String[] data = {name, id, role, dept, email};
                table.setHeaders(headers);
                table.addRow(data);
                table.print();
            }
            else
                System.out.println("Invalid username or password");
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
        String url = "jdbc:postgresql://localhost:5432/test";
        try {
            connection = DriverManager.getConnection(url, USER, PASS);
            if(connection != null) {
                System.out.println("Database Connection Successful");
            }
            else {
                System.out.println("Database Connection Failed");
            }
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your ID : ");
//        String username = sc.nextLine();
        System.out.print("Enter your password : ");
//        String password = sc.nextLine();
//        Console console = System.console();
//        if(console == null) {
//            System.out.println("bc");
//        }
//        String username = console.readLine("Username: ");
//        char[] password = console.readPassword("Password: ");
//        app.authenticate(connection, username, password);
    }
}