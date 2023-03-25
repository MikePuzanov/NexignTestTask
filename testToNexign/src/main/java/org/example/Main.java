package org.example;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, ArrayList<String>> dict = new HashMap<>();
        var func = new Function();
        try {
            dict = func.readFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, ArrayList<String>> entry : dict.entrySet()) {
            func.makeReport(entry.getKey(), entry.getValue());
        }
    }
}