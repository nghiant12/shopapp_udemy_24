package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtil;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.entities.Product;
import com.project.shopapp.entities.ProductImage;
import com.project.shopapp.responses.*;
import com.project.shopapp.services.IProductService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final LocalizationUtil util;

    @PostMapping("")
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(CreateProductResponse.builder().message(util.getMessage(MessageKeys.CREATE_PRODUCT_FAILED, errMessages)).build());
            }

            Product newProduct = productService.createProduct(productDTO);

            return ResponseEntity.ok(CreateProductResponse.builder().message(util.getMessage(MessageKeys.CREATE_PRODUCT_SUCCESSFULLY)).product(newProduct).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CreateProductResponse.builder().message(util.getMessage(MessageKeys.CREATE_PRODUCT_FAILED, e.getMessage())).build());
        }
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@ModelAttribute("files") List<MultipartFile> files, @PathVariable("id") Long productId) {
        try {
            Product existingProduct = productService.getProductById(productId);
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                return ResponseEntity.badRequest().body(UploadProductImageResponse.builder().message(MessageKeys.ERROR_MAX_5_IMAGES).build());
            }
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.getSize() == 0) {
                    continue;
                }
                // kiểm tra kích thước file và định dạng
                if (file.getSize() > 10 * 1024 * 1024) { //kích thước > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(UploadProductImageResponse.builder().message(MessageKeys.FILE_LARGE).build());
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(UploadProductImageResponse.builder().message(MessageKeys.FILE_MUST_BE_IMAGE).build());
                }
                String filename = storeFile(file);
                ProductImage productImage = productService.createProductImage(existingProduct.getId(), ProductImageDTO.builder().imageUrl(filename).build());
                productImages.add(productImage);
            }
            return ResponseEntity.ok(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("images/");
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewsImage(@PathVariable String imageName) {
        try {
            Path imagePath = Paths.get("uploads/" + imageName);
            UrlResource urlResource = new UrlResource(imagePath.toUri());
            if (urlResource.exists()) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(urlResource);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound.gif").toUri()));
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        if (isImageFile(file) && file.getOriginalFilename() != null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // đường dẫn đến thư mục muốn lưu file
        Path uploadDir = Paths.get("uploads");
        // kiểm tra và tạo thư mục nếu nó không tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // đường dẫn đầy đủ đến file
        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // copy file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<ProductResponse> productPage = productService.getProducts(keyword, categoryId, pageRequest);
        int totalPages = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();
        return ResponseEntity.ok(ProductListResponse
                .builder()
                .products(products)
                .totalPages(totalPages)
                .build());
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam String ids) {
        try {
            List<Long> productsIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Product> products = productService.findProductsByIds(productsIds);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long productId) {
        try {
            Product existingProduct = productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.status(HttpStatus.OK).body(util.getMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(util.getMessage(MessageKeys.DELETE_PRODUCT_FAILED, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateProductResponse> updateProduct(
            @PathVariable long id,
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(UpdateProductResponse.builder()
                        .message(util.getMessage(MessageKeys.UPDATE_PRODUCT_FAILED, errMessages))
                        .build());
            }
            Product product = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(UpdateProductResponse.builder()
                    .message(util.getMessage(MessageKeys.UPDATE_PRODUCT_SUCCESSFULLY))
                    .product(product).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UpdateProductResponse.builder()
                    .message(util.getMessage(MessageKeys.UPDATE_PRODUCT_FAILED, e.getMessage()))
                    .build());
        }
    }

    //    @PostMapping("/generateFakeProducts")
    //    public ResponseEntity<String> generateFakeProducts() {
    private ResponseEntity<String> generateFakeProducts() {
        Faker faker = new Faker();
        for (int i = 0; i < 1000; i++) {
            String productName = faker.commerce().productName();
            if (productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder().name(productName).price((float) faker.number().numberBetween(10, 9000000)).description(faker.lorem().sentence()).thumbnail("").categoryId((long) faker.number().numberBetween(1, 100)).build();
            try {
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake products created successfully");
    }
}
