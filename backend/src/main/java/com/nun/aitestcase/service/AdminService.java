package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.User;
import com.nun.aitestcase.mapper.UserMapper;
import com.nun.aitestcase.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final UserMapper userMapper;

    public AdminService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserVO> listUsers(Long currentUserId) {
        checkAdmin(currentUserId);
        List<User> users = userMapper.selectList(null);
        List<UserVO> result = new ArrayList<>();
        for (User user : users) {
            UserVO vo = toUserVO(user);
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public void updateRole(Long currentUserId, Long targetUserId, String newRole) {
        checkAdmin(currentUserId);
        if (!"ADMIN".equals(newRole) && !"MEMBER".equals(newRole)) {
            throw new BusinessException("Invalid role: must be ADMIN or MEMBER");
        }
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        user.setRole(newRole);
        userMapper.updateById(user);
    }

    @Transactional
    public void deleteUser(Long currentUserId, Long targetUserId) {
        checkAdmin(currentUserId);
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException("Cannot delete yourself");
        }
        User user = userMapper.selectById(targetUserId);
        if (user == null) return;
        userMapper.deleteById(targetUserId);
    }

    private void checkAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "Admin access required");
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        return vo;
    }
}
