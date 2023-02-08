package org.iitrpr.User;

import java.sql.*;
import de.vandermeer.asciitable.*;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Student extends abstractUser {
    public Student(Connection connection, String id) {
        super(connection, id);
    }

    private String[] fetchData() {
        PreparedStatement st = null;
        try {
            String query = String.format("SELECT * FROM STUDENT " +
                    "WHERE LOWER(id) = LOWER('%s')", id);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String name = null;
            String dept = null;
            String email = null;
            String contact = null;
            while(rs.next()) {
                name = rs.getString("name");
                dept = rs.getString("dept");
                email = rs.getString("email");
                contact = rs.getString("contact");
            }

            assert name != null;

            return new String[]{name.toUpperCase(), id.toUpperCase(), "STUDENT", deptMap.get(dept), email, contact};
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showMenu() {

//        System.out.println("This is a Student portal " + id.toUpperCase());


        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow(null, "PERSONAL DETAILS").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        String[] headers = {
                "name", "id", "role", "department", "email", "contact no."
        };
        String[] data = fetchData();

        for(int i = 0;i < headers.length;i++) {
            table.addRow(headers[i].toUpperCase(),data[i]).setPaddingLeftRight(2);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render());

    }

    @Override
    void showPersonalDetails() {

    }

    @Override
    void editPersonalDetails() {

    }

    @Override
    void showAllCourse() {

    }

    @Override
    void logout() {

    }

}