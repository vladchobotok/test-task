package org.testtask;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GetData {
    private static String jsonToString(Reader rd) throws IOException { // func for converting json to string

        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }

        return sb.toString();
    }

    public static String readFromUrl(String url) throws IOException { //func for reading json from URL
        try (InputStream inputStream = new URL(url).openStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String jsonArrayText = jsonToString(bufferedReader);
            jsonArrayText = jsonArrayText.substring(1, jsonArrayText.length() - 1);
            return jsonArrayText;
        }
    }
}
