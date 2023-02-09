package org.iitrpr;

import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

public class Seeder {

    public void generateSchema(Connection connection) {
        try {
            String query = "";
            try {
                String file = "./src/SQL/sys.sql";
                query = new String(Files.readAllBytes(Paths.get(file)));
            } catch (IOException e) {
                System.out.print(e.getLocalizedMessage());
            }
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void fill(Connection connection) {
            try {
                File file1 = new File("./assets/data/seedHelper.csv");
                File file2 = new File("./assets/data/department.csv");
                Scanner scan1 = new Scanner(file1);
                Scanner scan2 = new Scanner(file2);
                scan1.nextLine();
                scan2.nextLine();

                while (scan1.hasNextLine()) {
                    String str = scan1.nextLine();
                    String[] input = str.split(",");
                    String name = input[0].toLowerCase();
                    String id = input[1].toLowerCase();
                    String role = input[2].toLowerCase();
                    String deptId = input[3].toLowerCase();
                    String email = input[4];
                    String contact = input[5];
                    String password = input[6];

                    String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt(12));

                    try {
                        PreparedStatement st = connection.prepareStatement("INSERT INTO _USER VALUES (?, ?, ?)");
                        st.setString(1, id);
                        st.setString(2, role);
                        st.setString(3, hashedPass);
                        st.executeUpdate();

                        String query = String.format("INSERT INTO %s VALUES (?, ?, ?, ?, ?)", role.toUpperCase());

                        st = connection.prepareStatement(query);
                        st.setString(1, name);
                        st.setString(2, id);
                        st.setString(3, deptId);
                        st.setString(4, email);
                        st.setString(5, contact);
                        st.executeUpdate();
                        st.close();
                    } catch (SQLException exception) {
                        System.out.println(exception.getMessage());
                    }
                }
                scan1.close();

                while(scan2.hasNextLine()) {
                    String str = scan2.nextLine();
                    String[] input = str.split(",");
                    String deptId = input[0];
                    String deptName = input[1];
                    try {
                        PreparedStatement st = connection.prepareStatement("INSERT INTO DEPARTMENT VALUES (?, ?)");
                        st.setString(1, deptId);
                        st.setString(2, deptName);
                        st.executeUpdate();
                        st.close();
                    } catch (SQLException exception) {
                        System.out.println(exception.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.print(e.getLocalizedMessage());
            }
    }
    public static void main(String[] args) {
        Seeder seed = new Seeder();
        Dotenv dotenv = Dotenv.configure().load();

        String USER = dotenv.get("DB_USER");
        String PASS = dotenv.get("DB_PASS");

        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/aimsdb";
        try {
            connection = DriverManager.getConnection(url, USER, PASS);
            if(connection != null) {
                System.out.println("Database Connection Successful");
                seed.generateSchema(connection);
                seed.fill(connection);
            }
            else {
                System.out.println("Database Connection Failed");
            }
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
