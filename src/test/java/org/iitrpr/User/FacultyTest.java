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

class FacultyTest {

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
    public void TestFloatCourse() {
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");

        String query = "update event set _event = 1";
        faculty.dUtil.runQuery(query, false);

        String input = """
                2
                3
                1
                cs201
                y
                numberformatexception
                11
                5
                y
                y
                y
                y
                1
                cs201
                1
                cp301
                y
                5
                n
                n
                n
                n
                2
                7""";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestViewFacultyRecord1() {
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");
        String input = """
                4
                4
                4
                1
                2
                testcourse
                3
                4
                7""";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }


    @Test
    public void TestFacultyCourseDefloat() {
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");

        String query = "update event set _event = 1";
        faculty.dUtil.runQuery(query, false);

        String input = """
                2
                3
                1
                cp301
                y
                55
                3
                y
                y
                y
                y
                2
                4
                1
                cp301
                4
                7""";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }


    @Test
    public void gradeUpload() {
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");

        String query = "update event set _event = 1";
        faculty.dUtil.runQuery(query, false);

        String input = """
                2
                3
                1
                cs101
                n
                y
                n
                n
                n
                2
                7""";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);

        query = "update event set _event = 3";
        faculty.dUtil.runQuery(query, false);

        input = """
                2
                1
                cs101
                2
                6
                """;
        Student mockStudent = new Student(connection, "2020csb1102", "student");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        mockStudent.showMenu(sc);
        System.setIn(stdin);


        query = "update event set _event = 5";
        faculty.dUtil.runQuery(query, false);

        input = """
                4
                2
                cs101
                3
                testCourse
                3
                cs101
                /home/shilu/Documents/AimsPortal/GradeSubmission/Test.csv
                4
                7
                """;
        faculty = new Faculty(connection, "gunturi", "faculty");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void testFacultyPersonalDetails() {
        String input = """
                1
                1
                testnum
                2
                7
                """;
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }


    @Test
    public void testFacultyCourseOffering() {
        String input = """
                3
                3
                1
                1
                7
                """;
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestFacultyStudentRecord() {
        String input = """
                5
                2020csb1102
                1
                7
                """;
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void TestFacultyEvent() {
        String input = """
                6
                1
                7
                """;
        Faculty faculty = new Faculty(connection, "gunturi", "faculty");
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        faculty.showMenu(sc);
        System.setIn(stdin);
    }



}