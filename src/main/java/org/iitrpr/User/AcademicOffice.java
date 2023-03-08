package org.iitrpr.User;

import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;
import org.iitrpr.utils.fileWriterUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AcademicOffice extends abstractUser{
    public AcademicOffice(Connection connection, String id, String role) {
        super(connection, id, role);
//        sc = new Scanner(System.in);
    }

    @Override
    protected void floatCourse(String deptid, Scanner sc) {
//return null;
    }

    @Override
    public void showMenu(Scanner sc) {
        do {
            clearScreen();

            ArrayList<String> options = new ArrayList<>();
            options.add("Personal Details");
            options.add("Course Catalog");
            options.add("Course Offering");
            options.add("View Student Record");
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
                    case "1" -> showPersonalDetails(DataStorage._OFFICE, sc);
                    case "2" -> viewCourseCatalog(sc);
                    case "3" -> showCourseOffering(sc);
                    case "4" -> viewStudentRecord(sc);
                    case "5" -> currentEvent(sc);
                    case "6" -> graduationCheck(sc);
                    case "7" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }


    void showCourseOffering(Scanner sc) {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) showDeptOffering(dept.get(vl - 1).get(0), sc);
                else if(vl == dept.size() + 1) {
//                    return to previous method
                }
                else {
                    runner = true;
                }
            } catch (NumberFormatException e) {
                runner = true;
            }
        } while(runner);
    }

    void viewStudentRecord(Scanner sc) {
        String sId = null;
//        Scanner sc = new Scanner(System.in);
        boolean isValid = false;
        while(!isValid) {
            System.out.print("Enter the Student ID = ");
            sId = sc.next();
            String query = String.format("select * from student where lower(id) = lower('%s')", sId);
            if(runQuery(query, true)) {
                isValid = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "There is no student with that id" + DataStorage.ANSI_RESET);
            }
        }

        ResultSet studentData = getResultSet(String.format("select * from student  inner join department on department.deptid = student.deptid where lower(student.id) = lower('%s')", sId));
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
//        boolean outer;
//        do {
//            outer = false;
        clearScreen();
        CLI cli = new CLI();
        String query = String.format("SELECT DISTINCT session FROM _%s ORDER BY session ASC", sId);
        float cumulativeEarnedCredits = 0;
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

                String header = String.format("Academic Session: %d-%d", session[0], session[1]);
                stmt = connection.createStatement();
                ResultSet rs2 = stmt.executeQuery(query);
                float earnedCredits = 0;
                float registeredCredits = 0;
                float SGPA = 0;
                float CGPA = 0;
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
                        if(grade != null)
                            gp = dataStorage.GradePointMap.get(grade);
                        if (gp != 0) {
                            earnedCredits += credits;
                            cumulativeEarnedCredits += credits;
                        }
                        totalGP += (credits * gp);
                    }

                    temp.add(courseId);
                    temp.add(courseName);
                    temp.add(ltpc);
                    temp.add(status == DataStorage._COMPLETED ? "Completed" : "Running");
                    temp.add(grade!=null && status == DataStorage._COMPLETED ? grade : "N/A");
                    data.add(temp);
                }
                if (status == DataStorage._COMPLETED) {
                    totalSemester++;
                    cumulativeTotalGP += totalGP;
                    SGPA = (totalGP / registeredCredits);
                }
                if(cumulativeEarnedCredits == 0)
                    CGPA = 0;
                else
                    CGPA = (cumulativeTotalGP / cumulativeEarnedCredits);

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
                TRANSCRIPT.append(cli.specialPrint(header, options, data, footerOptions, footerData)).append("\n\n");
            }
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ArrayList<String> options = new ArrayList<>();
        options.add("Generate Transcript");
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String input = sc.next();
            switch (input) {
                case "1" -> {
//                    generate transcript
                    fileWriterUtil.generateTranscript(sId, TRANSCRIPT.toString());
                    System.out.println(DataStorage.ANSI_GREEN + "Transcript Generated successfully in the documents folder" + DataStorage.ANSI_RESET);
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
//        } while(outer);
    }

    private void showDeptOffering(String deptId, Scanner sc) {
        boolean validInput = false;
        int year = 0;
        while(!validInput) {
            System.out.print("Enter year (1, 2, 3, 4) = ");
//            Scanner sc = new Scanner(System.in);
            String inp = sc.next();
            try{
                year = Integer.parseInt(inp);
                if(year < 1 || year > 4) {
                    System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
                }
                else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

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
                FROM course_catalog_cse table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) <> lower('e');
        """, tabName, deptId, _CURR_SESSION[0] - year + 1);
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
                FROM course_catalog_cse table2
                WHERE batch <= %d
                GROUP BY courseid
            ) subquery
            ON table1.courseid = subquery.courseid
            AND table1.batch = subquery.max_value
            INNER JOIN faculty t3 on table1.fid = t3.id
            WHERE lower(table1.type) = lower('e');
        """, tabName, deptId, _CURR_SESSION[0] - year + 1);
        data = fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);
        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
//            Scanner sc = new Scanner(System.in);
            System.out.print("> ");
            String inp = sc.next();
            switch (inp) {
                case "1" -> {
//                    return back
                }
                default -> {
                    runner = true;
                }
            }
        } while(runner);
    }
    private void graduationCheck(Scanner sc) {
        String sId = null;
//        Scanner sc = new Scanner(System.in);
        boolean isValid = false;
        while(!isValid) {
            System.out.print("Enter the Student ID = ");
            sId = sc.next();
            String query = String.format("select * from student where lower(id) = lower('%s')", sId);
            if(runQuery(query, true)) {
                isValid = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "There is no student with that id" + DataStorage.ANSI_RESET);
            }
        }
        ArrayList<Float> earnedCredits = new ArrayList<>();
        try {
            String query = generateQuery(sId.substring(0, 4),"pc");
            earnedCredits.add(getResultSet(query).getFloat("credits"));
            query = generateQuery(sId.substring(0, 4),"ec");
            earnedCredits.add(getResultSet(query).getFloat("credits"));
            query = generateQuery(sId.substring(0, 4),"e");
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
        System.out.print("> ");
        String inp = sc.next().trim();
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

            options = new ArrayList<>();
            options.add("Create New Event");
            options.add("Back");
            cli.createVSubmenu("SubMenu", null, options);
//            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        createNewEvent(sc);
                        outer = true;
                    }
                    case "2" -> {
//                    back
                    }
                    default -> runner = true;
                }
            } while (runner);
        } while(outer);
    }


    private void createNewEvent(Scanner sc) {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        options.add("Start new Semester");
        options.add("Start Course Float Event");
        options.add("End Course Float Event");
        options.add("Start Course Registration Event");
        options.add("End Course Registration Event");
        options.add("Start Grade Submission Event");
        options.add("End Grade Submission Event");
        options.add("End the Semester");
        options.add("Back");
        cli.createVSubmenu("SubMenu", null, options);
//        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 &&  vl <= 8) {
                    int temp = vl - 2;
                    temp = (temp < 0 ? DataStorage._SEMESTER_END : temp);
                    if(_EVENT == temp) {
                        String query;
                        if(vl - 1 == DataStorage._SEMESTER_START) {
                            if(_CURR_SESSION[1] == 2) {
                                _CURR_SESSION[0]++;
                                _CURR_SESSION[1] = 1;
                            }
                            else {
                                _CURR_SESSION[1] = 2;
                            }
                            ArrayList<ArrayList<String>> Alldept = getAllDept();
                            for(var dept : Alldept) {
                                for(int yr = 1; yr <= 4; yr++) {
                                    query = String.format("DELETE FROM y%d_%s_offering", yr, dept.get(0));
                                    runQuery(query, false);
                                }
                            }
                            query = String.format("UPDATE EVENT SET _EVENT = 0, " +
                                    "_SESSION = ARRAY[%d, %d]", _CURR_SESSION[0], _CURR_SESSION[1]);
                        }
                        else {
                            query = String.format("UPDATE EVENT SET _EVENT = %d", vl - 1);
                        }
                        runQuery(query, false);

                    }
                    else {
                        System.out.println(DataStorage.ANSI_RED + "Not allowed to create this Event" + DataStorage.ANSI_RESET);
                        runner = true;
                    }
                }
                else if (vl == 9) {
//                    BACK
                }
                else {
                    runner = true;
                }
            }
            catch (NumberFormatException exception){
                runner = true;
            }
        } while(runner);
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






    @Override
    public void addNewCourseinCatalog(String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry currently you can't edit the course Catalog");
            return;
        }
