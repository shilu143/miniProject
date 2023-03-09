package org.iitrpr;

import org.iitrpr.User.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SeederTest {
    private Connection connection;
    @BeforeEach
    public void setUp() {
        // set up database connection
        String USER = "postgres";
        String PASS = "root";
        String dbName = "aimsdb";
        String url = String.format("jdbc:postgresql://localhost:5432/%s", dbName);
        try {
            connection = DriverManager.getConnection(url, USER, PASS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void seederTest() {
        Seeder ss = new Seeder();
        assertTrue(ss.generateSchema(connection));
        assertTrue(ss.fill(connection));
    }

    @AfterEach
    public void tearDown() {
        // close database connection
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}