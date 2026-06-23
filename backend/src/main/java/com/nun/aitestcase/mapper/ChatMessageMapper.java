package com.nun.aitestcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nun.aitestcase.entity.ChatMessage;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT id, conversation_id, sender, content, created_at FROM chat_message WHERE conversation_id = #{conversationId} ORDER BY created_at ASC")
    List<ChatMessage> selectByConversationId(Long conversationId);
}
