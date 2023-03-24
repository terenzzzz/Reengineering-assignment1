package app;


import structuralAnalysis.EnhancedClassDiagram;


import java.io.File;
import java.io.IOException;

// Class Path:
// -cp
//$Classpath$;"C:\Users\zhang\OneDrive\Desktop\year3Source\COM3523 Software Reengineering\Assignment1\jfreechart-master\target\jfreechart-2.0.0-SNAPSHOT.jar"

// Args:
// "C:\Users\zhang\OneDrive\Desktop\year3Source\COM3523 Software Reengineering\Assignment1\jfreechart-master\target\classes"
// true
// true
// org.jfree.chart.renderer
// rendererEnhance.dot


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
