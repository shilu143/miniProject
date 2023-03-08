package org.iitrpr;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

public class AppTest {

    @Test
    public void testAuthenticateWithValidCredentials() throws SQLException {
        // Mock the connection and result set
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString("id")).thenReturn("testuser");
        Mockito.when(resultSet.getString("role")).thenReturn("student");
        Mockito.when(resultSet.getString("pass")).thenReturn("testpass");

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("6\n".getBytes());
        System.setIn(in);
        Scanner sc = new Scanner(System.in);
        App app = new App();
        boolean result = app.authenticate(connection, "testuser", "testpass", sc);
        assertTrue(result);

        Mockito.when(resultSet.getString("role")).thenReturn("faculty");
        sysInBackup = System.in;
        in = new ByteArrayInputStream("7\n".getBytes());
        System.setIn(in);
        sc = new Scanner(System.in);
        app = new App();
        result = app.authenticate(connection, "testuser", "testpass", sc);
        assertTrue(result);

        Mockito.when(resultSet.getString("role")).thenReturn("office");
        sysInBackup = System.in;
        in = new ByteArrayInputStream("7\n".getBytes());
        System.setIn(in);
        sc = new Scanner(System.in);
        app = new App();
        result = app.authenticate(connection, "testuser", "testpass", sc);
        assertTrue(result);
    }

    @Test
    public void testAuthenticateWithInValidCredentials() throws SQLException {
        // Mock the connection and result set
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString("id")).thenReturn("testuser");
        Mockito.when(resultSet.getString("role")).thenReturn("student");
        Mockito.when(resultSet.getString("pass")).thenReturn("testpass");

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("6\n".getBytes());
        System.setIn(in);
        Scanner sc = new Scanner(System.in);
        App app = new App();
        boolean result = app.authenticate(connection, "testuser", "wrongPassword", sc);
        assertFalse(result);

        Mockito.when(resultSet.next()).thenReturn(false);
        sysInBackup = System.in;
        in = new ByteArrayInputStream("7\n".getBytes());
        System.setIn(in);
        sc = new Scanner(System.in);
        app = new App();
        result = app.authenticate(connection, "wrongemail", "testpass", sc);
        assertFalse(result);

        Mockito.when(resultSet.getString("role")).thenReturn("office");
        sysInBackup = System.in;
        in = new ByteArrayInputStream("7\n".getBytes());
        System.setIn(in);
        sc = new Scanner(System.in);
        app = new App();
        result = app.authenticate(connection, "testuser", "wrongpass", sc);
        assertFalse(result);
    }

    @Test
    public void testException() throws SQLException {
        // Mock the connection and result set
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        doThrow(new SQLException("Test SQL Exception")).when(resultSet).next();
        Scanner sc = new Scanner(System.in);
        App app = new App();
        boolean result = app.authenticate(connection, "testuser", "wrongpass", sc);
        assertFalse(result);
    }

    @Test
    public void testInvalidRole() throws SQLException {
        // Mock the connection and result set
        Connection connection = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        Mockito.when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString("id")).thenReturn("testuser");
        Mockito.when(resultSet.getString("role")).thenReturn("invalidRole");
        Mockito.when(resultSet.getString("pass")).thenReturn("testpass");
        Scanner sc = new Scanner(System.in);
        App app = new App();
        boolean result = app.authenticate(connection, "testuser", "testpass", sc);
        assertFalse(result);
    }
}
