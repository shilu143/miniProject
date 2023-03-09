package org.iitrpr.utils;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class CLI {
    public void createDiff(String header, ArrayList<String> title,  ArrayList<String> options, ArrayList<Float> req, ArrayList<Float> actual) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow(null, null, header).setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingLeftRight(1);
        table.addRule();
        for(int i = 0; i < options.size(); i++) {
            table.addRow(options.get(i), req.get(i), actual.get(i))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(1);
            table.addRule();
        }
        table.getRenderer().setCWC(new CWC_LongestLine());
        System.out.println(table.render() + "\n\n");
    }

    public void createVerticalTable(String header, ArrayList<String> options, ArrayList<String> data) {
        assert options.size() == data.size();

        AsciiTable table = new AsciiTable();
        if(header != null) {
            table.addRule();
            table.addRow(null, header).setTextAlignment(TextAlignment.CENTER);
            table.addRule();
        }

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

    public String recordPrint(String header, ArrayList<String>options,
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
        if(footerOptions != null) {
            headerData = new ArrayList<>();
            for (int i = 0; i < options.size() - footerOptions.size(); i++) {
                headerData.add(null);
            }
            for (int i = 0; i < footerOptions.size(); i++) {
                headerData.add(String.format("%s : %s", footerOptions.get(i), footerData.get(i)));
            }
            table.addRow(headerData)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeftRight(1);
            table.addRule();
        }

        table.addRule();
        table.addRow(options).setTextAlignment(TextAlignment.CENTER).setPaddingLeftRight(1);
        table.addRule();
        if(data != null)
            for (ArrayList<String> datum : data) {
                for (int i = 0; i < datum.size(); i++) {
                    datum.set(i,datum.get(i).toUpperCase());
                }
                table.addRow(datum).setTextAlignment(TextAlignment.CENTER).setPaddingLeftRight(1);
                table.addRule();
            }
        table.getRenderer().setCWC(new CWC_LongestLine());
        String render = table.render() + "\n";
        System.out.println(render);
        return render;
    }
}
