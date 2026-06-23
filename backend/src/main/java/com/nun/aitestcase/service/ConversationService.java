package com.nun.aitestcase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.ChatConversation;
import com.nun.aitestcase.entity.ChatMessage;
import com.nun.aitestcase.mapper.ChatConversationMapper;
import com.nun.aitestcase.mapper.ChatMessageMapper;
import com.nun.aitestcase.vo.ConversationDetailVO;
import com.nun.aitestcase.vo.ConversationListItemVO;
import com.nun.aitestcase.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public ConversationService(ChatConversationMapper conversationMapper, ChatMessageMapper messageMapper, ObjectMapper objectMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    public List<ConversationListItemVO> listByUser(Long userId) {
        List<ChatConversation> list = conversationMapper.selectByUserId(userId);
        List<ConversationListItemVO> result = new ArrayList<>();
        for (ChatConversation c : list) {
            ConversationListItemVO vo = new ConversationListItemVO();
            vo.setId(c.getId());
            vo.setTitle(c.getTitle());
            vo.setRequirement(c.getRequirement());
            vo.setUpdatedAt(c.getUpdatedAt());
            result.add(vo);
        }
        return result;
    }

    public ConversationDetailVO getDetail(Long id, Long userId) {
        ChatConversation conv = conversationMapper.selectById(id);
        if (conv == null) {
            throw new BusinessException(404, "Conversation not found");
        }
        if (!conv.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        List<ChatMessage> messages = messageMapper.selectByConversationId(id);
        ConversationDetailVO vo = new ConversationDetailVO();
        vo.setId(conv.getId());
        vo.setTitle(conv.getTitle());
        vo.setRequirement(conv.getRequirement());
        vo.setMessages(messages.stream().map(this::toMessageVO).toList());
        try {
            if (conv.getAnswers() != null) {
                vo.setAnswers(objectMapper.readValue(conv.getAnswers(), Object.class));
            }
            if (conv.getGeneratedPlan() != null) {
                vo.setGeneratedPlan(objectMapper.readValue(conv.getGeneratedPlan(), Object.class));
            }
        } catch (JsonProcessingException ignored) {}
        vo.setCreatedAt(conv.getCreatedAt());
        vo.setUpdatedAt(conv.getUpdatedAt());
        return vo;
    }

    @Transactional
    public Long create(Long userId, String title, String requirement) {
        ChatConversation conv = new ChatConversation();
        conv.setUserId(userId);
        conv.setTitle(title);
        conv.setRequirement(requirement);
        conversationMapper.insert(conv);
        return conv.getId();
    }

    @Transactional
    public void addMessage(Long conversationId, Long userId, String sender, String content) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSender(sender);
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    @Transactional
    public void updateAnswers(Long conversationId, Long userId, Object answers) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        try {
            conv.setAnswers(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize answers", e);
        }
        conversationMapper.updateById(conv);
    }

    @Transactional
    public void updatePlan(Long conversationId, Long userId, Object plan) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        try {
            conv.setGeneratedPlan(objectMapper.writeValueAsString(plan));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize plan", e);
        }
        conversationMapper.updateById(conv);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        ChatConversation conv = conversationMapper.selectById(id);
        if (conv == null) return;
        if (!conv.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }
        conversationMapper.deleteById(id);
    }

    private MessageVO toMessageVO(ChatMessage msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setSender(msg.getSender());
        vo.setContent(msg.getContent());
        vo.setCreatedAt(msg.getCreatedAt());
        return vo;
    }
}
