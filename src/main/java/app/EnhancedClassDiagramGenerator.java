package app;


import structuralAnalysis.EnhancedClassDiagram;


import java.io.File;
import java.io.IOException;

public class EnhancedClassDiagramGenerator {
    public static void main(String[] args) throws IOException {
        String root = args[0];
        Boolean ignoreLibs = Boolean.parseBoolean(args[1]);
        Boolean ignoreInnerClasses = Boolean.parseBoolean(args[2]);
        String signaturePrefix = args[3];
        String output = args[4];
        EnhancedClassDiagram cd = new EnhancedClassDiagram(root, ignoreLibs, ignoreInnerClasses, signaturePrefix);
        cd.writeDot(new File(output));
    }
}
