package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Student extends AbstractAll {
    public Student(Connection connection, String id, String role) {
        super(connection, id, role);
    }

    @Override
    public void showMenu(Scanner sc) {
        do {
            clearScreen();

            ArrayList<String> options = new ArrayList<>();
            options.add("Personal Details");
            options.add("Course Offering");
            options.add("Student Record");
            options.add("Show Current Event");
            options.add("Check Graduation");
            options.add("Logout");

            CLI cli = new CLI();
            String body = String.format("Welcome to AIMS Portal (%s)", role.toUpperCase());
            cli.createVSubmenu("Menu",body, options);

//            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> showPersonalDetails(DataStorage._STUDENT, sc);
                    case "2" -> showCourseOffering(sc);
                    case "3" -> studentRecordCumCgpaCalc(false, sc);
                    case "4" -> currentEvent(sc);
                    case "5" -> graduationCheck(sc);
                    case "6" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    void showCourseOffering(Scanner sc) {
        fetchEvent();
        Integer year = _CURR_SESSION[0] - Integer.parseInt(id.substring(0,4)) + 1;
        String query = String.format("SELECT deptid from student where id = lower('%s')", id);
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
        String deptId  = data.get(0).get(0);
//        System.out.println(year);
        showDeptOffering(deptId, year, sc);
    }

    @Override
    boolean eventMenu(Scanner sc) {
        ArrayList<String> options = new ArrayList<>();
        options.add("Back");
        CLI cli = new CLI();
        cli.createVSubmenu("SubMenu", null, options);
//            Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "1" -> {
//                    back
                }
                default -> runner = true;
            }
        } while (runner);
        return false;
    }

    @Override
    boolean studentRecordMenu(String sId, StringBuilder TRANSCRIPT, Scanner sc) {
        boolean outer = false;
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Course Drop");
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);

