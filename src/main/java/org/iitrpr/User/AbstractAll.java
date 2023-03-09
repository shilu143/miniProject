package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;
import org.iitrpr.utils.DatabaseQueryUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public abstract class AbstractAll {
    DataStorage dataStorage;
    Connection connection;
    String id;
    boolean isLoggedout;
    Integer[] _CURR_SESSION;
    int _EVENT;
    String role;
    DatabaseQueryUtils dUtil;

    AbstractAll(Connection connection, String id, String role) {
        this.connection = connection;
        this.id = id;
        this.role = role;
        isLoggedout = false;
        dataStorage = new DataStorage();
        dUtil = new DatabaseQueryUtils(connection);
        System.out.println(DataStorage.ANSI_CYAN + "\n\nAIMS PORTAL WELCOMES U ;)\n" + DataStorage.ANSI_RESET);
    }

    boolean showPersonalDetails(Integer usr, Scanner sc) {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> options = new ArrayList<>();
            options.add("name");
            options.add("id");
            options.add("role");
            if (usr == DataStorage._STUDENT)
                options.add("batch");
            options.add("department");
            options.add("email");
            options.add("contact no.");
            ArrayList<String> data = dUtil.fetchData(id, role);
            if (usr == DataStorage._STUDENT)
                data.add(3, id.substring(0, 4));//extra
            cli.createVerticalTable("Personal Details", options, data);

            options = new ArrayList<>();
            options.add("Edit");
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
                        editPersonalDetails(sc);
                        outer = true;
                    }
                    case "2" -> {
                        //returns to previous method
                    }
                    default -> inner = true;
                }
            } while (inner);
        } while (outer);
        return true;
    }

    boolean editPersonalDetails(Scanner sc) {
        System.out.print("Enter your new Contact number = ");
//        Scanner sc = new Scanner(System.in);
        String newContact = sc.nextLine();
        String query = String.format(
                "UPDATE %s " +
                        "SET contact = '%s' " +
                        "WHERE " +
                        "id = '%s'", role, newContact, id);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            stmt.close();
            return true;
        } catch (SQLException e) {
            return false;
//            throw new RuntimeException(e);
        }
    }

    public void logout() {
        isLoggedout = true;
    }

    abstract void showMenu(Scanner sc);

    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void graduationCheck(Scanner sc) {
        String sId = null;
//        Scanner sc = new Scanner(System.in);
        if(!role.equalsIgnoreCase("student")) {
            boolean isValid = false;
            while (!isValid) {
                System.out.print("Enter the Student ID = ");
                sId = sc.nextLine();
                String query = String.format("select * from student where lower(id) = lower('%s')", sId);
                if (dUtil.runQuery(query, true)) {
                    isValid = true;
                } else {
                    failurePrint("There is no student with that id");
                }
            }
        }
        else {
            sId = id;
        }
        ArrayList<Float> earnedCredits = new ArrayList<>();
        try {
            String query = dUtil.generateQuery(sId.substring(0, 4), "pc");
            earnedCredits.add(dUtil.getResultSet(query).getFloat("credits"));
            query = dUtil.generateQuery(sId.substring(0, 4), "ec");
            earnedCredits.add(dUtil.getResultSet(query).getFloat("credits"));
            query = dUtil.generateQuery(sId.substring(0, 4), "e");
            earnedCredits.add(dUtil.getResultSet(query).getFloat("credits"));
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
        ArrayList<String> temp = dUtil.fetchTable(query).get(0);
        ArrayList<Float> req = new ArrayList<>();
        for (String vl : temp) {
            req.add(Float.parseFloat(vl));
        }
        cli.createDiff("UG CRITERIA", title, options, req, earnedCredits);
        boolean flag = true;
        for (int i = 0; i < req.size(); i++) {
            if (earnedCredits.get(i) < req.get(i)) {
                flag = false;
                break;
            }
        }
        System.out.print("Graduated : ");
        if (flag) {
            successPrint("YES\n");
        } else {
            failurePrint("NO\n");
        }
        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Submenu", null, options);
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
        } while (runner);
    }

    protected void fetchEvent() {
        String query = "SELECT * FROM EVENT";
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                _EVENT = rs.getInt("_EVENT");
                Array rsString = rs.getArray("_SESSION");
                if (rsString != null) {
                    _CURR_SESSION = (Integer[]) rsString.getArray();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void currentEvent(Scanner sc) {
        boolean outer;
        do {
            outer = false;
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
            outer = eventMenu(sc);
        } while (outer);
    }

    abstract boolean eventMenu(Scanner sc);


    public float studentRecordCumCgpaCalc(boolean Calc, Scanner sc) {
        String sId = null;
        boolean outer;
        do {
            if (!role.equalsIgnoreCase("student")) {
//        Scanner sc = new Scanner(System.in);
                boolean isValid = false;
                while (!isValid) {
                    System.out.print("Enter the Student ID = ");
                    sId = sc.nextLine();
                    String query = String.format("select * from student where lower(id) = lower('%s')", sId);
                    if (dUtil.runQuery(query, true)) {
                        isValid = true;
                    } else {
                        failurePrint("There is no student with that id");
                    }
                }
            } else {
                sId = id;
            }

            ResultSet studentData = dUtil.getResultSet(String.format("select * from student  inner join department on department.deptid = student.deptid where lower(student.id) = lower('%s')", sId));
            StringBuilder TRANSCRIPT = new StringBuilder();

            try {
                TRANSCRIPT.append("\nName : ");
                TRANSCRIPT.append(studentData.getString("name").toUpperCase());
                TRANSCRIPT.append("\nID : ");
                TRANSCRIPT.append(studentData.getString("id").toUpperCase());
                TRANSCRIPT.append("\nDepartment : ");
                TRANSCRIPT.append(studentData.getString("deptname"));
                TRANSCRIPT.append("\nEmail : ");
                TRANSCRIPT.append(studentData.getString("email"));
                TRANSCRIPT.append("\nContact : ");
                TRANSCRIPT.append(studentData.getString("contact"));
                TRANSCRIPT.append("\n\n");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            float CGPA = 0;
            outer = false;
            if (!Calc)
                clearScreen();
            CLI cli = new CLI();
            String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", sId);
            float cumulativeEarnedCreated = 0;
            int totalSemester = 0;
            float cumulativeTotalGP = 0;
            try {
                //Event fetching
                fetchEvent();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
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

                    if (Arrays.equals(_CURR_SESSION, session) && _EVENT < DataStorage._GRADE_SUBMISSION_END) {
                        status = DataStorage._RUNNING;
                    }

                    query = String.format("SELECT * FROM _%s " +
                            "WHERE session = ARRAY[%d, %d]", sId, session[0], session[1]);
                    String header;
                    if (Arrays.equals(session, _CURR_SESSION)) {
                        header = String.format("(Current) Academic Session: %d-%d", session[0], session[1]);
                    } else {
                        header = String.format("Academic Session: %d-%d", session[0], session[1]);
                    }
                    stmt = connection.createStatement();
                    ResultSet rs2 = stmt.executeQuery(query);
                    float earnedCredits = 0;
                    float registeredCredits = 0;
                    float SGPA = 0;

                    float totalGP = 0;
                    while (rs2.next()) {
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
                        int credits = ltp[0] + ltp[2] / 2;
                        ltpc = String.format("(%d-%d-%d-%d)", ltp[0], ltp[1], ltp[2], credits);


                        registeredCredits += credits;
                        if (status == DataStorage._COMPLETED) {
                            int gp = 0;
                            if (grade != null) {
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
                        temp.add(grade != null && status == DataStorage._COMPLETED ? grade.toUpperCase() : "N/A");
                        data.add(temp);
                    }
                    if (status == DataStorage._COMPLETED) {
                        totalSemester++;
                        cumulativeTotalGP += totalGP;
                        SGPA = (totalGP / registeredCredits);
                    }
                    if (cumulativeEarnedCreated == 0) CGPA = 0;
                    else CGPA = (cumulativeTotalGP / cumulativeEarnedCreated);
                    if (Calc) {
                        return CGPA;
                    }

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
                    TRANSCRIPT.append(cli.recordPrint(header, options, data, footerOptions, footerData));
                }
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(Calc) {
                return CGPA;
            }
            outer = studentRecordMenu(sId, TRANSCRIPT, sc);
        } while (outer);
        return 0.0f;
    }

    public void showDeptOffering(String deptId, Integer year, Scanner sc) {
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
        ArrayList<ArrayList<String>> data = dUtil.fetchTable(query);
        ArrayList<String> options = new ArrayList<>();
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
        data = dUtil.fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);
        deptCourseOfferingMenu(year, deptId, sc);
    }

    public void successPrint(String out) {
        System.out.println(DataStorage.ANSI_GREEN + out + DataStorage.ANSI_RESET);
    }

    public void failurePrint(String out) {
        System.out.println(DataStorage.ANSI_RED + out + DataStorage.ANSI_RESET);
    }


    abstract boolean studentRecordMenu(String sId, StringBuilder TRANSCRIPT, Scanner sc);

    abstract void deptCourseOfferingMenu(int year, String deptId, Scanner sc);
}