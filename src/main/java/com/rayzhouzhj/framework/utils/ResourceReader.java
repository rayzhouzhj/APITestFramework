package com.rayzhouzhj.framework.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ResourceReader {

    public static String readResource(String filePath) {
        StringBuilder builder = new StringBuilder();
        if (filePath != null) {
            try {
                File logFile = new File(filePath);
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return builder.toString();
    }
}
