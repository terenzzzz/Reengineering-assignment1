package structuralAnalysis;

import util.ReflectionClassReader;

import java.io.File;
import java.util.*;

public class EnhancedClassDiagram extends ClassDiagram {

    Map<String,Integer> numMethods, numMembers;


    /**
     * Instantiating the class will populate the inheritance and association relations.
     *
     * @param root
     * @param ignoreLibs
     * @param ignoreInnerClasses
     * @param signaturePrefix
     */
    public EnhancedClassDiagram(String root, boolean ignoreLibs, boolean ignoreInnerClasses, String signaturePrefix) {
        super(root, ignoreLibs, ignoreInnerClasses, signaturePrefix);

        numMembers = new HashMap<>();
        numMethods = new HashMap<>();

        File dir = new File(root);
        ReflectionClassReader rcr = new ReflectionClassReader();

        List<Class<?>> classes = rcr.processDirectory(dir,"");

        for(Class<?> cl : classes){
            numMembers.put(cl.getName(),cl.getDeclaredFields().length);

            numMethods.put(cl.getName(),cl.getMethods().length);
        }

    }

    public String toString(){
        StringBuffer dotGraph = new StringBuffer();
        dotGraph.append("digraph classDiagram{\n" +
                "graph [splines=ortho, rankdir=BT]\n\n");

        // 计算出所有类中拥有最多成员变量的类的成员变量数量
        int maxMembers = Collections.max(numMembers.values());
        // 对于每个在 includedClasses 中的类
        for(String className : includedClasses){
            int members = numMembers.get(className);
            // 节点的宽度由该类的成员变量数量决定
            double width = 1 + (members*.45) ;
            // 节点的高度由该类的方法数量决定
            double height = 1 + (numMethods.get(className)*.15) ;

            /*
            Nodes take a fill-colour in RGB format (where each R,G,B is given as a two-digit hexadecimal)
            The below code will calculate an RGB colour as a colour on a spectrum from green to red,
            where the top of the scale is determined by the largest class in terms of number of methods.
             */
            // 将红色的分量设为 255 乘以该类的成员变量数量再除以 maxMembers。
            String r = String.format("%02X", (255 * members) / maxMembers);
            //然后，将绿色的分量设为 255 减去红色分量。
            String  g = String.format("%02X", (255 * (maxMembers - members)) / maxMembers);
            //最后，将蓝色的分量设为 0。
            String b = String.format("%02X", 0);

            /*
            set width and height, fill colour to RGB computed above. The parameter fixedsize has to be true, otherwise the
            size of a node will automatically expand to fit the label (which would skew our visualisation).
             */
            // 将该代码添加到 dotGraph 中
            dotGraph.append("\""+className + "\"[shape = box, width="+width+", height="+height+", style=filled, fillcolor=\"#"+r+g+b+"\",fixedsize=true];\n");
        }

        //Add inheritance relations
        for(String childClass : inheritance.keySet()){
            if(includedClasses.contains(childClass) && includedClasses.contains(inheritance.get(childClass))) {
                String from = "\"" + childClass + "\"";
                String to = "\"" + inheritance.get(childClass) + "\"";
                dotGraph.append(from + " -> " + to + "[arrowhead = onormal, style=\"bold\", penwidth=10];\n");
            }
        }

        //Add associations
        for(String cls : associations.keySet()){
            if(!includedClasses.contains(cls))
                continue;
            Set<String> fields = associations.get(cls);
            for(String field : fields) {
                if(!includedClasses.contains(field))
                    continue;
                String from = "\""+cls +"\"";
                String to = "\""+field+"\"";
                dotGraph.append(from + " -> " +to + "[arrowhead = diamond,style=\"bold\", penwidth=10];\n");
            }
        }

        dotGraph.append("}");
        return dotGraph.toString();
    }
}
