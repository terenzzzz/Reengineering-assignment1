package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dependenceAnalysis.interprocedural.CallGraph;
import dependenceAnalysis.interprocedural.RestrictedCallGraph;
import dependenceAnalysis.util.Signature;

public class CallFanOutGenerator {

    public static void main(String[] args) throws IOException{
        CallGraph cg = null;
        if(args.length>1){
            cg = new RestrictedCallGraph(args[0],args[1]);
        }else {
            cg = new CallGraph(args[0]);
        }
        writeFanOut(cg);
    }

    private static void writeFanOut(CallGraph cg) throws IOException {
        String toReturn = "Class, Method, Number of uses\n";
        for(Signature sig : cg.getCallGraph().getNodes()) {
            String[] parts = sig.toString().split("\\.");
            String left = parts[0];
            String right = parts[1];
            // 调用另一个类或另一个类的方法的数量
            int outGoing = cg.getCallGraph().getSuccessors(sig).size();
            if (outGoing > 0) {
                toReturn += left + ", " + right + ", " + outGoing + "\n";
            }
        }
        // 对数据进行排序
        List<String[]> lines = new ArrayList<>();
        for (String line : toReturn.split("\n")) {
            lines.add(line.split(","));
        }
        Collections.sort(lines, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                // 按第一列的值升序排序
                int cmp = o1[0].trim().compareTo(o2[0].trim());
                if (cmp != 0) {
                    return cmp;
                }
                // 如果第一列值相同，按第三列的值降序排序
                return Integer.compare(Integer.parseInt(o2[2].trim()), Integer.parseInt(o1[2].trim()));
            }
        });

        // 将排序后的数据写入CSV文件
        BufferedWriter bw = new BufferedWriter(new FileWriter("call_fan_out_sorted.csv"));
        for (String[] fields : lines) {
            bw.write(String.join(",", fields));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }


}
