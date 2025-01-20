package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Img;
import com.java.email.repository.ImgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ImgService {

    @Autowired
    private ImgRepository imgRepository;

    @Autowired
    private UserService userService;

    /**
     * 根据条件筛选图片
     *
     * @param belongUserId 所属用户ID列表
     * @param creatorId    创建人ID
     * @param status       图片状态
     * @param imgName      图片名称
     * @param imgSize      图片大小
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的图片分页结果
     */
//    public Result<Page<Img>> findImgsByCriteria(
//            List<String> belongUserId, String creatorId,
//            Integer status, String imgName, Long imgSize,
//            int page, int size) {
//        try {
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null && !belongUserId.isEmpty()) {
//                return Result.success(imgRepository.findByBelongUserIdIn(belongUserId, pageable));
//            } else if (creatorId != null) {
//                return Result.success(imgRepository.findByCreatorId(creatorId, pageable));
//            } else if (status != null) {
//                return Result.success(imgRepository.findByStatus(status, pageable));
//            } else if (imgName != null) {
//                return Result.success(imgRepository.findByImgName(imgName, pageable));
//            } else if (imgSize != null) {
//                return Result.success(imgRepository.findByImgSize(imgSize, pageable));
//            } else {
//                // 如果没有条件，返回所有图片（分页）
//                return Result.success(imgRepository.findAll(pageable));
//            }
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }
    public Result<Page<Img>> findImgsByCriteria(
            String currentUserId, // 当前用户的ID
            int currentUserRole,  // 当前用户的角色
            List<String> belongUserId,
            String creatorId,
            Integer status,
            String imgName,
            Long imgSize,
            int page,
            int size) {

        try {
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色决定查询条件
            if (currentUserRole == 1) { // 公司角色，可以查看所有图片
                return Result.success(imgRepository.findAll(pageable));
            } else if (currentUserRole == 2) { // 大管理角色，可以查看所有图片
                return Result.success(imgRepository.findAll(pageable));
            } else if (currentUserRole == 3) { // 小管理角色，可以查看公司、自己、下属用户的图片
                List<String> allowedUserIds = new ArrayList<>();
                allowedUserIds.add("1"); // 公司用户ID
                allowedUserIds.add(currentUserId); // 自己
                // 假设有一个方法可以获取当前用户的下属用户ID列表
                allowedUserIds.addAll(getSubordinateUserIds(currentUserId));
                return Result.success(imgRepository.findByBelongUserIdIn(allowedUserIds, pageable));
            } else if (currentUserRole == 4) { // 普通用户，只能查看自己的图片
                return Result.success(imgRepository.findByBelongUserIdIn(Collections.singletonList(currentUserId), pageable));
            } else {
                // 如果没有匹配的角色，返回空结果
                return Result.success(new PageImpl<>(Collections.emptyList()));
            }
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // 假设有一个方法可以获取当前用户的下属用户ID列表
    private List<String> getSubordinateUserIds(String userId) {
        // 这里实现获取下属用户ID的逻辑
        // 例如：从数据库或ES中查询
        return userService.getSubordinateUserIds(userId);
    }
}