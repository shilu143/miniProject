package org.iitrpr.utils;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.ArrayList;
import java.util.List;

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
            List<String> temp = options.subList(i*cols, i*cols + sz%cols);
            for(int j = 0; j < 2; j++) {
                temp.add(null);
            }
            menuTable.addRow(temp)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(1);
            menuTable.addRule();
        }


        menuTable.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(menuTable.render() + "\n\n");
    }

    public void createVerticalTable(ArrayList<String> options, ArrayList<String> data) {
        assert options.size() == data.size();

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow(null, "PERSONAL DETAILS").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        for(int i = 0;i < options.size();i++) {
            table.addRow(options.get(i).toUpperCase(),data.get(i)).setPaddingLeftRight(2);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render());
    }

    public void createVSubmenu(String header,String body, ArrayList<String> options){

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow(null, header).setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        if(body != null) {
            table.addRow(null, body)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(2)
                    .setPaddingTopBottom(4);
            table.addRule();
        }

        table.addRow("Options", "KeyPress").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        for(int i = 0;i < options.size();i++) {
            table.addRow(options.get(i).toUpperCase(),String.valueOf(i + 1))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(2);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render()+"\n");
    }

    public void recordPrint(String header, ArrayList<String>options,
                            ArrayList<ArrayList<String>> data,
                            ArrayList<String> footerOptions,
                            ArrayList<String> footerData) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        ArrayList<String> headerData = new ArrayList<>();
        for(int i = 0; i < options.size() - 1; i++) {
            headerData.add(null);
        }
        headerData.add(header);

        table.addRow(headerData).setTextAlignment(TextAlignment.CENTER).setPaddingLeftRight(1);
        table.addRule();
        headerData = new ArrayList<>();
        for(int i = 0; i < options.size() - footerOptions.size(); i++) {
            headerData.add(null);
        }
        for(int i = 0;i < footerOptions.size(); i++){
            headerData.add(String.format("%s : %s", footerOptions.get(i), footerData.get(i)));
        }
        table.addRow(headerData)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingLeftRight(1);
        table.addRule();

        table.addRule();
        table.addRow(options).setTextAlignment(TextAlignment.CENTER).setPaddingLeftRight(1);
        table.addRule();

        for (ArrayList<String> datum : data) {
            table.addRow(datum).setTextAlignment(TextAlignment.CENTER).setPaddingLeftRight(1);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render()+"\n");
    }
}
