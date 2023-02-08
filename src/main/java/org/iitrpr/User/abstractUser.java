package org.iitrpr.User;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.sql.Connection;
import java.util.*;

abstract class abstractUser {
    Map<String, String> deptMap = new HashMap<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    Connection connection;
    String id;
    abstractUser(Connection connection, String id) {
        this.connection = connection;
        this.id = id;
        deptMap.put("cse", "Computer Science and Engineering");
        deptMap.put("ce", "Civil Engineering");
        deptMap.put("che", "Chemical Engineering");
        deptMap.put("ee", "Electrical Engineering");
        deptMap.put("mce", "Mathematics and Computing Engineering");
        deptMap.put("me", "Mechanical Engineering");
        deptMap.put("mme", "Metallurgical and Materials Engineering");
        System.out.println(ANSI_CYAN + "\n\nAIMS PORTAL WELCOMES U ;)\n" + ANSI_RESET);
    }

    abstract void showMenu();
    void showPersonalDetails() {

    };
    abstract void editPersonalDetails();
    abstract void showAllCourse();
    abstract void logout();

}
