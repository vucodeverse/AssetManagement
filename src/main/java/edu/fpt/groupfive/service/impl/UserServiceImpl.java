package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.mapper.UserMapper;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentDAO departmentDAO;

    // Tạo một user mới
    @Override
    public void createUser(UserCreateRequest request) {

        // Map dữ liệu từ request qua model
        Users user = userMapper.toUser(request);
        // Mã hóa & Lưu mật khẩu
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Mặc định trạnh thái khi tạo
        user.setStatus("ACTIVE");
        // Lấy thời gian hiện tại
        user.setCreatedDate(LocalDateTime.now());

        // Lưu user xuống database
        userDAO.insert(user);

        // Nếu vai trò là trưởng phòng cập nhật trưởng phòng cho phòng đấy luôn
        if (user.getRole() == Role.DEPARTMENT_MANAGER) {
            departmentDAO.updateManager(user.getDepartmentId(), user.getUserId());
        }
    }

    // Update thông tin của user
    @Override
    public void updateUser(UserUpdateRequest request) {
        // Lấy user theo id
        Users existing = userDAO.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role oldRole = existing.getRole();
        Integer oldDepartmentId = existing.getDepartmentId();

        // Map dữ liệu từ request qua model
        userMapper.updateUserFromRequest(request, existing);

        // Chỉ đổi password nếu nhập mới
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Lưu thời gian cập nhật
        existing.setUpdatedDate(LocalDateTime.now());

        // Nếu trước là manager trong một phòng mà giờ không còn
        if (oldRole == Role.DEPARTMENT_MANAGER && existing.getRole() != Role.DEPARTMENT_MANAGER) {
            departmentDAO.updateManager(oldDepartmentId, null);
        }

        // Nếu trước không phải manager mà giờ là manager của phòng
        if (oldRole != Role.DEPARTMENT_MANAGER && existing.getRole() == Role.DEPARTMENT_MANAGER) {
            departmentDAO.updateManager(existing.getDepartmentId(), existing.getUserId());
        }

        // Nếu vẫn là manager nhưng đổi phòng
        if (oldRole == Role.DEPARTMENT_MANAGER
                && existing.getRole() == Role.DEPARTMENT_MANAGER
                && !oldDepartmentId.equals(existing.getDepartmentId())) {

            // Check phòng mới

            // Xóa manager phòng cũ
            departmentDAO.updateManager(oldDepartmentId, null);

            // Set manager phòng mới
            departmentDAO.updateManager(existing.getDepartmentId(), existing.getUserId());
        }

        // Cập nhật thông tin
        userDAO.update(existing);

    }

    // Remove một user (Update status)
    @Override
    public void removeUser(Integer id) {
        Users user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userDAO.delete(id);
    }

    // Tìm kiếm user theo keyword
    @Override
    public List<UserResponse> searchUsers(
            int page, int size, String status,
            Integer departmentId, Role role, String keyword) {

        int offset = page * size;

        List<Users> users = userDAO.searchUsers(offset, size, status,
                departmentId, role, keyword);

        return userMapper.toResponseList(users);
    }

    @Override
    public int getTotalPagesWithFilter(
            int size, String status, Integer departmentId,
            Role role, String keyword) {

        int total = userDAO.countUsersWithFilter(status, departmentId, role, keyword);

        return (int) Math.ceil((double) total / size);
    }

    @Override
    public UserResponse getUserById(Integer id) {
        Users user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUserByDepartId(Integer departId) {
        List<Users> list = userDAO.findByDepartmentId(departId);
        return userMapper.toResponseList(list);
    }

    @Override
    public List<Users> getAllUsers() {
        return List.of();
    }

    @Override
    public List<UserResponse> getAllUsers2() {
        return userDAO.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    public Integer getUserIdByUsername(String username) {
        return userDAO.findUserIdByUsername(username);
    }

    @Override
    public String findNameById(Integer userId) {
        return userDAO.findFullNameById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDAO.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email, Integer userId) {
        return userDAO.existsByEmail(email, userId);
    }

    @Override
    public boolean existsManager(Integer departmentId, Integer userId) {
        return userDAO.existsManagerByDepartment(departmentId, userId);
    }

    @Override
    public Map<Integer, String> getUserIdToUsernameMap() {
        return userDAO.findAll().stream()
                .collect(Collectors.toMap(Users::getUserId, Users::getUsername, (existing, replacement) -> existing));
    }

}
