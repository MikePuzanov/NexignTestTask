package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Function {
    private final static String root = "src/main/java/org/example/";

    public static Map<String, ArrayList<String>> readFromFile() throws RuntimeException, IOException {
        File file = new File(root + "test_data/cdr.txt");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        Map<String, ArrayList<String>> dict = new HashMap<>();
        String line;
        while((line = br.readLine()) != null){
            var number = line.split(", ")[1];
            if (dict.containsKey(number))
            {
                var list = dict.get(number);
                list.add(line);
            } else {
                dict.put(number, new ArrayList<>());
            }
        }
        br.close();
        return dict;
    }

    private class DataComparator implements Comparator<String> {

        public int compare(String a, String b) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dateA = null;
            Date dateB = null;
            try {
                dateA = formatter.parse(a.split(", ")[2]);
                dateB = formatter.parse(b.split(", ")[2]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return dateA.compareTo(dateB);
        }
    }

    private void writeToFileStart(FileWriter writer, String number, String tariff) throws IOException {
        String str = "Tariff index: " + tariff + "\n";
        writer.write(str);
        str = "----------------------------------------------------------------------------\n";
        writer.write(str);
        str = "Report for phone number " + number + ":\n";
        writer.write(str);
        str = "----------------------------------------------------------------------------------------------\n";
        writer.write(str);
        str = "| Call Type |         Start Time           |           End Time           | Duration | Cost  |\n";
        writer.write(str);
        str = "----------------------------------------------------------------------------------------------\n";
        writer.write(str);
    }

    private void writeToFileEnd(FileWriter writer, double bil) throws IOException {

        String str = "----------------------------------------------------------------------------\n";
        writer.write(str);
        str = "|                                           Total Cost: |     " + bil + " rubles |\n";
        writer.write(str);
        str = "----------------------------------------------------------------------------\n";
        writer.write(str);
    }

    public void makeReport(String number, ArrayList<String> list) {
        var tariff = list.get(0).split(", ")[4];
        Comparator<String> comparator = new DataComparator();
        list.sort(comparator);
        String path = "reports/" + number + "_report.txt";
        try (FileWriter writer = new FileWriter(path, false)) {
            writeToFileStart(writer, number, tariff);
            double bill = 0;
            switch (tariff) {
                case "03":
                    bill = countTariff03(writer, list);
                    break;
                case "06":
                    bill = countTariff06(writer, list);
                    break;
                case "11":
                    bill = countTariff11(writer, list);
                    break;
            }
        writeToFileEnd(writer, bill);
        writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTime(Date dateA, Date dateB) {
        long difference_In_Time = dateB.getTime() - dateA.getTime();
        long difference_In_Seconds = (difference_In_Time / 1000) % 60;
        long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
        long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
        String time = "";
        if (difference_In_Hours > 9) {
            time += difference_In_Hours + ":";
        } else {
            time += "0" + difference_In_Hours + ":";
        }
        if (difference_In_Minutes > 9) {
            time += difference_In_Minutes + ":";
        } else {
            time += "0" + difference_In_Minutes + ":";
        }
        if (difference_In_Seconds > 9) {
            time += difference_In_Seconds;
        } else {
            time += "0" + difference_In_Seconds;
        }
        return time;
    }

    private int getMinute(String time) {
        int min = 0;
        if (time.charAt(3) == '0') {
            min = Integer.parseInt(String.valueOf(time.charAt(4)));
        } else {
            var str = time.substring(3, 5);
            min = Integer.parseInt(str);
        }
        int sec = Integer.valueOf(time.substring(6));
        return sec > 0 ? min + 1 : min;
    }

    private Date[] getDates(String line) {
        var arr = line.split(", ");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date dateA = null;
        Date dateB = null;
        try {
            dateA = formatter.parse(arr[2]);
            dateB = formatter.parse(arr[3]);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new Date[] {dateA, dateB};
    }

    private double countTariff03(FileWriter writer, ArrayList<String> list) throws IOException {
        double sum = 0;
        String str = "";
        for (String line : list) {
            var dates = getDates(line);
            String time = getTime(dates[0], dates[1]);
            var min = getMinute(time); /// время звонка в минутах
            var cost = min * 1.5;
            sum += cost;
            str = "|     " + line.split(", ")[0] + "    | " + dates[0] + " | " + dates[1] + " | " + time + " |  " + cost + " |\n";
            writer.write(str);
        }
        return sum;
    }

    private double countTariff06(FileWriter writer, ArrayList<String> list) throws IOException {
        double sum = 100.0;
        int minAll = 0;
        var condition = false;
        String str = "";
        for (String line : list) {
            var dates = getDates(line);
            String time = getTime(dates[0], dates[1]);
            int min = getMinute(time); /// время звонка в минутах
            minAll += min;
            double cost = 0;
            if (condition) {
                cost = min;
            }
            if (minAll > 300) {
                condition = true;
                cost = minAll - 300;
            }
            sum += cost;
            str = "|     " + line.split(", ")[0] + "    | " + dates[0] + " | " + dates[1] + " | " + time + " |  " + cost + " |\n";
            writer.write(str);
        }

        return sum;
    }

    private double countTariff11(FileWriter writer, ArrayList<String> list) throws IOException {
        double sum = 0;
        int minAll = 0;
        var condition = false;
        String str = "";
        for (String line : list) {
            var type = line.split(", ")[0];
            var dates = getDates(line);
            double cost = 0;
            String time = getTime(dates[0], dates[1]);
            if (type.equals("02")) {
                cost = 0;
            } else {
                int min = getMinute(time); /// время звонка в минутах
                minAll += min;
                if (minAll <= 100) {
                    cost = min * 0.5;
                } else if (condition) {
                    cost = min * 1.5;
                } else {
                    condition = true;
                    cost = 0.5 * (100 - (minAll - min)) + (minAll - 100) * 1.5;
                }
            }
            str = "|     " + type + "    | " + dates[0] + " | " + dates[1] + " | " + time + " |  " + cost + " |\n";
            writer.write(str);
            sum += cost;
        }

        return sum;
    }
}
