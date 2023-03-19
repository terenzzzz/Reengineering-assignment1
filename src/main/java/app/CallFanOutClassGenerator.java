package app;

import dependenceAnalysis.interprocedural.CallGraph;
import dependenceAnalysis.interprocedural.RestrictedCallGraph;
import dependenceAnalysis.util.Signature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CallFanOutClassGenerator {

    public static void main(String[] args) throws IOException{
        CallGraph cg = null;
        if(args.length>1){
            cg = new RestrictedCallGraph(args[0],args[1]);
        }else {
            cg = new CallGraph(args[0]);
        }
        writeFanIn(cg);
//        System.out.println(cg);
    }

    private static void writeFanIn(CallGraph cg) throws IOException {
        Map<String, Integer> myDict = new HashMap<String, Integer>();

        String toReturn = "";

        for(Signature sig : cg.getCallGraph().getNodes()) {
            String[] parts = sig.toString().split("\\.");
            String left = parts[0];

            // Get the call count
            int outGoing = cg.getCallGraph().getSuccessors(sig).size();

            // Store className and callCount into a Hashmap
            if (myDict.containsKey(left)){
                Integer origin = myDict.get(left);
                myDict.put(left,(origin+outGoing));
            }else {
                myDict.put(left,outGoing);
            }
        }

        // Iterator hash map to out put the data
        for (String key : myDict.keySet()) {
            int value = myDict.get(key);
            toReturn += key + ", " + value + "\n";
        }

        sorter(toReturn);

    }

    private static void sorter(String toReturn) throws IOException {
        // Convert hashmap to a List
        List<String[]> lines = new ArrayList<>();

        for (String line : toReturn.split("\n")) {

            lines.add(line.split(","));
        }
        // Sort by callCount
        Collections.sort(lines, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int num1 = Integer.parseInt(o1[1].trim());
                int num2 = Integer.parseInt(o2[1].trim());
                return Integer.compare(num2, num1);
            }
        });

        // Write data to csv file
        BufferedWriter bw = new BufferedWriter(new FileWriter("call_fan_out_class_sorted.csv"));
        bw.write("Class, Fan Out\n");
        for (String[] fields : lines) {
            bw.write(String.join(",", fields));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }


}
