package org.iitrpr.utils;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.ArrayList;

public class CLI {
    public void createMenu(int cols, String header, String body, ArrayList<String> options) {

        for(int i = 0; i < options.size(); i++) {
            options.set(i, "[" + String.valueOf(i) + " - " + options.get(i) + "]");
        }

        AsciiTable menuTable = new AsciiTable();

        //header
        menuTable.addRule();
        ArrayList<String> rowData = new ArrayList<>();
        for(int i = 0; i < cols - 1;i++) {
            rowData.add(null);
        }
        rowData.add(header);
        menuTable.addRow(rowData)
                .setTextAlignment(TextAlignment.CENTER);
        menuTable.addRule();


        //body
        if(body != null) {
            rowData.set(rowData.size() - 1, body);
            menuTable.addRow(rowData)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(2)
                    .setPaddingTopBottom(4);
            menuTable.addRule();
        }

        //menu
        int sz = options.size(),i;
        for(i = 0; i < (sz/cols); i++) {
            menuTable.addRow(options.subList(i*cols, i*cols + cols))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(1);
            menuTable.addRule();
        }
        if(sz % cols != 0) {
            menuTable.addRow(options.subList(i*cols, i*cols + sz%cols))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(1);
            menuTable.addRule();
        }


        menuTable.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(menuTable.render() + "\n\n");
    }

    public void createVerticalTable(ArrayList<String> headers, ArrayList<String> data) {
        assert headers.size() == data.size();

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow(null, "PERSONAL DETAILS").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        for(int i = 0;i < headers.size();i++) {
            table.addRow(headers.get(i).toUpperCase(),data.get(i)).setPaddingLeftRight(2);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render());
    }
}
