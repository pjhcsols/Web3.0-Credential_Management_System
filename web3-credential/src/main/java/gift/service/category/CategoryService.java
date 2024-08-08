package gift.service.category;

import gift.domain.category.Category;
import gift.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Transactional
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = getCategoryById(id);

        existingCategory.changeName(updatedCategory.getName());
        existingCategory.changeColor(updatedCategory.getColor());
        existingCategory.changeImageUrl(updatedCategory.getImageUrl());
        existingCategory.changeDescription(updatedCategory.getDescription());

        return existingCategory;  // JPA가 트랜잭션 커밋 시 자동으로 업데이트
    }


    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
