package app;


import java.io.*;
import java.util.*;


public class SoftwareReconnaissance {
    public static void main(String[] args) throws IOException {
        // 1. 确定需要隔离的功能和特性
        //      BarChart
        // 2. 选择一个有关功能的测试用例
        //      BarChartTest
        // 3. 选择2-3个不使用该功能的测试用例
        //      ScatterPlotTest / KeyedObjectsTest / CategoryToPieDatasetTest
        // 4. 运行测试用例并收集轨迹
        // 5. 分析轨迹并提取特定类型的图表相关的源代码
        //   5.1 过滤掉与特定类型的图表不相关的代码
        //
        // 先收集两组测试用例的执行轨迹，
        // 一组包含特定功能的测试用例（记为P），
        // 一组不包含特定功能的测试用例（记为N），
        // 然后将N中的所有元素从P中减去，得到的就是与特定功能相关的代码。

        // BarChartTest.log
        HashMap<String,String> firstTrace = readFile(args[0]);
        // GanttChartTest.log
        HashMap<String,String> secondTrace = readFile(args[1]);
        // LineChartTest.log
        HashMap<String,String> thirdTrace = readFile(args[2]);
        // PieChartTest.log
        HashMap<String,String> fourthTrace = readFile(args[3]);

        // TODO: Subtract all of the elements in N from P.
        HashMap<String, String> relevant = subtractFeature(subtractFeature(subtractFeature(firstTrace,secondTrace),thirdTrace),fourthTrace);
        BufferedWriter cm = new BufferedWriter(new FileWriter("relevant_class_method.csv"));
        cm.write("RelevantClass,Method \n");

        // Write sorted data to .csv file
        for(String key: relevant.keySet()) {
            cm.write(key + "," + relevant.get(key) + "\n");

        }
        cm.flush();
        cm.close();



        // TODO: get package call Count
        HashMap<String, Integer> packageCount = packageCount(relevant);
        BufferedWriter pc = new BufferedWriter(new FileWriter("relevant_package_count.csv"));
        pc.write("Package,Count \n");

        // Write sorted data to .csv file
        for(String key: packageCount.keySet()) {
            pc.write(key + "," + packageCount.get(key) + "\n");
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
