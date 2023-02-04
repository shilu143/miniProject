package org.iitrpr;

//import picocli.CommandLine;
import org.iitrpr.models.User;
import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) {
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

        User student = new User(
                "shilu tudu",
                "2020CSB1126",
                "student",
                "cse",
                "2020csb1126@iitrpr.ac.in"
        );
        student.show();
    }
}