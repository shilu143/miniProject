package org.iitrpr;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.StringUtils;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

public class Seeder {

    public boolean generateSchema(Connection connection) {
        try {
            String query = "";
            try {
                String file = "./src/SQL/sys.sql";
                query = new String(Files.readAllBytes(Paths.get(file)));
            } catch (IOException e) {
                System.out.print(e.getLocalizedMessage());
            }
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    public boolean fill(Connection connection) {
            try {
                File file1 = new File("./assets/data/seedHelper.csv");
                File file2 = new File("./assets/data/department.csv");
                Scanner scan1 = new Scanner(file1);
                Scanner scan2 = new Scanner(file2);
                scan1.nextLine();
                scan2.nextLine();

                while (scan1.hasNextLine()) {
                    String str = scan1.nextLine();
                    String[] input = str.split(",");
                    String name = input[0].toLowerCase();
                    String id = input[1].toLowerCase();
                    String role = input[2].toLowerCase();
                    String deptId = input[3].toLowerCase();
                    String email = input[4];
                    String contact = input[5];
                    String password = input[6];


                    try {
                        PreparedStatement st = connection.prepareStatement("INSERT INTO _USER VALUES (?, ?, ?)");
                        st.setString(1, id);
                        st.setString(2, role);
                        st.setString(3, password);
                        st.executeUpdate();

                        String query = String.format("INSERT INTO %s VALUES (?, ?, ?, ?, ?)", role.toUpperCase());

                        st = connection.prepareStatement(query);
                        st.setString(1, name);
                        st.setString(2, id);
                        st.setString(3, deptId);
                        st.setString(4, email);
                        st.setString(5, contact);
                        st.executeUpdate();

                        if(role.equalsIgnoreCase("student")) {
                            query = String.format("CREATE TABLE %s (" +
                                    "courseId varchar," +
//                                    todo
                                    "coursename varchar," +
                                    "ltp integer[3]," +
                                    "prereq varchar[]," +
//                                    todo
                                    "grade varchar," +
                                    "fid varchar," +
                                    "session integer[2]" +
                                    ")", "_" + id);
                            Statement pstmt = connection.createStatement();
                            pstmt.execute(query);
                            pstmt.close();
                        }
                        else if(role.equalsIgnoreCase("faculty")) {
                            query = String.format("CREATE TABLE %s (" +
                                    "courseId varchar," +
                                    "coursename varchar," +
                                    "sid varchar," +
                                    "session integer[2]" +
                                    ")", "_" + id);
                            Statement pstmt = connection.createStatement();
                            pstmt.execute(query);
                            pstmt.close();
                        }
                        st.close();
                    } catch (SQLException exception) {
                        System.out.println(exception.getMessage());
                    }
                }
                scan1.close();

                while(scan2.hasNextLine()) {
                    String str = scan2.nextLine();
                    String[] input = str.split(",");
                    String deptId = input[0];
                    String deptName = input[1];
                    try {
                        PreparedStatement st = connection.prepareStatement("INSERT INTO DEPARTMENT VALUES (?, ?)");
                        st.setString(1, deptId);
                        st.setString(2, deptName);
                        st.executeUpdate();
                        st.close();
                        if(!deptId.equalsIgnoreCase("acad")) {
//                            Statement stmt = connection.createStatement();
//                            String query = String.format("CREATE TABLE _%s_ ( " +
//                                    "courseid varchar, " +
//                                    "coursename varchar, " +
//                                    "prereq varchar[], " +
//                                    "cgcriteria numeric(4, 2), " +
//                                    "type varchar, " +
//                                    "fid varchar, " +
//                                    "primary key(courseid) " +
//                                    ");", deptId);
//                            stmt.execute(query);

                            Statement stmt = connection.createStatement();
                            String query = String.format("CREATE TABLE COURSE_CATALOG_%s ( " +
                                    "courseid varchar, " +
                                    "coursename varchar, " +
                                    "ltp integer[3], " +
                                    "prereq varchar[], " +
                                    "type varchar, " +
                                    "batch integer, " +
                                    "primary key(courseid, batch) " +
                                    ");", deptId);
                            stmt.execute(query);
                            String csvFilePath = String.format("./assets/data/catalog_%s.csv", deptId);
                            try {
                                CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
                                String[] record;
                                csvReader.readNext();
                                while ((record = csvReader.readNext()) != null) {
                                    String courseId = record[0].trim().toLowerCase();
                                    String courseName = record[1].trim();
                                    String[] str_ltp = record[2].trim().split(",");
                                    Integer[] ltp = new Integer[3];
                                    for(int i = 0; i < str_ltp.length; i++) {
                                        ltp[i] = Integer.parseInt(str_ltp[i]);
                                    }
                                    String temp = record[3];
                                    String[] prereq;
                                    if(temp.length() == 0) {
                                        prereq = null;
                                    }
                                    else {
                                        prereq = temp.split(",");
                                        prereq = StringUtils.stripAll(prereq);
                                    }
                                    String type = record[4].trim().toLowerCase();
                                    int batch = Integer.parseInt(record[5]);
                                    query = String.format("insert into course_catalog_%s values(?,?,?,?,?,?)", deptId);
                                    PreparedStatement pstmtt = connection.prepareStatement(query);
                                    pstmtt.setString(1, courseId);
                                    pstmtt.setString(2, courseName);
                                    Array array = connection.createArrayOf("INTEGER", ltp);
                                    pstmtt.setArray(3, array);
                                    array = connection.createArrayOf("VARCHAR", prereq);
                                    pstmtt.setArray(4, prereq == null ? null : array);
                                    pstmtt.setString(5, type);
                                    pstmtt.setInt(6, batch);
                                    pstmtt.execute();
                                }
                            } catch (CsvValidationException | IOException e) {
                                throw new RuntimeException(e);
                            }
                            for(int y = 1; y <= 4; y++) {
                                query = String.format("""
                                        create table y%d_%s_offering (
                                            courseId  varchar,
                                            cgcriteria numeric(4,2),
                                            fid varchar
                                        )""", y, deptId);
                                stmt = connection.createStatement();
                                stmt.execute(query);
                            }
                        }
                    } catch (SQLException exception) {
                        System.out.println(exception.getMessage());
                        return false;
                    }
                }
            } catch (IOException e) {
                System.out.print(e.getLocalizedMessage());
                return false;
            }
            return true;
    }
    public static void main(String[] args) {
        Seeder seed = new Seeder();
        Dotenv dotenv = Dotenv.configure().load();

        String USER = dotenv.get("DB_USER");
        String PASS = dotenv.get("DB_PASS");

        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/aimsdb";
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
