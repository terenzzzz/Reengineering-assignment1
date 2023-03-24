package app;


import java.io.*;
import java.util.*;


// Args:
// "C:\Users\zhang\OneDrive\Desktop\BarChartTest.log"
// "C:\Users\zhang\OneDrive\Desktop\ScatterPlotTest.log"
//  "C:\Users\zhang\OneDrive\Desktop\KeyedObjectsTest.log"
//  "C:\Users\zhang\OneDrive\Desktop\CategoryToPieDatasetTest.log"

public class SoftwareReconnaissance {
    public static void main(String[] args) throws IOException {
        // 1. Determine the functions and features that need to be isolated
        //      BarChart
        // 2. Select a test case about a function
        //      BarChartTest.java
        // 3. Select 2-3 test cases that do not use the feature
        //      ScatterPlotTest.java / KeyedObjectsTest.java / CategoryToPieDatasetTest.java
        // 4. Run test cases and collect traces
        // 5. Analyze tracks and extract source code related to specific types of charts
        //   5.1 Filter out code that is not relevant to a specific type of chart
        //
        // The execution traces of two sets of test cases are collected first.
        // a set of test cases containing a specific function (denoted as P), and
        // a set of test cases that do not contain a specific function (denoted as N).
        // Then all the elements in N are subtracted from P to obtain the code related to the specific function.

        // BarChartTest.log
        HashMap<String,String> firstTrace = readFile(args[0]);
        // GanttChartTest.log
        HashMap<String,String> secondTrace = readFile(args[1]);
        // LineChartTest.log
        HashMap<String,String> thirdTrace = readFile(args[2]);
        // PieChartTest.log
        HashMap<String,String> fourthTrace = readFile(args[3]);

        // Subtract all of the elements in N from P.
        HashMap<String, String> relevant = subtractFeature(subtractFeature(subtractFeature(firstTrace,secondTrace),thirdTrace),fourthTrace);
        List<String> sortedKeys = new ArrayList<>(relevant.keySet());
        Collections.sort(sortedKeys);

        BufferedWriter cm = new BufferedWriter(new FileWriter("relevant_class_method.csv"));
        cm.write("Package,Class,Method \n");

        // Write sorted data to .csv file
        for(String key: sortedKeys) {
            String packageName = key;
            String className = key;
            int lastDotIndex = key.lastIndexOf(".");
            if (lastDotIndex != -1) {
                packageName = key.substring(0, lastDotIndex);
                className = key.substring(lastDotIndex+1, key.length());
            }
            cm.write(packageName + "," + className + "," + relevant.get(key) + "\n");
        }
        cm.flush();
        cm.close();



        // Get package call Count
        HashMap<String, Integer> packageCount = packageCount(relevant);
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(packageCount.entrySet());
        Collections.sort(sortedData, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        BufferedWriter pc = new BufferedWriter(new FileWriter("relevant_package_count.csv"));
        pc.write("Package,MethodCount \n");

        // Write sorted data to .csv file
        for(Map.Entry<String, Integer> entry: sortedData) {
            pc.write(entry.getKey() + "," + entry.getValue() + "\n");
        }
        pc.flush();
        pc.close();

    }

    public static HashMap<String, String> subtractFeature(HashMap<String, String> p,HashMap<String, String> n) {
        HashMap<String, String> removedIntersection = new HashMap<>();

        for(String key: p.keySet()) {
            if (n.containsKey(key) && n.get(key).equals(p.get(key))){
            }else {
                removedIntersection.put(key,p.get(key));
            }
        }
        return removedIntersection;
    };


    public static HashMap<String, Integer> packageCount(HashMap<String, String> p) {
        HashMap<String, Integer> packageCount = new HashMap<>();

        for(String key: p.keySet()) {
            int index = key.lastIndexOf(".");
            if (index != -1) {
                String result = key.substring(0, index);
                if (packageCount.containsKey(result)){
                    packageCount.put(result , packageCount.get(result) + 1);
                }else {
                    packageCount.put(result,1);
                }
            } else {
                System.out.println(key);
            }

        }
        return packageCount;
    };


    public static HashMap<String, String> readFile(String fileName){
        HashMap<String,String> trace = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts  = line.split(",");
                String call = parts[parts.length-1];

                String[] callParts  = call.split(":");

                if (callParts.length == 2 ){
                    String className = callParts[0];
                    String method = callParts[1];
                    trace.put(className,method);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trace;
    }

}
