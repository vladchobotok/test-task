package org.testtask;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.testtask.GetData.readFromUrl;

public class ReadInput {

    private static final String URL_BASE = "https://bank.gov.ua/NBUStatService/v1/statdirectory/res?date=&json";
    private static Calendar currentUrlDate;
    private static String currentUrl;
    private static final StringBuilder endDate = new StringBuilder();
    private static final List<String[]> csvLines = new ArrayList<>();
    private static String currentDt;
    private static double currentValue;
    private static double previousValue = 0;
    private static double difference;

    public void readJsonAndCovertToCsv(){ //func for reading json files from NBU site, getting info from them and converting them into single CSV file

        Scanner scanner = new Scanner(System.in);
        String[] input = scanner.nextLine().split(" ");

        if(!(input[0].equals("GET"))) {

            System.out.println("Wrong command");

        }
        else {
            currentUrlDate = Calendar.getInstance();
            currentUrlDate.set(Calendar.DAY_OF_MONTH, 1);
            currentUrlDate.set(Calendar.MONTH, 0);
            currentUrlDate.set(Calendar.YEAR, 2004);

            endDate.append(input[1], input[1].indexOf("date=") + 5, input[1].lastIndexOf("date=") + 11);
            if(endDate.charAt(4) == '0'){
                endDate.deleteCharAt(4);
            }

            csvLines.add(new String[]{"Month", "Value", "Difference"});

            do {

                ThreadForMonths threadForMonths = new ThreadForMonths();
                threadForMonths.start();
                try {
                    threadForMonths.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentUrlDate.add(Calendar.MONTH, 1);

            } while (!(Integer.toString(currentUrlDate.get(Calendar.YEAR)) +
                    currentUrlDate.get(Calendar.MONTH)).equals(endDate.toString()));

            makeCsv();

            System.out.println("Check output in \"output.csv\"");
        }
    }

    public String refactorToCsv(String[] data) { // func for refactoring data for CSV file
        return String.join(",", data);
    }

    public void makeCsv() { // func, which creates the CSV file
        File csvOutputFile = new File("output.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            csvLines.stream()
                    .map(this::refactorToCsv)
                    .forEach(pw::println);
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
    }

    public class ThreadForMonths extends Thread { // class, which allows reading json files in multithreading way

        @Override
        public void run(){

            String dateMonth;
            if(currentUrlDate.get(Calendar.MONTH) < 9){
                dateMonth = "0" + (currentUrlDate.get(Calendar.MONTH) + 1);
            }
            else {
                dateMonth = Integer.toString(currentUrlDate.get(Calendar.MONTH) + 1);
            }

            currentUrl = URL_BASE;
            currentUrl = currentUrl.replace("date=","date="
                    + currentUrlDate.get(Calendar.YEAR)
                    + dateMonth);

            String[] jsonObjectsAsStrings;
            try {
                jsonObjectsAsStrings = readFromUrl(currentUrl).split("(?<=}),(?=\\{)");
                List<JSONObject> jsonObjects = new ArrayList<>();

                for (int i = 0; i < jsonObjectsAsStrings.length; i++) {
                    jsonObjects.add(new JSONObject(jsonObjectsAsStrings[i]));
                }

                List<JSONObject> currJson = jsonObjects.stream()
                        .filter(obj -> obj.get("id_api")
                                .equals("RES_OffReserveAssets"))
                        .collect(Collectors.toList());

                currentDt = currJson.get(0).get("dt").toString();
                currentValue = Double.parseDouble(currJson.get(0).get("value").toString());
                if(currentDt.equals("20040101")){
                    previousValue = currentValue;
                }
                difference = BigDecimal.valueOf(currentValue).subtract(BigDecimal.valueOf(previousValue)).doubleValue();
                csvLines.add(new String[]{currentDt, Double.toString(currentValue), Double.toString(difference)});
                previousValue = currentValue;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
