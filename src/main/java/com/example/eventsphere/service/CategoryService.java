package com.example.eventsphere.service;

import com.example.eventsphere.model.Category;
import com.example.eventsphere.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public List<Category> findAll()           { return categoryRepository.findAll(); }
    public List<Category> findActive()        { return categoryRepository.findByActiveTrue(); }
    public Optional<Category> findById(Long id) { return categoryRepository.findById(id); }
    public Category save(Category cat)        { return categoryRepository.save(cat); }
    public void delete(Long id)               { categoryRepository.deleteById(id); }
}
