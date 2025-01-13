package com.java.email.common.userCommon;

import com.java.email.esdao.UserDocument;
import com.java.email.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SubordinateValidation {
    
    private final UserRepository userRepository;

    public SubordinateValidation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 验证用户是否是当前用户的下属或自己
     * @param userId 待验证的用户ID
     * @param currentUserId 当前用户ID
     * @return 是否为下属或自己
     */
    public boolean isSubordinateOrSelf(String userId, String currentUserId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(currentUserId)) {
            return false;
        }

        // 是否为自己
        if (currentUserId.equals(userId)) {
            return true;
        }

        // 检查是否为下属
        UserDocument user = userRepository.findByUserId(userId).orElse(null);
        return user != null && currentUserId.equals(user.getBelongUserId());
    }

    /**
     * 根据用户名查找当前用户的下属和自己
     * @param userName 用户名（支持模糊查询）
     * @param currentUserId 当前用户ID
     * @return 验证结果，包含有效的用户列表和验证状态
     */
    public ValidationResult findSubordinatesAndSelfByName(String userName, String currentUserId) {
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(currentUserId)) {
            return new ValidationResult(false, Collections.emptyList());
        }

        List<UserDocument> users = userRepository.findByUserNameLike(userName);
        if (users.isEmpty()) {
            return new ValidationResult(false, Collections.emptyList());
        }

        List<UserDocument> validUsers = users.stream()
            .filter(user -> currentUserId.equals(user.getUserId()) || 
                          currentUserId.equals(user.getBelongUserId()))
            .collect(Collectors.toList());

        return new ValidationResult(!validUsers.isEmpty(), validUsers);
    }

    /**
     * 获取当前用户的所有下属ID
     * @param currentUserId 当前用户ID
     * @return 下属ID集合
     */
    public Set<String> getAllSubordinateIds(String currentUserId) {
        if (!StringUtils.hasText(currentUserId)) {
            return Collections.emptySet();
        }

        List<UserDocument> subordinates = userRepository.findByBelongUserId(currentUserId);
        return subordinates.stream()
            .map(UserDocument::getUserId)
            .collect(Collectors.toSet());
    }

    /**
     * 根据用户名查询用户ID
     * @param userName 用户名
     * @return 用户ID集合
     */
    public Set<String> getUserIdsByUserName(String userName) {
        if (!StringUtils.hasText(userName)) {
            return null;
        }
        List<UserDocument> users = userRepository.findByUserNameLike(userName);
        return users.stream()
            .map(UserDocument::getUserId)
            .collect(Collectors.toSet());
    }
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<UserDocument> validUsers;

        public ValidationResult(boolean valid, List<UserDocument> validUsers) {
            this.valid = valid;
            this.validUsers = validUsers;
        }

        public boolean isValid() {
            return valid;
        }

        public List<UserDocument> getValidUsers() {
            return validUsers;
        }

        public Set<String> getValidUserIds() {
            return validUsers.stream()
                .map(UserDocument::getUserId)
                .collect(Collectors.toSet());
        }
    }
} 