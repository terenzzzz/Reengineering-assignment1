package dependenceAnalysis.intraprocedural;

import dependenceAnalysis.util.Graph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

/**
 * Created by neilwalkinshaw on 19/10/2017.
 */
public class PostDominatorTree extends Analysis {

    public PostDominatorTree(ClassNode cn, MethodNode mn) {
        super(cn, mn);
    }

    /**
     * Return a graph representing the post-dominator tree of the control
     * flow graph, which is stored in the controlFlowGraph class attribute
     * (this is inherited from the Analysis class).
     * @return
     */
    public Graph computeResult() {
        Graph cfg = reverseGraph(controlFlowGraph);
        Graph dominanceTree = computePostDominanceTree(cfg);
        return dominanceTree;
    }

    protected Graph computePostDominanceTree(Graph<AbstractInsnNode> cfg) {
        Map<AbstractInsnNode,Collection<AbstractInsnNode>> pDom = calculatePostDominance(cfg,new HashMap<AbstractInsnNode,Collection<AbstractInsnNode>>());

        Graph dominanceTree = new Graph();
        dominanceTree.addNode(cfg.getEntry());
        Map<AbstractInsnNode,Collection<AbstractInsnNode>> mapCopy = new HashMap<AbstractInsnNode,Collection<AbstractInsnNode>>();
        mapCopy.putAll(pDom);
        Iterator<AbstractInsnNode> keyIt = mapCopy.keySet().iterator();
        while(keyIt.hasNext()){
            AbstractInsnNode next = keyIt.next();
            mapCopy.get(next).remove(next);
        }
        Queue<AbstractInsnNode> nodeQueue = new LinkedList<AbstractInsnNode>();
        nodeQueue.add(cfg.getEntry());
        while(!nodeQueue.isEmpty()) {
            AbstractInsnNode m = nodeQueue.remove();
            Iterator<AbstractInsnNode> nodeIterator = mapCopy.keySet().iterator();
            while (nodeIterator.hasNext()) {
                AbstractInsnNode n = nodeIterator.next();
                Collection<AbstractInsnNode> doms = mapCopy.get(n);
                if (doms.contains(m)) {
                    doms.remove(m);
                    if (doms.isEmpty()) {
                        dominanceTree.addNode(n);
                        dominanceTree.addEdge(m, n);
                        nodeQueue.add(n);
                    }
                }

            }
        }
        return dominanceTree;
    }

    /**
     * The dominance computation function.
     *
     * @param map
     * @return
     */
    private Map<AbstractInsnNode, Collection<AbstractInsnNode>> calculatePostDominance(Graph<AbstractInsnNode> cfg, Map<AbstractInsnNode, Collection<AbstractInsnNode>> map){
        AbstractInsnNode entry = cfg.getEntry();
        HashSet<AbstractInsnNode> entryDom = new HashSet<AbstractInsnNode>();
        entryDom.add(entry);
        map.put(entry, entryDom);
        for(AbstractInsnNode n: cfg.getNodes()){
            if(n.equals(entry))
                continue;
            HashSet<AbstractInsnNode> allNodes = new HashSet<AbstractInsnNode>();
            allNodes.addAll(cfg.getNodes());
            map.put(n, allNodes);
        }
        boolean changed = true;
        while(changed){
            changed = false;
            for(AbstractInsnNode n: cfg.getNodes()){
                if(n.equals(entry))
                    continue;
                Collection<AbstractInsnNode> currentDominators = map.get(n);
                Collection<AbstractInsnNode> newDominators = calculateDominators(cfg, map,n);

                if(!currentDominators.equals(newDominators)){
                    changed = true;
                    map.put(n, newDominators);
                    break;
                }
            }
        }
        return map;
    }

    /**
     * Computes the intersection for a given set of sets of nodes (representing
     * the sets of dominators).
     * @param cfg
     * @param dominate
     * @param n
     * @return
     */
    private static Set<AbstractInsnNode> calculateDominators(Graph<AbstractInsnNode> cfg, Map<AbstractInsnNode,Collection<AbstractInsnNode>> dominate, AbstractInsnNode n) {
        Set<AbstractInsnNode> doms = new HashSet<AbstractInsnNode>();
        doms.add(n);
        Iterator<AbstractInsnNode> predIt = cfg.getPredecessors(n).iterator();
        Set<AbstractInsnNode> intersection = new HashSet<AbstractInsnNode>();
        if(!predIt.hasNext())
            return new HashSet<AbstractInsnNode>();
        boolean firstTime = true;
        while(predIt.hasNext()){
            AbstractInsnNode pred = predIt.next();
            Collection<AbstractInsnNode> pDoms = dominate.get(pred);
            if(firstTime){
                intersection.addAll(pDoms);
                firstTime = false;
            }
            else{
                intersection.retainAll(pDoms);
            }
        }
        intersection.addAll(doms);
        return intersection;
    }

        /**
         * Produce a new Graph object, representing the reverse of the
         * Graph given in the cfg parameter.
         * @param cfg
         * @return
         */
        protected Graph<AbstractInsnNode> reverseGraph(Graph<AbstractInsnNode> cfg){
            Graph reverseCFG = new Graph();
            Iterator<AbstractInsnNode> cfgIt = cfg.getNodes().iterator();
            while(cfgIt.hasNext()){
                reverseCFG.addNode(cfgIt.next());
            }
            cfgIt = cfg.getNodes().iterator();
            while(cfgIt.hasNext()){
                AbstractInsnNode n = cfgIt.next();
                Set<AbstractInsnNode> successors = cfg.getSuccessors(n);
                for (AbstractInsnNode succ : successors) {
                    reverseCFG.addEdge(succ, n);
                }
            }
            return reverseCFG;
        }
}
