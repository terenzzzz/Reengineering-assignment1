package dependenceAnalysis.intraprocedural;

import dependenceAnalysis.util.Graph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CFGExtractor {

	/**
	 * Builds the control flow graph for mn.
	 * @param owner
	 * @param mn
	 * @return
	 * @throws AnalyzerException
     */
	public static Graph getCFG(String owner, MethodNode mn)throws AnalyzerException {
		final Graph<AbstractInsnNode> g = buildGraph(owner, mn);
		InsnNode entry = new InsnNode(0);
		InsnNode exit = new InsnNode(0);

		g.addNode(entry);
		g.addNode(exit);
		for(AbstractInsnNode n: g.getNodes()){
			if(n.equals(entry) || n.equals(exit))
				continue;
			if(g.getSuccessors(n).isEmpty())
				g.addEdge(n, exit);
			if(g.getPredecessors(n).isEmpty())
				g.addEdge(entry, n);
		}
	return g;
	}

	protected static Graph<AbstractInsnNode> buildGraph(String owner,
			MethodNode mn) throws AnalyzerException {
		final InsnList instructions = mn.instructions;
		final Graph g = new Graph<AbstractInsnNode>();
		Analyzer a =new Analyzer(new BasicInterpreter()) {
			
			
			protected void newControlFlowEdge(int src, int dst) {
				AbstractInsnNode from = instructions.get(src);
                AbstractInsnNode to = instructions.get(dst);
                g.addNode(from);
                g.addNode(to);
                g.addEdge(from, to);
			}
		};
		
		a.analyze(owner, mn);
		
		return g;
	}
	
	public static void main(String[] args) throws IOException{
		ClassNode cn = new ClassNode(Opcodes.ASM4);
        InputStream in=CFGExtractor.class.getResourceAsStream("/org/apache/commons/compress/archivers/ArchiveInputStream.class");
        ClassReader classReader=new ClassReader(in);
        classReader.accept(cn, 0);
        for(MethodNode mn : (List<MethodNode>)cn.methods){
        	try {
        		System.out.println("================CFG FOR: "+cn.name+"."+mn.name+mn.desc+" =================");
        		System.out.println(CFGExtractor.getCFG(cn.name, mn));
			} catch (AnalyzerException e) {
				e.printStackTrace();
			}
        }
	}
}
