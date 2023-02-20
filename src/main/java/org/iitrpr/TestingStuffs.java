package org.iitrpr;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class TestingStuffs {
    public static void main(String[] args) {
        String myString = "cs2010";
        String myRegex = "\\b(CS101|cs101|Computer Science 101)\\b";
        boolean matches = myString.matches("^[a-z][a-z][1-9][0-9][0-9]$");

        if (matches) {
            System.out.println("The string matches the regex!");
        } else {
            System.out.println("The string does not match the regex.");
        }

    }
}
