package com.transportation.booking.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.transportation.booking.dto.request.UserCreationRequest;
import com.transportation.booking.dto.request.UserUpdateRequest;
import com.transportation.booking.dto.response.UserResponse;
import com.transportation.booking.entity.User;
import com.transportation.booking.enums.Role;
import com.transportation.booking.exception.AppException;
import com.transportation.booking.exception.ErrorCode;
import com.transportation.booking.mapper.UserMapper;
import com.transportation.booking.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        // 1. Map dữ liệu cơ bản
        User user = userMapper.toUser(request);

        // 2. Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. LƯU SỐ ĐIỆN THOẠI (Quan trọng)
        user.setPhone(request.getPhone());

        // 4. Set Role (SỬA LẠI CHỖ NÀY THÀNH SỐ ÍT CHO KHỚP VỚI BẠN)
        user.setRole(Role.USER);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }
}