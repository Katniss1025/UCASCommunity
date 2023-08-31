package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component

public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替换
    private static final String REPLACEMENT = "***";

    // 初始化根节点
    private TrieNode rootNode = new TrieNode();


    // 实例被创建后自动完成敏感词库的加载和前缀树的构建
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");  // 通过类加载器获取敏感词库的字节流
                // 字节流转换为字符流
                // 然后在转换为具有缓冲区、读取性能高的BufferedReader
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword=reader.readLine())!=null){ // 每读一行获取一个keywords
                // 添加到前缀树
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词汇表失败："+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // 将一个敏感词加入前缀树
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for(int i=0; i<keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            // 指向子节点，进入下一轮训练
            tempNode = subNode;
            // 设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){  // 文本为空
            return null;
        }
        // 指针1:
        TrieNode tempNode = rootNode;
        // 指针2:
        int begin = 0;
        // 指针3:
        int position = 0;
        // 变长字符串保存扫描结果
        StringBuilder sb = new StringBuilder();
        // 用指针2做循环
        while(begin < text.length()){
            if(position < text.length()){
                Character c = text.charAt(position);
                // 跳过符号
                if(isSymbol(c)){
                    if(tempNode == rootNode){
                        begin ++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }
                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if(tempNode == null){ // 不是敏感词
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    tempNode = rootNode;
                } else if (tempNode.isKeywordEnd() ) { // 是敏感词
                    sb.append(REPLACEMENT);
                    begin = ++position;
                } else {
                    position++;
                }
            } else { // position遍历出界
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FF为东亚文字范围
        // CharUtils.isAsciiAlphanumeric()判断是否为普通字符
        return !CharUtils.isAsciiAlphanumeric(c)  && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树
    private class TrieNode{

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级节点字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }


}
