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
//        Seeder.main(new String[0]);
        connection.rollback();
        connection.setAutoCommit(true);
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
    }

    @Test
    void TestCourseEnrollmentWithOFFevent() {
        //Setting event to course Registration
//        String input = "5\n1\n2\n1\n3\n1\n4\n2\n6\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
//        Scanner sc = new Scanner(System.in);
//        AcademicOffice mockOffice = new AcademicOffice(connection, "deanoffice", "office");
//        mockOffice.showMenu(sc);
//        System.setIn(stdin);

//      course not course register/deregister event
        Student student = new Student(connection, "2020csb1102", "student");
        String input = "2\n1\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestCourseEnrollmentWithInvalidCourse() {
        //Setting event to course Registration
        String input = "5\n1\n2\n1\n3\n1\n4\n2\n7\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        AcademicOffice mockOffice = new AcademicOffice(connection, "deanoffice", "office");
        mockOffice.showMenu(sc);
        System.setIn(stdin);

//      course not course register/deregister event
        Student student = new Student(connection, "2020csb1102", "student");
        input = "2\n1\ninvalidCourse\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestCourseEnrollmentWithCreditLimitExceed() {
        Student student = new Student(connection, "2020csb1102", "student");
        String query = "update event set _event = 3";
        student.dUtil.runQuery(query, false);

        query = "insert into y1_cse_offering values ('cs101', null, 'gunturi')";
        student.dUtil.runQuery(query, false);

        query = "update ug_req set sem1 = 0";
        student.dUtil.runQuery(query, false);

        String input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestCourseEnrollmentAlreadyDonetheCourse() {
        Student student = new Student(connection, "2020csb1102", "student");
        String query = "update event set _event = 3";
        student.dUtil.runQuery(query, false);

        query = "insert into y1_cse_offering values ('cs101', null, 'gunturi')";
        student.dUtil.runQuery(query, false);

//        query = "update ug_req set sem1 = 0";
//        student.dUtil.runQuery(query, false);

        String input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);

        query = "update _2020csb1102 set grade = 'A'";
        student.dUtil.runQuery(query, false);
        query = "update event set _session = array[2020,2]";
        student.dUtil.runQuery(query, false);

        input = "2\n1\ncs101\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestCapstoneEnrollmentInvalidFID() {
        Student student = new Student(connection, "2020csb1102", "student");
        String query = "update event set _event = 3";
        student.dUtil.runQuery(query, false);

        query = "insert into y1_cse_offering values ('cp301', null, 'gunturi')";
        student.dUtil.runQuery(query, false);

//        query = "update ug_req set sem1 = 0";
//        student.dUtil.runQuery(query, false);

        String input = "2\n1\ncp301\ninvalidID\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestEnrollmentWithFetchCreditLimit() {
        Student student = new Student(connection, "2020csb1102", "student");
        String query = "update event set _event = 3 , _session = array[2021, 1]";
        student.dUtil.runQuery(query, false);

        query = "insert into y2_cse_offering values ('cp301', null, 'gunturi')";
        student.dUtil.runQuery(query, false);

        String input = "2\n1\ncp301\ngunturi\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        student.showMenu(sc);
        System.setIn(stdin);
    }


}
