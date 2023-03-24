package structuralAnalysis;

import util.ReflectionClassReader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class EnhancedClassDiagram extends ClassDiagram {

    Map<String,Integer> numMethods, numMembers;
    Map<String,String> classType;


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

        classType = new HashMap<>();


        File dir = new File(root);
        ReflectionClassReader rcr = new ReflectionClassReader();

        List<Class<?>> classes = rcr.processDirectory(dir,"");

        for(Class<?> cl : classes){
            numMembers.put(cl.getName(),cl.getDeclaredFields().length);

            numMethods.put(cl.getName(),cl.getMethods().length);

            if (cl.isInterface()){
                classType.put(cl.getName(),"Interface");
            } else if (cl.isEnum()) {
                classType.put(cl.getName(),"Enum");
            } else if (Modifier.isAbstract(cl.getModifiers())) {
                classType.put(cl.getName(),"Abstract");
            }else{
                // cl.isMemberClass();
                classType.put(cl.getName(),"Class");
            }
        }
    }

    public String toString(){
        StringBuffer dotGraph = new StringBuffer();
        dotGraph.append("digraph classDiagram{\n" +
                "graph [splines=ortho, rankdir=BT]\n\n");

        int maxMembers = Collections.max(numMembers.values());
        for(String className : includedClasses){
            int members = numMembers.get(className);
            double width = 1 + (members*.65) ;
            double height = 1 + (numMethods.get(className)*.15) ;

            /*
            Nodes take a fill-colour in RGB format (where each R,G,B is given as a two-digit hexadecimal)
            The below code will calculate an RGB colour as a colour on a spectrum from green to red,
            where the top of the scale is determined by the largest class in terms of number of methods.
             */
            String r = String.format("%02X", (255 * members) / maxMembers);
            String  g = String.format("%02X", (255 * (maxMembers - members)) / maxMembers);
            String b = String.format("%02X", 0);

            /*
            set width and height, fill colour to RGB computed above. The parameter fixedsize has to be true, otherwise the
            size of a node will automatically expand to fit the label (which would skew our visualisation).
             */

            // Interface: diamond , Abstract: circle , Class: box, Enum: oval
            String type = classType.get(className);
            String shape = "";
            if (type == "Interface"){
                shape = "diamond";
            } else if (type == "Enum") {
                shape = "triangle";
            } else if (type == "Abstract") {
                shape = "circle";
            } else {
                shape = "box";
            }

            dotGraph.append("\""+className + "\"[shape = "+ shape+", width="+width+", height="+height+", style=filled, fillcolor=\"#"+r+g+b+"\",fixedsize=true];\n");
        }

        //Add inheritance relations
        for(String childClass : inheritance.keySet()){
            if(includedClasses.contains(childClass) && includedClasses.contains(inheritance.get(childClass))) {
                String from = "\"" + childClass + "\"";
                String to = "\"" + inheritance.get(childClass) + "\"";
                Integer inheritanceDepth = calcDepth(childClass);
                String penwidth = Integer.toString((1 + 4*inheritanceDepth));
                dotGraph.append(from + " -> " + to + "[arrowhead = onormal, penwidth="+penwidth+", color=red];\n");
            }
        }

        //Add associations 
        System.out.println(associations);
        for(String cls : associations.keySet()){
            if(!includedClasses.contains(cls))
                continue;
            Set<String> fields = associations.get(cls);
            for(String field : fields) {
                if(!includedClasses.contains(field))
                    continue;
                String from = "\""+cls +"\"";
                String to = "\""+field+"\"";
                dotGraph.append(from + " -> " +to + "[arrowhead = diamond,penwidth=10, color=blue];\n");
            }
        }

        dotGraph.append("}");
        return dotGraph.toString();
    }

    public Integer calcDepth(String childClass){
        Integer counter = 0;
        while (includedClasses.contains(childClass) && includedClasses.contains(inheritance.get(childClass))) {
            counter ++;
            childClass = inheritance.get(childClass);
        }
        return counter;
    }

}
