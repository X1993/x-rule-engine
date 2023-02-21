package com.github.xengine.core;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author X1993
 * @date 2023/2/17
 * @description
 */
public class XNodeUtils {

    /**
     * 起始节点检验
     * @param startNode
     * @param <CONTENT>
     */
    public static <CONTENT extends XRuleContent> void validationStartNode(XNode<CONTENT> startNode){
        if (!startNode.isStartNode()){
            throw new XNodeException("起始节点不能有前置节点");
        }
        List<XNode<CONTENT>> nodes = startNode.getReachableNodes(false ,true);
        if (nodes.stream().filter(XNode::isStartNode).count() != 1){
            throw new XNodeException("起始节点有且只能有一个");
        }
        if (nodes.stream().filter(XNode::isTerminationNode).count() != 1){
            throw new XNodeException("终止节点有且只能有一个");
        }
        validationCycle(nodes);
    }

    /**
     * 校验是否有环
     * @return
     */
    public static void validationCycle(List<? extends XNode> nodes)
    {
        Set<XNode> unTraversedVertexSet = new HashSet<>(nodes);

        XNode currentVertex = null;
        //dfs,用循环模拟递归
        while ((currentVertex = unTraversedVertexSet.stream().findAny().orElse(null)) != null)
        {
            Stack<List<XNode>> methodStack = new Stack<>();

            List<XNode> startEntry = new ArrayList<>();
            startEntry.add(currentVertex);
            methodStack.add(startEntry);

            //模拟方法调用链，检测环
            LinkedHashSet<XNode> traversingVertexSet = new LinkedHashSet<>();
            Stack<XNode> traversingVertexStack = new Stack<>();

            while (!methodStack.isEmpty())
            {
                List<XNode> topEntry = methodStack.peek();
                if (topEntry.isEmpty()){
                    //模拟方法返回
                    methodStack.pop();
                    if (traversingVertexStack.isEmpty()){
                        continue;
                    }
                    XNode popVertex = traversingVertexStack.pop();
                    if (popVertex != null){
                        traversingVertexSet.remove(popVertex);
                    }
                }else {
                    XNode popVertex = topEntry.remove(0);
                    if (traversingVertexSet.contains(popVertex)){
                        List<XNode> cycleNode = new ArrayList<>();
                        for (XNode xNode : traversingVertexSet) {
                            if (xNode.equals(popVertex) || !cycleNode.isEmpty()){
                                cycleNode.add(xNode);
                            }
                        }
                        throw new XNodeException(MessageFormat.format("规则链{0}构成了环" ,
                                cycleNode.stream()
                                        .map(x -> "【" + x.getRule().name() + "】")
                                        .collect(Collectors.joining("-"))));
                    }
                    //剪枝
                    if (unTraversedVertexSet.remove(popVertex))
                    {
                        List<XNode> nextEntry = new ArrayList<>(popVertex.getPostNodes());

                        //模拟方法调用
                        methodStack.add(nextEntry);
                        traversingVertexSet.add(popVertex);
                        traversingVertexStack.push(popVertex);
                    }
                }
            }
        }
    }

    public static void validationCycle(XNode xNode){
        validationCycle(xNode.getReachableNodes(true ,true));
    }

    /**
     * 构建并行规则流
     * @param xNodes
     * @param <CONTENT>
     * @return
     */
    public static <CONTENT extends XRuleContent> XNode<CONTENT> buildParallel(XNode<CONTENT> ... xNodes)
    {
        XNode<CONTENT> startXNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<CONTENT> endXNode = new XNode<>(new EmptyXRule<>("END"));
        startXNode.addPostNode(xNodes);
        endXNode.addPreNode(xNodes);
        return startXNode;
    }

    /**
     * 构建串行规则流
     * @param xNodes
     * @param <CONTENT>
     * @return
     */
    public static <CONTENT extends XRuleContent> XNode<CONTENT> buildSerial(XNode<CONTENT> ... xNodes)
    {
        XNode<CONTENT> startXNode = new XNode<>(new EmptyXRule<>("START"));
        XNode<CONTENT> endXNode = new XNode<>(new EmptyXRule<>("END"));
        XNode<CONTENT> beforeNode = startXNode;
        for (XNode<CONTENT> xNode : xNodes) {
            beforeNode.addPostNode(xNode);
            beforeNode = xNode;
        }
        beforeNode.addPostNode(endXNode);
        return startXNode;
    }

}
