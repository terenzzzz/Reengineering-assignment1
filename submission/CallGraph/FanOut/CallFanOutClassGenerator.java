package app;

import dependenceAnalysis.interprocedural.CallGraph;
import dependenceAnalysis.interprocedural.ClassCallGraph;
import dependenceAnalysis.interprocedural.RestrictedCallGraph;
import dependenceAnalysis.util.Signature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// class path:
// -cp
// $Classpath$;"C:\Users\zhang\OneDrive\Desktop\jfreechart\target\jfreechart-2.0.0-SNAPSHOT.jar"

// arg:
// "C:\Users\zhang\OneDrive\Desktop\jfreechart\target\classes"
public class CallFanOutClassGenerator {

    public static void main(String[] args) throws IOException{

        // build graph
        CallGraph cg = new CallGraph(args[0]);
        ClassCallGraph ccg = new ClassCallGraph(args[0]);

        // get call data
        Map<String, Integer> cfo = classFanOut(ccg);
        Map<String, Integer> mfo = methodFanOut(cg);

        BufferedWriter bw = new BufferedWriter(new FileWriter("call_fan_out_class_sorted.csv"));
        bw.write("Class,ClassFanOut,MethodFanOut\n");

        // read data to list
        List<String[]> data = new ArrayList<>();
        for (String key : cfo.keySet()) {
            int classFanIn = cfo.get(key);
            int methodFanIn = mfo.get(key);
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

    private static Map<String, Integer> methodFanOut(CallGraph cg) throws IOException {
        Map<String, Integer> mfo = new HashMap<String, Integer>();

        for(Signature sig : cg.getCallGraph().getNodes()) {
            String[] parts = sig.toString().split("\\.");
            String left = parts[0];

            // Get the call count
            int incoming = cg.getCallGraph().getSuccessors(sig).size();

            // Store className and callCount into a Hashmap
            if (mfo.containsKey(left)){
                Integer origin = mfo.get(left);
                mfo.put(left,(origin+incoming));
            }else {
                mfo.put(left,incoming);
            }
        }
        return mfo;
    }

    private static Map<String, Integer> classFanOut(ClassCallGraph ccg) throws IOException {
        Map<String, Integer> cfo = new HashMap<String, Integer>();

        for(String node : ccg.getClassCG().getNodes()) {
            // Get the class count
            int incoming = ccg.getClassCG().getSuccessors(node).size();

            // Store className and callCount into a Hashmap
            cfo.put(node,incoming);
        }
        return cfo;
    }

}
