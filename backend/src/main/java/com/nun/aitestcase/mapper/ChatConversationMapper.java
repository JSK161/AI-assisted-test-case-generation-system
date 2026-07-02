package com.nun.aitestcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nun.aitestcase.entity.ChatConversation;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    @Select("SELECT id, user_id, title, requirement, created_at, updated_at FROM chat_conversation WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<ChatConversation> selectByUserId(Long userId);
}
