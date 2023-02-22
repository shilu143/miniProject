package org.iitrpr.User;

import org.iitrpr.TestingStuffs;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AcademicOffice extends abstractUser{
    public AcademicOffice(Connection connection, String id, String role) {
        super(connection, id, role);
    }

    @Override
    public void showMenu() {
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
            cli.createVSubmenu("Menu","Welcome to AIMS Portal", options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> showPersonalDetails();
                    case "2" -> viewCourseCatalog();
                    case "3" -> showCourseOffering();
                    case "4" -> viewStudentRecord();
//                    case "5" -> isGraduated();
                    case "7" -> logout();
                    default -> runner = true;
                }
            } while(runner);
        } while(!isLoggedout);
    }

    private void viewStudentRecord() {
        String sId = null;
        Scanner sc = new Scanner(System.in);
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
        String TRANSCRIPT = "";
//        boolean outer;
//        do {
//            outer = false;
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
                            int gp = dataStorage.GradePointMap.get(grade);
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
                        temp.add(status == DataStorage._COMPLETED ? grade : "N/A");
                        data.add(temp);
                    }
                    if (status == DataStorage._COMPLETED) {
                        totalSemester++;
                        cumulativeTotalGP += totalGP;
                        SGPA = (totalGP / registeredCredits);
                    }
                    CGPA = (cumulativeTotalGP / cumulativeEarnedCreated);

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
                    TRANSCRIPT = cli.specialPrint(header, options, data, footerOptions, footerData);
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
                        TestingStuffs.generateTranscript(sId, TRANSCRIPT);
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

    private void showCourseOffering() {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) showDeptOffering(dept.get(vl - 1).get(0));
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

    private void showDeptOffering(String deptId) {
        boolean validInput = false;
        int year = 0;
        while(!validInput) {
            System.out.print("Enter year (1, 2, 3, 4) = ");
            Scanner sc = new Scanner(System.in);
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

        String tabName = String.format("y%d_%s_offering", year, deptId);
        String query = String.format("select t1.courseid , t2.coursename, t2.ltp, t2.prereq, t2.type, t1.cgcriteria, t3.name as Instructor\n" +
                "from %s t1\n" +
                "inner join course_catalog_%s t2 on t1.courseid = t2.courseid\n" +
                "inner join faculty t3 on t1.fid=t3.id\n" +
                "where lower(t2.type) <> lower('e')", tabName, deptId);
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
        query = String.format("select t1.courseid , t2.coursename, t2.ltp, t2.prereq, t2.type, t1.cgcriteria, t3.name as Instructor\n" +
                "from %s t1\n" +
                "inner join course_catalog_%s t2 on t1.courseid = t2.courseid\n" +
                "inner join faculty t3 on t1.fid=t3.id\n" +
                "where lower(t2.type) = lower('e')", tabName, deptId);
        data = fetchTable(query);
        cli.recordPrint("Elective Courses", options, data, null, null);
        options = new ArrayList<>();
        options.add("Back");
        cli.createVSubmenu("Sub Menu", null, options);
        boolean runner;
        do {
            runner = false;
            Scanner sc = new Scanner(System.in);
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

    private void viewCourseCatalog() {
        clearScreen();
        CLI cli = new CLI();
        ArrayList<String> options = new ArrayList<>();
        ArrayList<ArrayList<String>> dept = getAllDept();
        for(var d : dept) {
            options.add(d.get(1));
        }
        options.add("Back");
        cli.createVSubmenu("Choose Department", null, options);
        Scanner sc = new Scanner(System.in);
        boolean runner;
        do {
            runner = false;
            System.out.print("> ");
            String inp = sc.nextLine().trim();
            try {
                int vl = Integer.parseInt(inp);
                if(vl >= 1 && vl <= dept.size()) showDeptCourse(dept.get(vl - 1).get(0));
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

    private void showDeptCourse(String deptid) {
        clearScreen();
        boolean outer;
        do {
            outer = false;
            CLI cli = new CLI();
            String query = String.format("SELECT * FROM course_catalog_%s", deptid);
            ArrayList<ArrayList<String>> data = fetchTable(query);
            ArrayList<String> options = new ArrayList<>();
            options.add("Course ID");
            options.add("Course Name");
            options.add("LTP");
            options.add("Prerequisites");
            options.add("Type");
            options.add("Batch Onwards");
            cli.recordPrint(deptid.toUpperCase() + " Courses",options, data, null, null);
            options = new ArrayList<>();
            options.add("Add New Course");
            options.add("Edit Course");
            options.add("Back");
            cli.createVSubmenu("Menu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine().trim();
                switch (inp) {
                    case "1" -> {
                        addNewCourseinCatalog(deptid);
                        outer = true;
                    }
                    case "2" -> {
                        editCourseCatalog(deptid);
                        outer = true;
                    }

                    case "3" -> {
    //                    return back
                    }
                    default -> runner = true;
                }
            } while(runner);
        }   while(outer);
    }

    private void addNewCourseinCatalog(String deptId) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry you can't edit the course Catalog");
            return;
        }
        Scanner sc = new Scanner(System.in);
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

    private void editCourseCatalog(String deptId) {
        fetchEvent();
        if(_EVENT > DataStorage._SEMESTER_START && _EVENT < DataStorage._SEMESTER_END) {
            System.out.println("Sorry you can't edit the course Catalog");
            return;
        }
        Scanner sc = new Scanner(System.in);
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


    private void showEvents() {
        try {
            String query = "SELECT * FROM EVENT";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            boolean _courseFloat = false;
            boolean _courseReg = false;
            boolean _semester = false;
            int[] _session = null;
            while (rs.next()) {
                _courseFloat = rs.getBoolean("_coursefloat");
                _courseReg = rs.getBoolean("_coursereg");
                _semester = rs.getBoolean("_semester");
                Array a = rs.getArray("_session");
                _session = (int[])a.getArray();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void showPersonalDetails() {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> headers = new ArrayList<>();
            headers.add("name");
            headers.add("id");
            headers.add("role");
            headers.add("department");
            headers.add("email");
            headers.add("contact no.");
            ArrayList<String> data = fetchData();
            cli.createVerticalTable(headers, data);

            ArrayList<String> options = new ArrayList<>();
            options.add("Back");
            options.add("Edit");


            cli.createVSubmenu("SubMenu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        //returns to previous method
                    }
                    case "2" -> {
                        editPersonalDetails();
                        outer = true;
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
    }

    @Override
    void editPersonalDetails() {
        System.out.print("Enter your new Contact number = ");
        Scanner sc = new Scanner(System.in);
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
