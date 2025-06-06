package com.java.email.common.userCommon;

import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.entity.user.UserDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
     * 验证用户是否为下属
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 验证结果
     */
    public ValidationResult isSubordinate(String currentUserId, String targetUserId) {
        if (!StringUtils.hasText(currentUserId) || !StringUtils.hasText(targetUserId)) {
            return new ValidationResult(false, Collections.emptyList());
        }

        UserDocument targetUser = userRepository.findByUserId(targetUserId).orElse(null);
        if (targetUser == null) {
            return new ValidationResult(false, Collections.emptyList());
        }

        boolean isSubordinate = currentUserId.equals(targetUser.getBelongUserId());
        return new ValidationResult(isSubordinate, isSubordinate ? Arrays.asList(targetUser) : Collections.emptyList());
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