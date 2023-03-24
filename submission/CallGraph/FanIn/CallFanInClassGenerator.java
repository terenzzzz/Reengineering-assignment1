package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import dependenceAnalysis.interprocedural.CallGraph;
import dependenceAnalysis.interprocedural.ClassCallGraph;
import dependenceAnalysis.interprocedural.RestrictedCallGraph;
import dependenceAnalysis.util.Signature;


// class path:
// -cp
// $Classpath$;"C:\Users\zhang\OneDrive\Desktop\jfreechart\target\jfreechart-2.0.0-SNAPSHOT.jar"

// arg:
// "C:\Users\zhang\OneDrive\Desktop\jfreechart\target\classes"

public class CallFanInClassGenerator {

    public static void main(String[] args) throws IOException{
        CallGraph cg = null;
        ClassCallGraph ccg = null;

        // build graph
        cg = new CallGraph(args[0]);
        ccg = new ClassCallGraph(args[0]);

        // get call data
        Map<String, Integer> cfi = classFanIn(ccg);
        Map<String, Integer> mfi = methodFanIn(cg);

        BufferedWriter bw = new BufferedWriter(new FileWriter("call_fan_in_class_sorted.csv"));
        bw.write("Class,ClassFanIn,MethodFanIn\n");

        // read data to list
        List<String[]> data = new ArrayList<>();
        for (String key : cfi.keySet()) {
            int classFanIn = cfi.get(key);
            int methodFanIn = mfi.get(key);
            data.add(new String[]{key, Integer.toString(classFanIn), Integer.toString(methodFanIn)});
        }

        // using compare to sort
        Collections.sort(data, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return Integer.compare(Integer.parseInt(o2[1]), Integer.parseInt(o1[1]));
            }
        });

        // Write sorted data to .csv file
        for (String[] row : data) {
            bw.write(row[0] + "," + row[1] + "," + row[2] + "\n");
        }

        bw.flush();
        bw.close();

    }

    private static Map<String, Integer> classFanIn(ClassCallGraph ccg) throws IOException {
        Map<String, Integer> cfi = new HashMap<String, Integer>();

        for(String node : ccg.getClassCG().getNodes()) {
            // Get the class count
            int incoming = ccg.getClassCG().getPredecessors(node).size();

            // Store className and callCount into a Hashmap
            cfi.put(node,incoming);
        }

        return cfi;
    }

    private static Map<String, Integer> methodFanIn(CallGraph cg) throws IOException {
        Map<String, Integer> mti = new HashMap<String, Integer>();

        for(Signature sig : cg.getCallGraph().getNodes()) {
            String[] parts = sig.toString().split("\\.");
            String left = parts[0];

            // Get the call count
            int incoming = cg.getCallGraph().getPredecessors(sig).size();

            // Store className and callCount into a Hashmap
            if (mti.containsKey(left)){
                Integer origin = mti.get(left);
                mti.put(left,(origin+incoming));
            }else {
                mti.put(left,incoming);
            }
        }
        return mti;
    }
}
