package org.iitrpr.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentTest3 {
    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;


    @Mock
    private Student mockStudent;

    private InputStream stdin;

    @BeforeEach
    void setUp() throws SQLException {
//        when(mockConnection.createStatement()).thenReturn(mockStatement);
//        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
//        when(mockResultSet.next()).thenReturn(true);

        stdin = System.in;
    }

    @Test
    public void testCourseOffering() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
//        the first 4 digit should be the entry year of the student
        Student myClassSpy = spy(new Student(mockConnection, "2020csb1126", "testpass"));
        doAnswer(invocation -> {
            myClassSpy._CURR_SESSION = new Integer[2];
            myClassSpy._CURR_SESSION[0] = 2020;
            myClassSpy._CURR_SESSION[1] = 1;
            return null;
        }).when(myClassSpy).fetchEvent();
        ArrayList<ArrayList<String>> deptidForStudent = new ArrayList<>();
        ArrayList<String> dept = new ArrayList<>();
        dept.add("CSE");
        deptidForStudent.add(dept);
        when(myClassSpy.fetchTable(Mockito.anyString())).thenAnswer(new Answer<ArrayList<ArrayList<String>>>() {
            private int count = 0;
            public ArrayList<ArrayList<String>> answer(InvocationOnMock invocation) {
                count++;
                if(count == 1) {
                    return deptidForStudent;
                }else {
                    return new ArrayList<>();
                }
            }
        });
        String input = "2\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        myClassSpy.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void testCourseOfferingandEnrollment() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
//        the first 4 digit should be the entry year of the student
        Student myClassSpy = spy(new Student(mockConnection, "2020csb1126", "testpass"));
        doAnswer(invocation -> {
            myClassSpy._CURR_SESSION = new Integer[2];
            myClassSpy._CURR_SESSION[0] = 2020;
            myClassSpy._CURR_SESSION[1] = 1;
            myClassSpy._EVENT = 1;
            return null;
        }).when(myClassSpy).fetchEvent();
        ArrayList<ArrayList<String>> deptidForStudent = new ArrayList<>();
        ArrayList<String> dept = new ArrayList<>();
        dept.add("CSE");
        deptidForStudent.add(dept);
        when(myClassSpy.fetchTable(Mockito.anyString())).thenAnswer(new Answer<ArrayList<ArrayList<String>>>() {
            private int count = 0;
            public ArrayList<ArrayList<String>> answer(InvocationOnMock invocation) {
                count++;
                if(count == 1) {
                    return deptidForStudent;
                }else {
                    return new ArrayList<>();
                }
            }
        });
        String input = "2\n1\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        myClassSpy.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void testCourseOfferingwithWrongCourseId() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
        Student myClassSpy = spy(new Student(mockConnection, "2020csb1126", "testpass"));
        ArrayList<ArrayList<String>> deptidForStudent = new ArrayList<>();
        ArrayList<String> dept = new ArrayList<>();
        dept.add("CSE");
        deptidForStudent.add(dept);

        doAnswer(invocation -> {
            myClassSpy._CURR_SESSION = new Integer[2];
            myClassSpy._CURR_SESSION[0] = 2020;
            myClassSpy._CURR_SESSION[1] = 1;
            myClassSpy._EVENT = 3;
            return null;
        }).when(myClassSpy).fetchEvent();

        when(myClassSpy.fetchTable(Mockito.anyString())).thenAnswer(new Answer<ArrayList<ArrayList<String>>>() {
            private int count = 0;
            public ArrayList<ArrayList<String>> answer(InvocationOnMock invocation) {
                count++;
                if(count == 1) {
                    return deptidForStudent;
                }else {
                    return new ArrayList<>();
                }
            }
        });

        String input = "2\n1\nwrongcourseid\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        myClassSpy.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    void TestshowCurrentEvent() throws SQLException {
//        when(mockConnection.createStatement()).thenReturn(mockStatement);
//        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
        Student myClassSpy = spy(new Student(mockConnection, "2020csb1126", "testpass"));
        doAnswer(invocation -> {
            myClassSpy._CURR_SESSION = new Integer[2];
            myClassSpy._CURR_SESSION[0] = 2020;
            myClassSpy._CURR_SESSION[1] = 1;
            return null;
        }).when(myClassSpy).fetchEvent();
        String input = "4\n1\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        myClassSpy.showMenu(sc);
        System.setIn(stdin);
    }

//    @Test
//    void TeststudentRecord() {
//        Student myClassSpy = spy(new Student(mockConnection, "2020csb1126", "testpass"));
//        doAnswer(invocation -> {
//            myClassSpy._CURR_SESSION = new Integer[2];
//            myClassSpy._CURR_SESSION[0] = 2020;
//            myClassSpy._CURR_SESSION[1] = 1;
//            return null;
//        }).when(myClassSpy).fetchEvent();
//        String input = "3\n1\n6\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
//        Scanner sc = new Scanner(System.in);
//        myClassSpy.showMenu(sc);
//        System.setIn(stdin);
//    }
}
