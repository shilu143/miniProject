package org.iitrpr.utils;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseQueryUtils {
    Connection connection;
    public DatabaseQueryUtils(Connection connection) {
        this.connection = connection;
    }
    public boolean runQuery(String query, boolean response) {
        try {
            Statement stmt = connection.createStatement();
            if(response) {
                ResultSet rs = stmt.executeQuery(query);
                return rs.isBeforeFirst();
            }
            else {
                stmt.execute(query);
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ArrayList<String>> getAllDept() {
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        try {
            String query = "SELECT * FROM DEPARTMENT";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                ArrayList<String> temp = new ArrayList<>();
                String deptid = rs.getString("deptid");
                if(!deptid.equalsIgnoreCase("acad")) {
                    temp.add(rs.getString("deptid"));
                    temp.add(rs.getString("deptname"));
                    data.add(temp);
                }
            }
            return data;
        } catch (SQLException e) {
            return null;
        }
    }

    public ArrayList<String> fetchData(String id, String role) {
        PreparedStatement st = null;
        try {
            String query = String.format("SELECT * FROM %s as s " +
                    "INNER JOIN DEPARTMENT as d " +
                    "ON s.deptid = d.deptid " +
                    "WHERE LOWER(id) = LOWER('%s')", role, id);

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String name = null;
            String dept = null;
            String email = null;
            String contact = null;
            if(rs.next()) {
                name = rs.getString("name").trim();
                dept = rs.getString("deptname").trim();
                email = rs.getString("email").trim();
                contact = rs.getString("contact").trim();
            }

            assert name != null;
            ArrayList<String> result = new ArrayList<>();
            result.add(name.toUpperCase());
            result.add(id.toUpperCase());
            result.add(role.toUpperCase());
            result.add(dept);
            result.add(email);
            result.add(contact);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ArrayList<String>> fetchTable(String query) {
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                ArrayList<String> temp = new ArrayList<>();
                ResultSetMetaData rsmd = rs.getMetaData();
                int n = rsmd.getColumnCount();
                for(int i = 1;i <= n; i++) {
                    String tp = rs.getString(i);
                    temp.add(tp == null ? "":tp);
                }
                data.add(temp);
            }
            return data;
        } catch (SQLException e) {
            return null;
//            throw new RuntimeException(e);
        }
    }

    public ResultSet getResultSet(String query) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs;
        } catch (SQLException e) {
            return null;
//            throw new RuntimeException(e);
        }
    }

    public boolean findCourse(String courseId, String deptId) {
        String query = String.format("Select * from course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs.isBeforeFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateQuery(String batch, String type) {
        return String.format("""
                SELECT SUM(LTP[1] + LTP[2] / 2) AS credits
                FROM (
                    SELECT t1.courseid, t2.batch, t2.ltp, t2.type, t1.grade
                    FROM _2020csb1102 t1
                    INNER JOIN course_catalog_cse t2
                        ON t2.courseid = t1.courseid
                ) table1
                INNER JOIN (
                  SELECT courseid, MAX(batch) as max_value
                  FROM course_catalog_cse table2
                  WHERE batch <= %s
                  GROUP BY courseid
                ) subquery
                ON table1.courseid = subquery.courseid
                   AND table1.batch = subquery.max_value
                WHERE table1.type = lower('%s') AND table1.grade IS NOT NULL
                    AND LOWER(table1.grade) <> 'f'""", batch, type);
    }

    public float getCourseCredit(String courseId, Integer year, String deptId, String batch) {
        String query = String.format("""
                SELECT table1.courseid, table1.coursename, table1.ltp, table1.prereq, table1.type, table1.cgcriteria, t3.name as Instructor
                    FROM (
                        SELECT t1.courseid, t2.coursename, t2.batch, t2.ltp, t2.type, t1.fid, t1.cgcriteria, t2.prereq
                        FROM y%d_%s_offering t1
                        INNER JOIN course_catalog_%s t2
                        ON t2.courseid = t1.courseid
                    ) table1
                INNER JOIN (
                    SELECT courseid, MAX(batch) as max_value
                    FROM course_catalog_%s table2
                    WHERE batch <= %s
                    GROUP BY courseid
                ) subquery
                ON table1.courseid = subquery.courseid
                AND table1.batch = subquery.max_value
                INNER JOIN faculty t3 on table1.fid = t3.id
                WHERE table1.courseid = lower('%s');
                """, year, deptId, deptId, deptId, batch, courseId);
//        System.out.println(query);
        float credit = 0.0f;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                Array temp = rs.getArray("ltp");
                Integer[] ltp = (Integer[])temp.getArray();
                credit = ltp[0] + ltp[2] / 2.0f;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return credit;
    }
}
