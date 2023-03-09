package org.iitrpr.User;

import org.iitrpr.Seeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AcademicOfficeTest {
    private Connection connection;
    private InputStream stdin;
    private Seeder seeder;
    // Login to the database before each test
    @BeforeEach
    public void setUp() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/aimsdbtest";
        String user = "postgres";
        String password = "root";
        connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        stdin = System.in;
//        Seeder.main(new String[0]);
    }

    // Close the database connection after each test
    @AfterEach
    public void tearDown() throws SQLException {
        Seeder.main(new String[0]);
        connection.rollback();
        connection.setAutoCommit(true);
        connection.close();
    }

    @Test
    public void TestAcademicOfficePersonalDetails() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");

        String input = """
                1
                1
                testnum
                2
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestAcademicOfficeCourseCatalog() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");
        String input = """
                2
                3
                1
                cs555
                test coursename
                3,0,2
                y
                ge103
                pc
                2
                cs101
                y
                newtest course
                y
                3,0,0
                y
                ge103
                y
                e
                3
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestAcademicOfficeOffering() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");
        String input = """
                3
                3
                1
                1
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }


    @Test
    public void TestAcademicOfficeStudentRecord() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");
        String input = """
                4
                2020csb1102
                1
                2
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestAcademicOfficeViewAndEditEvent() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");
        String input = """
                5
                1
                2
                1
                3
                1
                4
                1
                5
                1
                6
                1
                7
                1
                8
                1
                1
                2
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestGraduationCheck() {
        AcademicOffice office = new AcademicOffice(connection, "deanoffice", "office");
        String input = """
                6
                2020csb1102
                1
                7
                """;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        office.showMenu(sc);
        System.setIn(stdin);
    }
}