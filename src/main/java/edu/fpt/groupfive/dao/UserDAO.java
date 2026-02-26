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
    boolean existsByEmail(String email);
    List<Users> findAll();
    Optional<Users> findById(Integer id);
    List<Users> findByDepartmentId(Integer departmentId);
    List<Users> findAllByFirstNameDesc();
    List<Users> findAllByFirstNameAsc();
    List<Users> findAllByCreateDateAsc();
    List<Users> findAllByCreateDateDesc();
    int countUsersInDepartment(Integer departmentId);
    List<Users> searchUsers(int offset, int size, String status,
            Integer departmentId, Role role, String keyword);

    int countUsersWithFilter(String status, Integer departmentId, Role role, String keyword);
}