//            Scanner sc = new Scanner(System.in);
        boolean inner;
        do {
            inner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "1" -> {
                    if(courseDrop(sc)) {
                        outer = true;
                    }
                    else {
                        inner = true;
                    }
                }
                case "2" -> {
                    //returns to previous method
                }
                default -> inner = true;
            }
        } while (inner);
        return outer;
    }

    @Override
    void deptCourseOfferingMenu(int year, String deptId, Scanner sc) {
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Enroll Course");
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
//            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            String inp = sc.nextLine();
            switch (inp) {
                case "1" -> {
                    enrollCourse(year, deptId, sc);
                    runner = true;
                }
                case "2" -> {
//                    return back
                }
                default -> {
                    runner = true;
                }
            }
        } while(runner);
    }

    private boolean courseDrop(Scanner sc) {
        fetchEvent();
        Integer[] session = new Integer[2];
        if(_EVENT == DataStorage._COURSE_REG_START) {
            System.out.print("Enter the CourseID : ");
//            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();

            String query = String.format("SELECT * FROM _%s WHERE courseid = LOWER('%s')", id, input);
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (!rs.isBeforeFirst() ) {
                    System.out.println("There is no such course that you have registered ");
                }
                while(rs.next()) {
                    String courseid = rs.getString("courseid");
                    Array rsString = rs.getArray("session");
                    if (rsString != null) {
                        session = (Integer[]) rsString.getArray();
                    }
                    fetchEvent();
                    if(Arrays.equals(session, _CURR_SESSION)) {
                        if(_EVENT == DataStorage._COURSE_REG_START) {
                            query = String.format("SELECT fid from _%s where lower(courseid) = lower('%s') and session = array[%d, %d]",
                                    id,
                                    courseid,
                                    _CURR_SESSION[0],
                                    _CURR_SESSION[1]);
                            String fid = dUtil.getResultSet(query).getString("fid");
                            query = String.format("DELETE FROM _%s WHERE courseid = LOWER('%s') and session = array[%d, %d]",
                                    id, courseid, _CURR_SESSION[0], _CURR_SESSION[1]);
                            stmt = connection.createStatement();
                            stmt.execute(query);
                            query = String.format("""
                                    DELETE FROM _%s
                                    WHERE LOWER(courseid) = LOWER('%s')
                                    and LOWER(sid) = LOWER('%s')
                                    and session = array[%d, %d]
                                    """, fid, courseid, id, _CURR_SESSION[0], _CURR_SESSION[1]);
                            dUtil.runQuery(query, false);
                            System.out.println("Course Dropped Successfully");
                            return true;
                        }
                        else {
                            System.out.println("Course register/drop event has ended");
                        }
                    }
                    else {
                        failurePrint("Sorry, you can't drop this course");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else if(_EVENT >= DataStorage._COURSE_FLOAT_END) {
            System.out.println("Sorry, Course Register/Drop Event has Ended");
        }
        else {
            System.out.println("Sorry, Course Register/Drop Event has not yet Started");
        }
        return false;
    }

    private void enrollCourse(Integer year, String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_REG_START) {
            failurePrint("Sorry currently course Registration is not allowed");
            return;
        }
//        Scanner sc = new Scanner(System.in);
        System.out.print("Enter courseId = ");
        String courseId = sc.nextLine();
        String query = String.format("SELECT * FROM y%d_%s_offering where courseid = lower('%s')", year, deptId, courseId);
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
        if(data.size() == 0) {
            failurePrint("There is no such course exist in the course offering");
            return;
        }
        query = String.format("""
            SELECT * FROM _%s
            WHERE lower(courseid) = lower('%s')
                and grade is not null and lower(grade) <> 'f'
                """, id, courseId);
//        System.out.println(query);
        if(dUtil.runQuery(query, true)) {
            failurePrint("You have already done this course previously");
            return;
        }
        query = String.format("""
            SELECT * FROM _%s
            WHERE lower(courseid) = lower('%s')
                and session = array[%d,%d]
                """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
        if(dUtil.runQuery(query, true)) {
            System.out.println(DataStorage.ANSI_BLUE + "You have already registered this course" + DataStorage.ANSI_RESET);
            return;
        }
//        System.out.println(data);
        Float credit = dUtil.getCourseCredit(courseId, year, deptId, id.substring(0,4));
//        System.out.println(credit);
        query = String.format("""
                SELECT COALESCE(SUM(ltp[1] + ltp[3] / 2.0), 0.0) as credit
                    FROM (
                        SELECT t1.courseid, t2.batch, t2.ltp, t1.session
                        FROM _%s t1
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
                WHERE table1.session = array[%d,%d]
                """, id, deptId, deptId, id.substring(0, 4), _CURR_SESSION[0], _CURR_SESSION[1]);
        Float curRegisteredCredits = Float.parseFloat(dUtil.fetchTable(query).get(0).get(0));
//        System.out.println(curRegisteredCredits);
        Float creditLimit = fetchCreditLimit(deptId, year);
        if(curRegisteredCredits + credit > creditLimit) {
            System.out.println("Credit Limit exceeded");
            return;
        }

        String fId = "";
        if(courseId.equalsIgnoreCase("cp301")) {
            System.out.print("Enter the Instructor ID = ");
            fId = sc.nextLine();
            query = String.format("SELECT * FROM y%d_%s_offering where lower(courseid) = lower('%s') and lower(fid) = lower('%s')",
                    year,
                    deptId,
                    courseId,
                    fId);
            if(!dUtil.runQuery(query, true)) {
                failurePrint("Invalid Instructor ID");
                return;
            }
        }


//        System.out.println("OK credit check ok");
        query  = String.format("""
                SELECT *
                from course_catalog_%s t1
                inner join (
                SELECT courseid, MAX(batch) as max_value
                FROM course_catalog_%s table2
                WHERE batch <= %s
                    and lower(courseid) = lower('%s')
                GROUP BY courseid) t2 on t1.courseid = t2.courseid and t1.batch = t2.max_value;
                """, deptId, deptId, id.substring(0, 4), courseId);

        float cgCriteria = 0.0f;
        try {
            Statement stmt = null;
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                Array temp = rs.getArray("prereq");
                String[] prereq = null;
                if(temp!=null)
                    prereq = (String[])temp.getArray();
                if(prereq != null) {
                    for (var crs : prereq) {
                        query = String.format("""
                                SELECT * FROM _%s
                                WHERE lower(courseid) = lower('%s')
                                    and grade is not null and lower(grade) <> 'f'
                                    """, id, crs);
                        if (!dUtil.runQuery(query, true)) {
                            failurePrint("You do not satisfied the prerequisite for this course");
                            return;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(!courseId.equalsIgnoreCase("cp301"))
            query = String.format("SELECT cgcriteria from y%d_%s_offering where lower(courseid) = lower('%s')", year, deptId, courseId);
        else
            query = String.format("SELECT cgcriteria FROM y%d_%s_offering where lower(courseid) = lower('%s') and lower(fid) = lower('%s')",
                    year,
                    deptId,
                    courseId,
                    fId);
        data = dUtil.fetchTable(query);
        if(data.get(0).get(0).isEmpty()) {
            cgCriteria = 0.0f;
        }
        else {
            cgCriteria = Float.parseFloat(data.get(0).get(0));
        }
        float _CGPA = studentRecordCumCgpaCalc(true, sc);
        if(_CGPA < cgCriteria) {
            failurePrint("CG Criteria is not satisfied");
            return;
        }
//        System.out.println("Done prerequisites");
        if(!courseId.equalsIgnoreCase("cp301"))
            query  = String.format("""
                    SELECT t1.courseid, t1.coursename, t1.ltp, t1.prereq, t3.fid
                    from course_catalog_%s t1
                    inner join (
                    SELECT courseid, MAX(batch) as max_value
                    FROM course_catalog_%s table2
                    WHERE batch <= %s
                        and lower(courseid) = lower('%s')
                    GROUP BY courseid) t2 on t1.courseid = t2.courseid and t1.batch = t2.max_value
                    inner join y%d_%s_offering t3 on t1.courseid = t3.courseid;
                    """, deptId, deptId, id.substring(0, 4), courseId, year, deptId);
        else
            query  = String.format("""
                    SELECT t1.courseid, t1.coursename, t1.ltp, t1.prereq, t3.fid
                    from course_catalog_%s t1
                    inner join (
                    SELECT courseid, MAX(batch) as max_value
                    FROM course_catalog_%s table2
                    WHERE batch <= %s
                        and lower(courseid) = lower('%s')
                    GROUP BY courseid) t2 on t1.courseid = t2.courseid and t1.batch = t2.max_value
                    inner join y%d_%s_offering t3 on t1.courseid = t3.courseid and t3.fid = lower('%s');
                    """, deptId, deptId, id.substring(0, 4), courseId, year, deptId, fId);
//        System.out.println(query);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                query = String.format("""
                insert into _%s
                (courseid, coursename, ltp, prereq, fid, session)
                values(?, ?, ?, ?, ?, ?)
                """, id);
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, rs.getString("courseid"));
                pstmt.setString(2, rs.getString("coursename"));
                pstmt.setArray(3, rs.getArray("ltp"));
                pstmt.setArray(4, rs.getArray("prereq"));
                pstmt.setString(5, rs.getString("fid"));
                Array temp = connection.createArrayOf("INT", _CURR_SESSION);
                pstmt.setArray(6, temp);
                pstmt.execute();
                query = String.format("insert into _%s values('%s', '%s', '%s', array[%d, %d])",
                        rs.getString("fid"),
                        rs.getString("courseid"),
                        rs.getString("coursename"),
                        id,
                        _CURR_SESSION[0],
                        _CURR_SESSION[1]
                );

//                System.out.println(query);
                dUtil.runQuery(query, false);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        successPrint("Course Enrolled Successfully");
    }

    private Float fetchCreditLimit(String deptId, Integer year) {
        float creditLimit = 0.0f;
        fetchEvent();
        String query = "select sem1, sem2, mult from ug_req";
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
        float mult = Float.parseFloat(data.get(0).get(2));
        if(year == 1) {
            if(_CURR_SESSION[1] == 1) {
                return Float.parseFloat(data.get(0).get(0));
            }
            if(_CURR_SESSION[1] == 2) {
                return Float.parseFloat(data.get(0).get(1));
            }
        }
        else {
            int temp = _CURR_SESSION[0] * 2 + _CURR_SESSION[1];
            Integer[] prevSem1 = new Integer[2];
            prevSem1[0] = (temp - 1) / 2;
            prevSem1[1] = ((temp - 1) % 2 == 0 ? 2 : 1);
            Integer[] prevSem2 = new Integer[2];
            prevSem2[0] = (temp - 2) / 2;
            prevSem2[1] = ((temp - 2) % 2 == 0 ? 2 : 1);
            float creditSum = 0.0f;
            query = String.format("""
                SELECT COALESCE(SUM(ltp[1] + ltp[3]),0.0)
                FROM _%s
                where session = array[%d, %d] or session = array[%d, %d]
                    and lower(grade) <> 'f'
                """, id, prevSem1[0], prevSem1[1], prevSem2[0], prevSem2[1]);
            data = dUtil.fetchTable(query);
            creditSum += Float.parseFloat(data.get(0).get(0));
            creditLimit =  (float) ((creditSum / 2.0)* mult);
        }
        return creditLimit;
    }

}
