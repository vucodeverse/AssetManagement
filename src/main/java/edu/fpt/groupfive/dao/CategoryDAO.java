package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDAO {
    void insert(Category category);

    void update(Category category);

    void delete(Integer id);

    Optional<Category> findById(Integer id);

    List<Category> findAll();

    boolean existsByName(String name);
}
