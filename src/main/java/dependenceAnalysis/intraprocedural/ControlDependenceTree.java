package dependenceAnalysis.intraprocedural;


import dependenceAnalysis.util.Graph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


/**
 * Created by neilwalkinshaw on 19/10/2017.
 */
public class ControlDependenceTree extends Analysis {

    public ControlDependenceTree(ClassNode cn, MethodNode mn) {
        super(cn, mn);
    }

    /**
     * Return a graph representing the control dependence tree of the control
     * flow graph, which is stored in the controlFlowGraph class attribute
     * (this is inherited from the Analysis class).
     *
     * You may wish to use the post dominator tree code you implement to support
     * computing the Control Dependence Graph.
     *
     * @return
     */
    public Graph computeResult() {
        Graph<AbstractInsnNode> controlDependenceTree = new Graph();
        Graph<AbstractInsnNode> augmentedGraph = createAugmentedGraph();
        Graph<AbstractInsnNode> postdomtree = computePostDomTree();
        try{
            //Compute dominance tree for CFG.

            Collection<AbstractInsnNode> done = new HashSet<AbstractInsnNode>();

            AbstractInsnNode startNode = augmentedGraph.getEntry();

            for(AbstractInsnNode cfgNode : augmentedGraph.getNodes()){
                for(AbstractInsnNode successor : augmentedGraph.getSuccessors(cfgNode)){
                    //Check whether link between cfgNode and Successor is decision.
                    //Could also have done this by checking number of successors of cfgNode.
                    if(!(augmentedGraph.getSuccessors(cfgNode).size()>1))
                        continue;
                    //Check whether successor post-dominates cfgNode
                    if(postdomtree.getTransitiveSuccessors(successor).contains(cfgNode))
                        continue;
                    assert(!cfgNode.equals(successor));

                    if(cfgNode.equals(startNode))
                        continue;
                    //Get least common ancestor of cfgnode and successor
                    AbstractInsnNode leastCommonAncestor = postdomtree.getLeastCommonAncestor(cfgNode,successor);
                    Collection<AbstractInsnNode> path = new HashSet<AbstractInsnNode>();
                    //Include cfgNode if cfgNode is least common ancestor.
                    if(leastCommonAncestor.equals(cfgNode)){
                        path.add(cfgNode);
                        path.addAll(getNodesOnPath(postdomtree,leastCommonAncestor,successor));
                    }
                    //Do not include cfgNode if cfgNode is not least common ancestor.
                    else{
                        path.addAll(getNodesOnPath(postdomtree,leastCommonAncestor,successor));
                        path.remove(cfgNode);
                    }
                    //Add control dependence edges between cfgNode and every node on path to successor.
                    for(AbstractInsnNode target : path){
                        controlDependenceTree.addNode(cfgNode);
                        controlDependenceTree.addNode(target);
                        controlDependenceTree.addEdge(cfgNode, target);
                        done.add(target);
                    }
                }
            }

            //For any node that is not already control-dependent upon another node, add control dependence from Entry node.
            AbstractInsnNode entry = controlFlowGraph.getEntry();
            controlDependenceTree.addNode(entry);
            done.add(entry);
            for(AbstractInsnNode n : controlFlowGraph.getNodes()){
                if(!done.contains(n)) {
                    controlDependenceTree.addNode(n);
                    controlDependenceTree.addEdge(entry, n);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return controlDependenceTree;
    }

    private Graph<AbstractInsnNode> computePostDomTree() {
        PostDominatorTree dtg = new PostDominatorTree(cn,mn);
        Graph<AbstractInsnNode> reversed = copyControlFlowGraph();
        reversed = dtg.reverseGraph(reversed);
        Graph<AbstractInsnNode> pdt = dtg.computePostDominanceTree(reversed);
        return pdt;
    }


    private Graph<AbstractInsnNode> createAugmentedGraph() {
        Graph<AbstractInsnNode> augmentedGraph = copyControlFlowGraph();
        addStartNode(augmentedGraph);
        return augmentedGraph;
    }

    private Graph<AbstractInsnNode> copyControlFlowGraph() {
        Graph<AbstractInsnNode> newGraph = new Graph();
        for(AbstractInsnNode n : controlFlowGraph.getNodes()){
            newGraph.addNode(n);
        }
        for(AbstractInsnNode m : controlFlowGraph.getNodes()){
            for(AbstractInsnNode n : controlFlowGraph.getSuccessors(m)){
                newGraph.addEdge(m,n);
            }
        }
        return newGraph;
    }

    private void addStartNode(Graph edges) {
        AbstractInsnNode start = new InsnNode(0);
        edges.addNode(start);
        edges.addEdge(start, edges.getEntry());
        edges.addEdge(start, edges.getExit());
    }

    private Collection<? extends AbstractInsnNode> getNodesOnPath(Graph<AbstractInsnNode> tree, AbstractInsnNode from,
                                                                  AbstractInsnNode to) {
        Collection<AbstractInsnNode> path = new HashSet<AbstractInsnNode>();
        AbstractInsnNode current = to;
        assert(tree.getTransitiveSuccessors(from).contains(to));
        while(!current.equals(from)){
            if(!current.equals(from))
                path.add(current);
            Collection<AbstractInsnNode> predecessors = tree.getPredecessors(current);
            if(predecessors.isEmpty()){
                System.err.println("Failed to find parent node for "+current);
                break;
            }
            //Because we are walking up a tree, we know that each node can only have a single predecessor.
            assert(predecessors.size()==1);
            current = predecessors.iterator().next(); //take what should be the only node in the set.
        }
        assert(!path.contains(from));
        return path;
    }

    public static void main(String[] args) throws IOException {
        ClassNode cn = new ClassNode(Opcodes.ASM4);
        InputStream in=CFGExtractor.class.getResourceAsStream("/org/apache/commons/compress/archivers/ArchiveInputStream.class");
        ClassReader classReader=new ClassReader(in);
        classReader.accept(cn, 0);
        for(MethodNode mn : (List<MethodNode>)cn.methods){
            try {
                System.out.println("================CDT FOR: "+cn.name+"."+mn.name+mn.desc+" =================");
                ControlDependenceTree cdt = new ControlDependenceTree(cn,mn);
                System.out.println(cdt.computeResult());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
