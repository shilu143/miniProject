package org.iitrpr;

import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Seeder {

    public void generateSchema(Connection connection) {
        try {
            String query = "";
            try {
                File file = new File("./src/SQL/sys.sql");
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    String str = scan.nextLine();
//                    Comment regex checking
                    if(Pattern.matches("(--.*)|(((/\\*)+?[\\w\\W]+?(\\*/)+))",str))
                        continue;
                    query = query.concat(str + " ");
                }
                scan.close();
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
//        try {
//            String query = "";
            try {
                File file = new File("./assets/seedHelper.csv");
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    String str = scan.nextLine();
                    String[] input = str.split(",");
                    String hashedPass = BCrypt.hashpw(input[5], BCrypt.gensalt(12));
                    try {
                        PreparedStatement pstmt = connection.prepareStatement("CALL CREATE_USER(?, ?, ?, ?, ?, ?)");
                        pstmt.setString(1, input[0]);
                        pstmt.setString(2, input[1]);
                        pstmt.setString(3, input[2]);
                        pstmt.setString(4, input[3]);
                        pstmt.setString(5, input[4]);
                        pstmt.setString(6, hashedPass);
                        pstmt.execute();
                        pstmt.close();
                    }catch (SQLException exception) {
                        System.out.println(exception.getMessage());
                    }
                }
                scan.close();
            } catch (IOException e) {
                System.out.print(e.getLocalizedMessage());
            }
//            Statement stmt = connection.createStatement();
//            stmt.execute(query);
//            stmt.close();
//        }
//        catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
    }
    public static void main(String[] args) {
        Seeder seed = new Seeder();
        Dotenv dotenv = Dotenv.configure().load();

        String USER = dotenv.get("DB_USER");
        String PASS = dotenv.get("DB_PASS");

        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/test";
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
