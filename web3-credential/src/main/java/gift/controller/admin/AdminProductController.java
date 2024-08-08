package gift.controller.admin;

import gift.domain.category.Category;
import gift.domain.product.Product;
import gift.domain.product.ProductRequestDTO;
import gift.service.category.CategoryService;
import gift.service.product.ProductService;
import gift.util.ImageStorageUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getProducts(Model model, @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        var productsPage = productService.getAllProducts(pageable, null);
        var categories = categoryService.getAllCategories();

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categories", categories);
        return "product";
    }


    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute ProductRequestDTO productRequestDTO,
                             @RequestPart MultipartFile imageFile) throws IOException {

        String imagePath = ImageStorageUtil.saveImage(imageFile);
        String imageUrl = ImageStorageUtil.encodeImagePathToBase64(imagePath);

        Product product = new Product(productRequestDTO.getName(), productRequestDTO.getPrice(),
                productRequestDTO.getDescription(), imageUrl);
        productService.addProduct(product, productRequestDTO.getCategoryId());

        return "redirect:/admin/products";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute ProductRequestDTO productRequestDTO,
                                @RequestPart MultipartFile imageFile) throws IOException {

        Product product = productService.getProductById(id);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            ImageStorageUtil.deleteImage(ImageStorageUtil.decodeBase64ImagePath(product.getImageUrl()));
        }

        String imagePath = ImageStorageUtil.saveImage(imageFile);
        String imageUrl = ImageStorageUtil.encodeImagePathToBase64(imagePath);

        product.update(productRequestDTO.getName(), productRequestDTO.getPrice(),
                productRequestDTO.getDescription(), imageUrl);

        productService.updateProduct(product, productRequestDTO.getCategoryId());

        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            ImageStorageUtil.deleteImage(ImageStorageUtil.decodeBase64ImagePath(product.getImageUrl()));
        }
        productService.deleteProduct(id);

        return "redirect:/admin/products";
    }
}
