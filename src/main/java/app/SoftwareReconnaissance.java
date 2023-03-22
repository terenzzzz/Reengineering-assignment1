package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SoftwareReconnaissance {
    public static void main(String[] args) {
        Set<String> firstTrace = readFile(args[0]);
        Set<String> secondTrace = readFile(args[1]);
        Set<String> thirdTrace = readFile(args[2]);
        Set<String> fourthTrace = readFile(args[3]);

        Set<String> commonElements = new HashSet<>(firstTrace);
        commonElements.retainAll(secondTrace);
        commonElements.retainAll(thirdTrace);
        commonElements.retainAll(fourthTrace);

        System.out.println(commonElements); // 输出共有元素的Set


    }

    public static Set<String> readFile(String fileName){
        Set<String> logLines = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Add each line to the Set
                String[] parts  = line.split(",");
                String className = parts[parts.length - 1];
                logLines.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logLines;
    }




}
