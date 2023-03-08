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
public class StudentTest2 {
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
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        stdin = System.in;
    }

    @Test
    void TestShowMenu() throws SQLException {
        when(mockResultSet.getString("name")).thenReturn("testName");
        when(mockResultSet.getString("deptname")).thenReturn("testDept");
        when(mockResultSet.getString("email")).thenReturn("testEmail");
        when(mockResultSet.getString("contact")).thenReturn("testContact");
        mockStudent = new Student(mockConnection, "testid", "testrole");

        String input = "1\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        mockStudent.showMenu(sc);
        System.setIn(stdin);
    }

    @Test
    public void testChangePersonalDetails() throws SQLException {
        when(mockResultSet.getString("name")).thenReturn("testName");
        when(mockResultSet.getString("deptname")).thenReturn("testDept");
        when(mockResultSet.getString("email")).thenReturn("testEmail");
        when(mockResultSet.getString("contact")).thenReturn("testContact");
        mockStudent = new Student(mockConnection, "testid", "testrole");
        when(mockStatement.execute(Mockito.anyString())).thenReturn(true);
        String input = "1\n1\n+91 6541232014\n2\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Scanner sc = new Scanner(System.in);
        mockStudent.showMenu(sc);
        System.setIn(stdin);
    }


}
