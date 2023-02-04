package org.iitrpr.models;

import org.iitrpr.utils.Table;
public class User {
    private String email;
    private String name;
    private String id;
    private String role;
    private String dept;
    public User(String name, String id, String role, String dept, String email) {
        this.name = name;
        this.id = id;
        this.role = role;
        this.dept = dept;
        this.email = email;
    }
    public void show() {
        String[] headers = {"NAME", "ID", "ROLE", "DEPARTMENT", "EMAIL"};
        String[][] details = {
                {
                        name.toUpperCase(),
                        id,
                        role.toUpperCase(),
                        dept.toUpperCase(),
                        email
                }
        };
        Table table = new Table();
        table.setHeaders(headers);
        for(var detail : details) {
            table.addRow(detail);
        }
        table.print();
    }
}
