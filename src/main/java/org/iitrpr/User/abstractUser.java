package org.iitrpr.User;

import org.iitrpr.TestingStuffs;
import org.iitrpr.utils.CLI;
import org.iitrpr.utils.DataStorage;

import java.sql.*;
import java.util.*;

abstract class abstractUser {
    DataStorage dataStorage;
    Connection connection;
    String id;
    boolean isLoggedout;
    Integer[] _CURR_SESSION;
    int _EVENT;
    String role;
    abstractUser(Connection connection, String id, String role) {
        this.connection = connection;
        this.id = id;
        this.role = role;
        isLoggedout = false;
        dataStorage = new DataStorage();
        System.out.println(DataStorage.ANSI_CYAN + "\n\nAIMS PORTAL WELCOMES U ;)\n" + DataStorage.ANSI_RESET);
    }

    protected String getFacultyName(String fid) {
        String query = String.format("SELECT name from faculty where LOWER(id) = LOWER('%s')", fid);
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
    protected boolean runQuery(String query, boolean response) {
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


//    protected
    protected ArrayList<ArrayList<String>> getAllDept() {
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



    protected ArrayList<String> fetchData() {
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
            while(rs.next()) {
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

    void showPersonalDetails(Integer usr) {
        boolean outer;
        do {
            outer = false;
            clearScreen();
            CLI cli = new CLI();

            ArrayList<String> options = new ArrayList<>();
            options.add("name");
            options.add("id");
            options.add("role");
            if(usr == DataStorage._STUDENT)
                options.add("batch");
            options.add("department");
            options.add("email");
            options.add("contact no.");
            ArrayList<String> data = fetchData();
            if(usr == DataStorage._STUDENT)
                data.add(3, id.substring(0,4));//extra
            cli.createVerticalTable("Personal Details", options, data);

            options = new ArrayList<>();
            options.add("Edit");
            options.add("Back");



            cli.createVSubmenu("SubMenu", null, options);
            Scanner sc = new Scanner(System.in);
            boolean inner;
            do {
                inner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        editPersonalDetails();
                        outer = true;
                    }
                    case "2" -> {
                        //returns to previous method
                    }
                    default -> inner = true;
                }
            } while (inner);
        }   while(outer);
    }

    void currentEvent() {
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
            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine();
                switch (inp) {
                    case "1" -> {
                        createNewEvent();
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

    protected void viewCourseCatalog() {
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
            if(role.equalsIgnoreCase("office")) {
                options.add("Add New Course");
                options.add("Edit Course");
            }
            else if(role.equalsIgnoreCase("faculty")) {
                options.add("Float Course");
            }
            options.add("Back");
            cli.createVSubmenu("Menu", null, options);

            Scanner sc = new Scanner(System.in);
            boolean runner;
            do {
                runner = false;
                System.out.print("> ");
                String inp = sc.nextLine().trim();
                if(role.equalsIgnoreCase("office")) {
                    switch (inp) {
                        case "1" -> {
                            addNewCourseinCatalog(deptid);
                            runner = true;
                        }
                        case "2" -> {
                            editCourseCatalog(deptid);
                            runner = true;
                        }

                        case "3" -> {
                            //                    return back
                        }
                        default -> runner = true;
                    }
                }
                else {
                    switch (inp) {
                        case "1" -> {
                            floatCourse(deptid);
                            runner = true;
                        }
                        case "2" -> {
//                            return back
                        }
                        default -> runner = true;
                    }
                }
            } while(runner);
        }   while(outer);
    }

    protected abstract void floatCourse(String deptid);


    protected abstract void editCourseCatalog(String deptid);

    protected abstract void addNewCourseinCatalog(String deptid);

    private void createNewEvent() {
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
        Scanner sc = new Scanner(System.in);
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

    void viewStudentRecord() {
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
                        int gp = dataStorage.GradePointMap.get(grade);
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
                    temp.add(status == DataStorage._COMPLETED ? grade : "N/A");
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
                    TestingStuffs.generateTranscript(sId, TRANSCRIPT.toString());
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

    protected void fetchEvent() {
        String query = "SELECT * FROM EVENT";
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
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

    protected ArrayList<ArrayList<String>> fetchTable(String query) {
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

    protected ResultSet getResultSet(String query) {
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

    abstract void showMenu();
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
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
    protected void logout() {
        isLoggedout = true;
    }
}