//        Scanner sc = new Scanner(System.in);
        String courseId = null;
        String courseName = null;
        Integer[] ltp = new Integer[3];
        String[] prereq = null;
        String type = null;
        int batch = _CURR_SESSION[0];
        boolean validInput = false;
        while(!validInput) {
            System.out.print("Enter the course ID = ");
            courseId = sc.next();
            String query = String.format("SELECT * FROM COURSE_CATALOG_%s where LOWER(courseid) = LOWER('%s')", deptId, courseId);
            if(runQuery(query, true)) {
                System.out.println(DataStorage.ANSI_RED + "There is already a course with that id" + DataStorage.ANSI_RESET);
            }
            else {
                validInput = true;
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter Course name = ");
            sc.nextLine();
            String name = sc.nextLine();
            System.out.println(name);
            if(name != null) {
                courseName = name;
                validInput = true;
            }
        }
        validInput = false;
        while(!validInput) {
            System.out.print("Enter ltp structure (l,t,p) = ");
            String temp = sc.nextLine();
            if(isIntegerArray(temp)) {
                String[] ttt = temp.split(",");
                System.out.println(Arrays.toString(ttt));
                for(int i = 0; i < ttt.length; i++) ltp[i] = Integer.parseInt(ttt[i].trim());
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter prerequisites (comma separated courseId) = ");
            String tt = sc.nextLine();
            if(checkPrereq(tt, deptId)) {
                prereq = tt.split(",");
                for(int i = 0; i < prereq.length; i++) prereq[i] = prereq[i].trim();
                validInput = true;
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Enter the course type(pc or ec or e) = ");
            type = sc.next().trim();
            if(type.equalsIgnoreCase("pc") || type.equalsIgnoreCase("ec") || type.equalsIgnoreCase("e")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "enter valid input" + DataStorage.ANSI_RESET);
            }
        }


        String query = String.format("insert into course_catalog_%s values(?,?,?,?,?,?)", deptId);
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, courseId);
            pstmt.setString(2, courseName);
            Array arr = connection.createArrayOf("INTEGER", ltp);
            pstmt.setArray(3, arr);
            arr = connection.createArrayOf("VARCHAR", prereq);
            pstmt.setArray(4, arr);
            pstmt.setString(5, type);
            pstmt.setInt(6, batch);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean findCourse(String courseId, String deptId) {
        String query = String.format("Select * from course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs.isBeforeFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isIntegerArray(String input) {
        String[] values = input.split(",");
        for (String value : values) {
            try {
                Integer.parseInt(value.trim());
                System.out.println(value);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return values.length == 3;
    }
    @Override
    public void editCourseCatalog(String deptId, Scanner sc) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry you can't edit the course Catalog");
            return;
        }
//        Scanner sc = new Scanner(System.in);
        String courseId = null;
        boolean validInput = false;
        while(!validInput) {
            System.out.print("Enter the course ID = ");
            courseId = sc.next();
            String query = String.format("SELECT * FROM COURSE_CATALOG_%s where LOWER(courseid) = LOWER('%s')", deptId, courseId);
            if(!runQuery(query, true)) {
                System.out.println(DataStorage.ANSI_RED + "There is no such course in the catalog with this id" + DataStorage.ANSI_RESET);
            }
            else {
                validInput = true;
            }
        }
        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the course Name (y/n) = ");
            String input = sc.next().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter Course name = ");
                    sc.nextLine();
                    String name = sc.nextLine();
                    System.out.println(name);
                    if(name != null) {
                        String query = String.format("UPDATE COURSE_CATALOG_%s SET coursename = '%s' where lower(courseid) = lower('%s')", deptId, name, courseId);
                        runQuery(query, false);
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the ltp structure (y/n) = ");
            String input = sc.next().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter ltp (l,t,p) = ");
                    sc.nextLine();
                    input = sc.nextLine();
                    System.out.println(input);
                    if(isIntegerArray(input)) {
                        String[] val = input.split(",");
                        System.out.println(Arrays.toString(val));
                        String query = String.format("UPDATE COURSE_CATALOG_%s SET ltp = array%s where lower(courseid) = lower('%s')",
                                deptId, Arrays.toString(val), courseId);
                        runQuery(query, false);
                        validInput = true;
                    }
                    else {
                        System.out.println("not a valid structure");
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to edit the prerequisites (y/n) = ");
            String input = sc.next().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter prerequisites (comma separated courseId) = ");
                    sc.nextLine();
                    input = sc.nextLine();
                    if(checkPrereq(input, deptId)) {
                        String[] val = input.split(",");
                        for(int i = 0; i < val.length; i++) val[i] = val[i].trim();
                        String query = String.format("UPDATE course_catalog_%s set prereq = ? where lower(courseid) = lower('%s')", deptId, courseId);
                        try {
                            PreparedStatement pstmt = connection.prepareStatement(query);
                            Array arr = connection.createArrayOf("VARCHAR", val);
                            pstmt.setArray(1, arr);
                            pstmt.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }

        validInput = false;
        while(!validInput) {
            System.out.print("Do you want to change the course type (y/n) = ");
            String input = sc.next().trim();
            if(input.equalsIgnoreCase("y")) {
                while(!validInput) {
                    System.out.print("Enter new Course Type (pc or ec or e) = ");
                    sc.nextLine();
                    input = sc.nextLine();
                    if(changeCourseType(input, deptId, courseId)) {
                        validInput = true;
                    }
                }
            }
            else if(input.equalsIgnoreCase("n")) {
                validInput = true;
            }
            else {
                System.out.println(DataStorage.ANSI_RED + "Enter a valid input" + DataStorage.ANSI_RESET);
            }
        }
    }

    private boolean changeCourseType(String type, String deptId, String courseId) {
        type = type.trim();
        if(!type.equalsIgnoreCase("pc") && !type.equalsIgnoreCase("ec") && !type.equalsIgnoreCase("e")) {
            return false;
        }
        String query = String.format("SELECT * FROM course_catalog_%s where lower(courseid) = lower('%s')", deptId, courseId);
        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                String courseName = rs.getString("coursename");
                Array ltp = rs.getArray("ltp");
                Array prereq = rs.getArray("prereq");
                int batch = _CURR_SESSION[0] + 1;
                query = String.format("insert into course_catalog_%s values(?,?,?,?,?,?)", deptId);
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, courseId);
                pstmt.setString(2, courseName);
                pstmt.setArray(3, ltp);
                pstmt.setArray(4, prereq);
                pstmt.setString(5, type);
                pstmt.setInt(6, batch);
                pstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean checkPrereq(String input, String deptId) {
        String[] value = input.split(",");
        for(var val : value) {
            if(!findCourse(val.trim(), deptId)) {
                System.out.println("Course " + val + " is not present in the catalog");
                return false;
            }
        }
        return true;
    }
}
