package org.iitrpr.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;


public class Student extends abstractUser {
    Float _CGPA = 0.0f;
//    Scanner sc;
    public Student(Connection connection, String id, String role) {
        super(connection, id, role);
//        sc = new Scanner(System.in);
    }

    @Override
    protected void floatCourse(String deptid, Scanner sc) {
//return null;
    }

    @Override
    protected void editCourseCatalog(String deptid, Scanner sc) {
//        return null
    }

    @Override
    protected void addNewCourseinCatalog(String deptid, Scanner sc) {
//        return null
    }

    @Override
    public void showMenu(Scanner sc) {
        do {
//            clearScreen();

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
                    case "3" -> studentRecord(false, sc);
                    case "4" -> showCurrentEvent(sc);
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
        ArrayList<ArrayList<String>> data = fetchTable(query);
        String deptId  = data.get(0).get(0);
//        System.out.println(year);
        showDeptOffering(deptId, year, sc);
    }

    private float getCourseCredit(String courseId, Integer year, String deptId, String batch) {
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

    private void showDeptOffering(String deptId, Integer year, Scanner sc) {
        clearScreen();
        fetchEvent();
        String tabName = String.format("y%d_%s_offering", year, deptId);
        String query = String.format("""
            SELECT table1.courseid, table1.coursename, table1.ltp, table1.prereq, table1.type, table1.cgcriteria, t3.name as Instructor 
                FROM (
                    SELECT t1.courseid, t2.coursename, t2.batch, t2.ltp, t2.type, t1.fid, t1.cgcriteria, t2.prereq
                    FROM %s t1
                    INNER JOIN course_catalog_%s t2
                    ON t2.courseid = t1.courseid
                ) table1
            INNER JOIN (
                SELECT courseid, MAX(batch) as max_value
                FROM course_catalog_%s table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) <> lower('e');
        """, tabName, deptId, deptId, _CURR_SESSION[0] - year + 1);
//        System.out.println(query);
        ArrayList<ArrayList<String>> data = fetchTable(query);
        ArrayList<String> options  = new ArrayList<>();
        options.add("Course ID");
        options.add("Course Name");
        options.add("LTP");
        options.add("Prerequisites");
        options.add("Type");
        options.add("CG Criteria");
        options.add("Instructor");
        CLI cli = new CLI();
        cli.recordPrint("Core Courses", options, data, null, null);
        query = String.format("""
            SELECT table1.courseid, table1.coursename, table1.ltp, table1.prereq, table1.type, table1.cgcriteria, t3.name as Instructor 
                FROM (
                    SELECT t1.courseid, t2.coursename, t2.batch, t2.ltp, t2.type, t1.fid, t1.cgcriteria, t2.prereq
                    FROM %s t1
                    INNER JOIN course_catalog_%s t2
                    ON t2.courseid = t1.courseid
                ) table1
            INNER JOIN (
                SELECT courseid, MAX(batch) as max_value
                FROM course_catalog_%s table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) = lower('e');
        """, tabName, deptId, deptId, _CURR_SESSION[0] - year + 1);
        data = fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);
        options = new ArrayList<>();
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

    private void enrollCourse(Integer year, String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT != DataStorage._COURSE_REG_START) {
            System.out.println(DataStorage.ANSI_RED + "Sorry currently course Registration is not allowed" + DataStorage.ANSI_RESET);
            return;
        }
//        Scanner sc = new Scanner(System.in);
        System.out.print("Enter courseId = ");
        String courseId = sc.nextLine();
        String query = String.format("SELECT * FROM y%d_%s_offering where courseid = lower('%s')", year, deptId, courseId);
        ArrayList<ArrayList<String>> data = fetchTable(query);
        if(data.size() == 0) {
            System.out.println(DataStorage.ANSI_RED + "There is no such course exist in the course offering" + DataStorage.ANSI_RESET);
            return;
        }
        query = String.format("""
            SELECT * FROM _%s
            WHERE lower(courseid) = lower('%s')
                and grade is not null and lower(grade) <> 'f'
                """, id, courseId);
//        System.out.println(query);
        if(runQuery(query, true)) {
            System.out.println(DataStorage.ANSI_RED + "You have already done this course previously" + DataStorage.ANSI_RESET);
            return;
        }
        query = String.format("""
            SELECT * FROM _%s
            WHERE lower(courseid) = lower('%s')
                and session = array[%d,%d]
                """, id, courseId, _CURR_SESSION[0], _CURR_SESSION[1]);
        if(runQuery(query, true)) {
            System.out.println(DataStorage.ANSI_BLUE + "You have already registered this course" + DataStorage.ANSI_RESET);
            return;
        }
//        System.out.println(data);
        Float credit = getCourseCredit(courseId, year, deptId, id.substring(0,4));
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
        Float curRegisteredCredits = Float.parseFloat(fetchTable(query).get(0).get(0));
//        System.out.println(curRegisteredCredits);
        Float creditLimit = fetchCreditLimit(deptId, year);
        if(curRegisteredCredits + credit > creditLimit) {
            System.out.println("Credit Limit exceeded");
            return;
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
                        if (!runQuery(query, true)) {
                            System.out.println(DataStorage.ANSI_RED + "You do not satisfied the prerequisite for this course" + DataStorage.ANSI_RESET);
                            return;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        query = String.format("SELECT cgcriteria from y%d_%s_offering where lower(courseid) = lower('%s')", year, deptId, courseId);
        data = fetchTable(query);
        if(data.get(0).get(0).isEmpty()) {
            cgCriteria = 0.0f;
        }
        else {
            cgCriteria = Float.parseFloat(data.get(0).get(0));
        }
        studentRecord(true, sc);
        if(_CGPA < cgCriteria) {
            System.out.println(DataStorage.ANSI_RED + "CG Criteria is not satisfied" + DataStorage.ANSI_RESET);
            return;
        }
//        System.out.println("Done prerequisites");
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
                runQuery(query, false);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(DataStorage.ANSI_GREEN + "Course Enrolled Successfully" + DataStorage.ANSI_RESET);
    }

    private Float fetchCreditLimit(String deptId, Integer year) {
        float creditLimit = 0.0f;
        fetchEvent();
        String query = "select sem1, sem2, mult from ug_req";
        ArrayList<ArrayList<String>> data= fetchTable(query);
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
            data = fetchTable(query);
            creditSum += Float.parseFloat(data.get(0).get(0));
            creditLimit =  (float) ((creditSum / 2.0)* mult);
        }
        return creditLimit;
    }
    private String generateQuery(String batch, String type) {
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

    private void graduationCheck(Scanner sc) {
        ArrayList<Float> earnedCredits = new ArrayList<>();
        try {
            String query = generateQuery(id.substring(0, 4),"pc");
            earnedCredits.add(getResultSet(query).getFloat("credits"));
            query = generateQuery(id.substring(0, 4),"ec");
            earnedCredits.add(getResultSet(query).getFloat("credits"));
            query = generateQuery(id.substring(0, 4),"e");
            earnedCredits.add(getResultSet(query).getFloat("credits"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        clearScreen();

        CLI cli = new CLI();
//        cli.
        ArrayList<String> title = new ArrayList<>();
        title.add("Course Type");
        title.add("Required Credits");
        title.add("Earned Credits");
        ArrayList<String> options = new ArrayList<>();
        options.add("Program Core");
        options.add("Engineering Core");
        options.add("Elective");
        String query = "SELECT * FROM UG_REQ";
        ArrayList<String> temp = fetchTable(query).get(0);
        ArrayList<Float> req = new ArrayList<>();
        for(String vl : temp) {
            req.add(Float.parseFloat(vl));
        }
        cli.createDiff("UG CRITERIA", title, options, req, earnedCredits);
        boolean flag = true;
        for(int i = 0; i < req.size(); i++) {
            if(earnedCredits.get(i) < req.get(i)) {
                flag = false;
                break;
            }
        }
        if(flag) {
            System.out.println("Graduated : " + DataStorage.ANSI_GREEN + "YES" + DataStorage.ANSI_RESET + "\n");
        }
        else {
            System.out.println("Graduated : " + DataStorage.ANSI_RED + "NO" + DataStorage.ANSI_RESET + "\n");
        }
        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Submenu", null, options);
//        Scanner sc = new Scanner(System.in);
        System.out.print("> ");
        String inp = sc.nextLine();
        boolean runner;
        do {
            runner = false;
            switch (inp) {
                case "1" -> {
                    //Back
                }
                default -> {
                    runner = true;
                }
            }
        } while(runner);
    }

    void showCurrentEvent(Scanner sc) {
        clearScreen();
        fetchEvent();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<String> data = new ArrayList<>();
        options.add("Academic Session");
        options.add("Current Event");
        data.add(String.format("%d - %d", _CURR_SESSION[0], _CURR_SESSION[1]));
        data.add(String.format("%s", dataStorage.EventHash.get(_EVENT)));
        cli.createVerticalTable("EVENT", options, data);

        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine();
            if (inp.equals("1")) {
//                    return back
            } else {
                runner = true;
            }
        } while (runner);
    }

    private void studentRecord(boolean flag, Scanner sc) {
        boolean outer;
        do {
            outer = false;
            if(!flag)
                clearScreen();
            CLI cli = new CLI();
            String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", id);
            float cumulativeEarnedCreated = 0;
            int totalSemester = 0;
            float cumulativeTotalGP = 0;
            try {
                //Event fetching
                fetchEvent();

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
                    int status = DataStorage._COMPLETED;
                    Array rsString = rs.getArray("session");
                    ArrayList<String> options = new ArrayList<>();
                    options.add("Course ID");
                    options.add("Course Name");
                    options.add("(L-T-P-C)");
                    options.add("Status");
                    options.add("Grade");

                    ArrayList<ArrayList<String>> data = new ArrayList<>();
                    Integer[] session = null;
                    if (rsString != null) {
                        session = (Integer[]) rsString.getArray();
                    }
                    assert session != null;

                    if(Arrays.equals(_CURR_SESSION, session) && _EVENT < DataStorage._GRADE_SUBMISSION_END) {
                        status = DataStorage._RUNNING;
                    }

                    query = String.format("SELECT * FROM _%s " +
                            "WHERE session = ARRAY[%d, %d]", id, session[0], session[1]);
                    String header;
                    if(Arrays.equals(session, _CURR_SESSION)) {
                        header = String.format("(Current) Academic Session: %d-%d", session[0], session[1]);
                    }
                    else {
                        header = String.format("Academic Session: %d-%d", session[0], session[1]);
                    }
                    stmt = connection.createStatement();
                    ResultSet rs2 = stmt.executeQuery(query);
                    float earnedCredits = 0;
                    float registeredCredits = 0;
                    float SGPA = 0;
                    float CGPA = 0;
                    float totalGP = 0;
                    while(rs2.next()) {
                        String courseId = rs2.getString("courseid")
                                .trim().
                                toUpperCase();
                        String courseName = rs2.getString("coursename").trim();
                        Integer[] ltp = null;
                        String ltpc = null;
                        String grade = rs2.getString("grade");


                        ArrayList<String> temp = new ArrayList<>();
                        rsString = rs2.getArray("ltp");
                        if (rsString != null) {
                            ltp = (Integer[]) rsString.getArray();
                        }
                        assert ltp != null;
                        int credits = ltp[0] + ltp[2]/2;
                        ltpc = String.format("(%d-%d-%d-%d)",ltp[0], ltp[1], ltp[2], credits);



                        registeredCredits += credits;
                        if(status == DataStorage._COMPLETED) {
                            int gp = 0;
                            if(grade != null) {
                                gp = dataStorage.GradePointMap.get(grade);
                            }
                            if (gp != 0) {
                                earnedCredits += credits;
                                cumulativeEarnedCreated += credits;
                            }
                            totalGP += (credits * gp);
                        }

                        temp.add(courseId);
                        temp.add(courseName);
                        temp.add(ltpc);
                        temp.add(status == DataStorage._COMPLETED ? "Completed" : "Running");
                        temp.add(grade != null && status == DataStorage._COMPLETED ? grade : "N/A");
                        data.add(temp);
                    }
                    if(status == DataStorage._COMPLETED) {
                        totalSemester++;
                        cumulativeTotalGP += totalGP;
                        SGPA = (totalGP / registeredCredits);
                    }
                    if(cumulativeEarnedCreated == 0) CGPA = 0;
                    else    CGPA = (cumulativeTotalGP / cumulativeEarnedCreated);
                    _CGPA = CGPA;


                    ArrayList<String> footerOptions = new ArrayList<>();
                    ArrayList<String> footerData = new ArrayList<>();
                    footerOptions.add("Registered Credits");
                    footerData.add(String.valueOf(registeredCredits));
                    footerOptions.add("Earned Credits");
                    footerData.add(String.valueOf((status == DataStorage._COMPLETED) ? earnedCredits : "N/A"));
                    footerOptions.add("SGPA");
                    footerData.add(String.valueOf((status == DataStorage._COMPLETED) ? SGPA : "N/A"));
                    footerOptions.add("CGPA");
                    footerData.add(String.valueOf(CGPA));
                    if(!flag)
                        cli.recordPrint(header, options, data, footerOptions, footerData);
                }
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            ArrayList<String> options = new ArrayList<>();
            options.add("Course Drop");
            options.add("Back");
            if(!flag)
                cli.createVSubmenu("SubMenu", null, options);

//            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                if(flag) break;
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
            if(flag) break;
        } while(outer);
    }

    private boolean courseDrop(Scanner sc) {
        fetchEvent();
        Integer[] session = new Integer[2];
        if(_EVENT == DataStorage._COURSE_REG_START) {
            System.out.print("Enter the CourseID : ");
//            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine().trim();

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
                            String fid = getResultSet(query).getString("fid");
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
                            runQuery(query, false);
                            System.out.println("Course Dropped Successfully");
                            return true;
                        }
                        else {
                            System.out.println("Course register/drop event has ended");
                        }
                    }
                    else {
                        System.out.println("Sorry, you can't drop this course");
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
}
