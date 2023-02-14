package com.github.xengine.core;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 规则节点
 * @author wangjj7
 * @date 2023/2/10
 * @description
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XNode<CONTENT extends XRuleContent<CONTENT>> {

    /**
     * 规则
     */
    final XRule<CONTENT> rule;

    /**
     * 前置节点
     */
    final Set<XNode<CONTENT>> preNodes = new HashSet<>();

    /**
     * 后置节点
     */
    final Set<XNode<CONTENT>> postNodes = new HashSet<>();

    /**
     * 节点上下文
     * 由于节点的执行支持并行，非幂等操作需要确保线程安全
     */
    final XNodeContent<CONTENT> nodeContent = new XNodeContent<CONTENT>().setRuleStatus(XRuleStatus.WAIT);

    public XNode(XRule<CONTENT> rule) {
        this.rule = rule;
    }

    /**
     * 添加前置节点
     * @param preNodes
     */
    public XNode<CONTENT> addPreNode(XNode<CONTENT> ... preNodes){
        for (XNode<CONTENT> preNode : preNodes) {
            if (this.getPreNodes().add(preNode)){
                preNode.getPostNodes().add(this);
            }
        }
        return this;
    }

    /**
     * 添加后置节点
     * @param postNodes
     */
    public XNode<CONTENT> addPostNode(XNode<CONTENT> ... postNodes){
        for (XNode<CONTENT> postNode : postNodes) {
            if (this.getPostNodes().add(postNode)){
                postNode.getPreNodes().add(this);
            }
        }
        return this;
    }

    /**
     * 获取规则状态
     * @return
     */
    public XRuleStatus getRuleStatus(){
        return nodeContent.getRuleStatus();
    }

    /**
     * 获取规则输入上下文
     * @return
     */
    public CONTENT getInputRuleContent(){
        return nodeContent.getInputRuleContent();
    }

    /**
     * 获取规则输出上下文
     * @return
     */
    public CONTENT getOutputRuleContent(){
        return nodeContent.getOutputRuleContent();
    }

    /**
     * 合并前置节点的规则输出的上下文
     * @param preOutputRuleContent
     * @return
     */
    public CONTENT mergePreOutput(CONTENT preOutputRuleContent){
        if (preOutputRuleContent == null){
            throw new IllegalArgumentException("规则上下文不能为空");
        }
        synchronized (nodeContent) {
            CONTENT currentInputRuleContent = getInputRuleContent();
            if (currentInputRuleContent == null){
                nodeContent.setInputRuleContent(preOutputRuleContent.create());
            }
            if (currentInputRuleContent == preOutputRuleContent){
                return currentInputRuleContent;
            }
            currentInputRuleContent = getInputRuleContent();
            CONTENT mergeInputRuleContent = currentInputRuleContent.merge(preOutputRuleContent);
            nodeContent.setInputRuleContent(mergeInputRuleContent);
            return mergeInputRuleContent;
        }
    }

    /**
     * 规则就绪
     * @return
     */
    public boolean ready()
    {
        synchronized (nodeContent) {
            if (getRuleStatus() == XRuleStatus.WAIT) {
                nodeContent.setRuleStatus(XRuleStatus.READY);
                return true;
            }
            return false;
        }
    }

    /**
     * 规则开始执行
     * @return
     */
    public boolean executing()
    {
        synchronized (nodeContent) {
            XRuleStatus ruleStatus = getRuleStatus();
            if (ruleStatus == XRuleStatus.WAIT || ruleStatus == XRuleStatus.READY) {
                nodeContent.setRuleStatus(XRuleStatus.EXECUTING);
                nodeContent.setStartTime(LocalDateTime.now());
                return true;
            }
            return false;
        }
    }

    public boolean complete(CONTENT outputRuleContent){
        return end(XRuleStatus.COMPLETE ,null ,outputRuleContent);
    }

    public boolean intercept(){
        return end(XRuleStatus.INTERCEPT ,null ,null);
    }

    public boolean exception(Exception e ,boolean cancelIfUndone){
        boolean result = end(XRuleStatus.EXCEPTION, e, null);
        if (result){
            cancelIfUndone();
        }
        return result;
    }

    private void cancelIfUndone(){
        //如果有进行中的规则发送中断通知
        List<XNode<CONTENT>> nodes = reachableNodes(true, true);
        for (XNode<CONTENT> xNode : nodes) {
            Future<CONTENT> ruleFuture = xNode.getNodeContent().getRuleFuture();
            if (ruleFuture != null && !ruleFuture.isDone()){
                //                            log.debug("中断规则【{}】" ,xNode.getRule().name());
                ruleFuture.cancel(true);
            }
        }
    }

    public boolean cancel(){
        return end(XRuleStatus.CANCEL ,null ,null);
    }

    //规则执行结束
    private boolean end(XRuleStatus finalRuleStatus ,Exception e ,CONTENT outputRuleContent)
    {
        if (!finalRuleStatus.isFinal()){
            throw new IllegalArgumentException(MessageFormat.format("{}不能作为规则的终止状态" ,finalRuleStatus));
        }
        synchronized (nodeContent) {
            if (!getRuleStatus().isFinal()) {
                nodeContent.setRuleStatus(finalRuleStatus);
                nodeContent.setException(e);
                nodeContent.setEndTime(LocalDateTime.now());
                if (outputRuleContent != null) {
                    nodeContent.setOutputRuleContent(outputRuleContent);
                }
                return true;
            }
            return false;
        }
    }

    public XNode<CONTENT> setRuleFuture(Future<CONTENT> ruleFuture){
        if (nodeContent.getRuleFuture() == null){
            nodeContent.setRuleFuture(ruleFuture);
        }
        return this;
    }

    //只读，不要在外部修改
    public XNodeContent<CONTENT> getNodeContent() {
        return nodeContent;
    }

    /**
     * 未完成的前置节点数
     * @return
     */
    public int preNodeUndoneCount()
    {
        if (preNodes.isEmpty()){
            return 0;
        }
        return (int) preNodes.stream()
                .filter(preXNode -> !preXNode.getRuleStatus().isFinal())
                .count();
    }

    /**
     * 是否为起始节点
     * @return
     */
    public boolean isStartNode(){
        return preNodes.isEmpty();
    }

    /**
     * 是否为终止节点
     * @return
     */
    public boolean isTerminationNode(){
        return postNodes.isEmpty();
    }

    /**
     * 获取规则执行时间，未完成取当前执行时间
     * @return
     */
    public long getDuration(ChronoUnit chronoUnit){
        return nodeContent.getDuration(chronoUnit);
    }

    /**
     * 返回所有可达的节点
     * @return
     */
    public List<XNode<CONTENT>> reachableNodes(boolean containPre ,boolean containPost)
    {
        List<XNode<CONTENT>> nodes = new ArrayList<>();
        Set<XNode<CONTENT>> markedNodes = new HashSet<>();
        Queue<XNode<CONTENT>> reachableNodes = new ArrayDeque<>();
        //广度遍历
        reachableNodes.add(this);
        XNode<CONTENT> currentNode = null;
        while ((currentNode = reachableNodes.poll()) != null){
            if (markedNodes.contains(currentNode)){
                continue;
            }
            markedNodes.add(currentNode);
            nodes.add(currentNode);

            if (containPre){
                reachableNodes.addAll(currentNode.getPreNodes());
            }
            if (containPost) {
                reachableNodes.addAll(currentNode.getPostNodes());
            }
        }
        return nodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "XNode{" +
                "ruleName='" + rule.name() + '\'' +
                ", preNodes=" + preNodes.stream().map(node -> node.getRule().name()).collect(Collectors.toList()) +
                ", postNodes=" + postNodes.stream().map(node -> node.getRule().name()).collect(Collectors.toList()) +
                ", nodeContent=" + nodeContent.toString() +
                '}';
    }

}
