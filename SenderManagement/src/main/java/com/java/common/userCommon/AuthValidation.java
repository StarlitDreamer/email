package com.java.common.userCommon;



import com.java.esdao.repository.user.AuthRepository;
import com.java.esdao.repository.user.UserRepository;
import com.java.model.entity.user.AuthDocument;
import com.java.model.entity.user.UserDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthValidation {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthRepository authRepository;

    public boolean checkAuth(String authName, String userId) {
        // 参数校验
        if (authName == null || userId == null || authName.isEmpty() || userId.isEmpty()) {
            return false;
        }

        // 获取用户信息及权限列表
        UserDocument userDoc = userRepository.findByUserId(userId).orElse(null);
        if (userDoc == null || userDoc.getUserAuthId() == null || userDoc.getUserAuthId().isEmpty()) {
            return false;
        }

        // 通过权限名称获取权限ID
        AuthDocument authDoc = authRepository.findByAuthName(authName).orElse(null);
        if (authDoc == null) {
            return false;
        }

        // 检查用户是否拥有该权限
        return userDoc.getUserAuthId().contains(authDoc.getAuthId());
    }
}
