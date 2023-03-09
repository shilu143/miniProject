package org.iitrpr.User;

import org.iitrpr.Seeder;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;



public class StudentTest4 {

    private Connection connection;
    private InputStream stdin;
    private Seeder seeder;
    // Login to the database before each test
    @BeforeEach
    public void setUp() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/aimsdb";
        String user = "postgres";
        String password = "root";
        connection = DriverManager.getConnection(url, user, password);
        stdin = System.in;
        seeder = new Seeder();
        seeder.generateSchema(connection);
        seeder.fill(connection);
    }

    // Close the database connection after each test
    @AfterEach
    public void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    public void TestFirstCourseEnrollment() {


        Faculty mockFaculty = new Faculty(connection, "gunturi", "faculty");
        String query = "update event set _event = 1";
        mockFaculty.dUtil.runQuery(query, false);

        String input = "cs101\nn\ny\ny\ny\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        mockFaculty.floatCourse("cse", sc);
        System.setIn(stdin);


        query = "update event set _event = 3";
        mockFaculty.dUtil.runQuery(query, false);


        //courseEnroll
        Student student = new Student(connection, "2020csb1102", "student");
        input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        //courseDrop
        student = new Student(connection, "2020csb1102", "student");
        input = "3\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        query = "update event set _event = 1";
        mockFaculty.dUtil.runQuery(query, false);

        input = "cs101\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        mockFaculty.dropCourse(sc);
        System.setIn(stdin);


        query = "update event set _event = 0";
        mockFaculty.dUtil.runQuery(query, false);
    }


    @Test
    public void TestCourseAlreadyEnrolled() {


        Faculty mockFaculty = new Faculty(connection, "gunturi", "faculty");
        String query = "update event set _event = 1";
        mockFaculty.dUtil.runQuery(query, false);

        String input = "cs101\nn\ny\ny\ny\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        mockFaculty.floatCourse("cse", sc);
        System.setIn(stdin);


        query = "update event set _event = 3";
        mockFaculty.dUtil.runQuery(query, false);

//      enrolled a course
        Student student = new Student(connection, "2020csb1102", "student");
        input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);


//        Again Registering for the same course
        student = new Student(connection, "2020csb1102", "student");
        input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        //Dropping invalid Course
        student = new Student(connection, "2020csb1102", "student");
        input = "3\n1\ninvalidCourse\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        query = "update event set _event = 4";
        mockFaculty.dUtil.runQuery(query, false);

        //Dropping course after add/drop event closed
        student = new Student(connection, "2020csb1102", "student");
        input = "3\n1\ninvalidCourse\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        query = "update event set _event = 3";
        mockFaculty.dUtil.runQuery(query, false);

        //courseDrop
        student = new Student(connection, "2020csb1102", "student");
        input = "3\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);


        query = "update event set _event = 1";
        mockFaculty.dUtil.runQuery(query, false);

        input = "cs101\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        mockFaculty.dropCourse(sc);
        System.setIn(stdin);


        query = "update event set _event = 0";
        mockFaculty.dUtil.runQuery(query, false);
    }

    @Test
    void TestgraduationCheck() {
        Student student = new Student(connection, "2020csb1102", "student");
        String input = "5\n1\n6\n2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TeststudentRecord() {
        Student student = new Student(connection, "2020csb1102", "student");
        String input = "5\n1\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestEventCheck() {
        Student student = new Student(connection, "2020csb1102", "student");
        String input = "4\n1\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }


    @Test
    void TestViewStudentRecordAndExit() {
        Student student = new Student(connection, "2020csb1102", "student");
        String input = "3\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestCourseEnrollmentPrereqNotFullFill() {

        Faculty mockFaculty = new Faculty(connection, "gunturi", "faculty");
        String query = "update event set _event = 1";
        mockFaculty.dUtil.runQuery(query, false);

        String input = "cs201\nn\ny\ny\ny\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        mockFaculty.floatCourse("cse", sc);
        System.setIn(stdin);


        query = "update event set _event = 3";
        mockFaculty.dUtil.runQuery(query, false);

//      course prereq not fulfilled
        Student student = new Student(connection, "2020csb1102", "student");
        input = "2\n1\ncs201\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        seeder.generateSchema(connection);
        seeder.fill(connection);
    }
}
