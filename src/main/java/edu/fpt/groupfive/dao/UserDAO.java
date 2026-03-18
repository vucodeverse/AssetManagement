package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<Users> findUserByUsername(String username);

    void insert (Users users);

    void update (Users users);

    void delete (Integer id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email, Integer userId);

    boolean existsManagerByDepartment(Integer departmentId, Integer userId);

    boolean exitsDirector(Role role);

    List<Users> findAll();

    Integer findUserIdByUsername(String username);

    Optional<Users> findById(Integer id);

    List<Users> findByDepartmentId(Integer departmentId);

    int countUsersInDepartment(Integer departmentId);

    List<Users> searchUsers(int offset, int size, String status,
                            Integer departmentId, Role role, String keyword);

    int countUsersWithFilter(String status, Integer departmentId,
                             Role role, String keyword);
}
