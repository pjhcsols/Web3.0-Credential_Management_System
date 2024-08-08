package gift.controller.category;

import gift.domain.category.Category;
import gift.service.category.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "모든 카테고리 조회", description = "모든 카테고리를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "카테고리 ID로 조회", description = "카테고리 ID로 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "조회할 카테고리의 ID", required = true)
            @PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "새 카테고리 추가", description = "새로운 카테고리를 추가합니다.")
    @PostMapping
    public ResponseEntity<Category> addCategory(
            @Parameter(description = "추가할 카테고리 정보", required = true)
            @RequestBody @Valid Category category) {
        Category createdCategory = categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @Operation(summary = "카테고리 수정", description = "카테고리 ID로 카테고리 정보를 수정합니다.")
    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "수정할 카테고리의 ID", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "수정할 카테고리 정보", required = true)
            @RequestBody @Valid Category updatedCategory) {
        Category savedCategory = categoryService.updateCategory(categoryId, updatedCategory);
        return ResponseEntity.ok(savedCategory);
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리 ID로 카테고리를 삭제합니다.")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "삭제할 카테고리의 ID", required = true)
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}

