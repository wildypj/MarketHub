package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.Page_Number, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.Page_Size, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
       ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
       return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // Create a get request for product by category
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.Page_Number, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.Page_Size, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sorOrder
            ) {
        ProductResponse productResponse = productService.searchByCategory(categoryId,pageNumber, pageSize, sortBy, sorOrder );
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    // Create a get request for product by keyWord
    @GetMapping("/public/categories/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductByKeyword(
            @PathVariable String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.Page_Number, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.Page_Size, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sorOrder
            ) {
        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sorOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    // Put API
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productDTO, productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }


    // POST API
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId){
        ProductDTO savedproductDTO = productService.addProduct(categoryId, productDTO);
        return new ResponseEntity<>(savedproductDTO, HttpStatus.CREATED);
    }


   // Delete API
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){

        ProductDTO deletedProductDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProductDTO, HttpStatus.OK);
    }

    // Put for images
    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("image") MultipartFile image) throws IOException {
        ProductDTO updatedproductDTO = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedproductDTO, HttpStatus.OK);
    }
}
