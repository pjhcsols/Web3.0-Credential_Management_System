package gift.controller.product;

import gift.domain.product.Product;
import gift.domain.product.ProductRequestDTO;
import gift.service.product.ProductService;
import gift.util.ImageStorageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "모든 상품 조회", description = "페이징 처리된 모든 상품을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable,
                                                        @RequestParam(required = false) Long categoryId) {
        Page<Product> products = productService.getAllProducts(pageable, categoryId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "상품 ID로 조회", description = "상품 ID로 상품을 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "조회할 상품의 ID", required = true)
            @PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "새 상품 추가", description = "새로운 상품을 추가합니다.")
    @PostMapping
    public ResponseEntity<Product> addProduct(
            @Parameter(description = "추가할 상품 정보", required = true)
            @RequestBody @Valid ProductRequestDTO productRequestDTO) {
        Product product = new Product(productRequestDTO.getName(), productRequestDTO.getPrice(),
                productRequestDTO.getDescription(), productRequestDTO.getImageUrl());
        productService.addProduct(product, productRequestDTO.getCategoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "상품 수정", description = "상품 ID로 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "수정할 상품의 ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "수정할 상품 정보", required = true)
            @RequestBody @Valid ProductRequestDTO productRequestDTO) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        product.update(productRequestDTO.getName(), productRequestDTO.getPrice(),
                productRequestDTO.getDescription(), productRequestDTO.getImageUrl());

        productService.updateProduct(product, productRequestDTO.getCategoryId());

        return ResponseEntity.ok(product);
    }

    @Operation(summary = "상품 삭제", description = "상품 ID로 상품을 삭제합니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "삭제할 상품의 ID", required = true)
            @PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 이미지 업로드", description = "새로운 상품 이미지를 업로드합니다.")
    @PostMapping("/imageUpload")
    public ResponseEntity<String> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        String imagePath = ImageStorageUtil.saveImage(imageFile);
        String imageUrl = ImageStorageUtil.encodeImagePathToBase64(imagePath);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "상품 이미지 수정", description = "상품 ID로 상품 이미지를 수정합니다.")
    @PostMapping("/{productId}/imageUpdate")
    public ResponseEntity<Product> updateProductImage(
            @Parameter(description = "이미지를 수정할 상품의 ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "업로드할 새로운 이미지 파일", required = true)
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            ImageStorageUtil.deleteImage(ImageStorageUtil.decodeBase64ImagePath(product.getImageUrl()));
        }

        String imagePath = ImageStorageUtil.saveImage(imageFile);
        String imageUrl = ImageStorageUtil.encodeImagePathToBase64(imagePath);
        product.updateImage(imageUrl);
        productService.updateProduct(product, product.getCategory().getId());
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "상품 이미지 조회", description = "인코딩된 경로를 통해 상품 이미지를 조회합니다.")
    @GetMapping(value = "/{base64EncodedPath}/imageView", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImageByEncodedPath(
            @Parameter(description = "Base64로 인코딩된 이미지 경로", required = true)
            @PathVariable String base64EncodedPath) throws IOException {
        String imagePath = ImageStorageUtil.decodeBase64ImagePath(base64EncodedPath);
        byte[] imageBytes = java.nio.file.Files.readAllBytes(new File(imagePath).toPath());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    }
}
