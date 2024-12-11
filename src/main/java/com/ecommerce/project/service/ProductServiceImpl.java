package com.ecommerce.project.service;

import com.ecommerce.project.expections.APIException;
import com.ecommerce.project.expections.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    //inject category repository
    private final CategoryRepository categoryRepository;
    //inject Product Repository
    private final ProductRepository productRepository;
    //inject Cart Repository
    private final CartRepository cartRepository;
    //inject ModelMapper
    private final ModelMapper modelMapper;
    //inject file Service
    private final FileService fileService;

    private final CartService cartService;

    @Value("${project.image}")
    private String path;


    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sorOrder) {
        //use spring built in sort class
        Sort sortByAndOrder = sorOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        //check if product size is 0 or not  if it is throw APIException
        if(products.isEmpty()){
            throw new APIException("No product Exist!");
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sorOrder) {
        // Check for categoryId in DB using categoryId
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        //Sort and page details
        Sort sortByAndOrder = sorOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = pageProducts.getContent();
        //add later validation of products size is 0 then throw APIException

        if(products.isEmpty()){
            throw new APIException("No product found for category: " + category.getCategoryName());
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sorOrder) {
        //Sort and page details
        Sort sortByAndOrder = sorOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        //check if product size is 0 or not  if it is throw APIException
        if(products.isEmpty()){
            throw new APIException("No product found for keyword: " + keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }


    // Put Method
    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        //check product  exist in DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        Product product = modelMapper.map(productDTO, Product.class);

         //updated product info with user info in request body
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //Save the product in DB
         Product saveProduct = productRepository.save(productFromDb);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(saveProduct, ProductDTO.class);
    }

    // Put Method for image

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // First get existing product from Db
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        //Second Upload image to server -- (first do it in an img directory )
        // Get the file name of uploaded image
        String filename = fileService.uploadImage(path, image);

        //Fourth Update the new file name to the product
        productFromDb.setImage(filename);

        //Last Save && return DTO after mapping product to DTO
        Product updateProduct = productRepository.save(productFromDb);
        return modelMapper.map(updateProduct, ProductDTO.class);
    }




    // POST Method
    private double calculateSpecialPrice(double price, double discount) {
        return price - (discount * 0.01 * price);
    }


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        // Check for categoryId in DB using categoryId
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        //check if product is already exists in category
        boolean isProductNew = category.getProducts().stream()
                .noneMatch(product -> product.getProductName().equals(productDTO.getProductName()));

        if(!isProductNew) {
            throw new APIException("Product '" + productDTO.getProductName()
                    + "' already exists in category '" + category.getCategoryName() + "'");
        }

        // Map DTO to Product entity
        Product product = modelMapper.map(productDTO, Product.class);
        product.setImage("default.png");
        product.setCategory(category);
        product.setSpecialPrice(calculateSpecialPrice(product.getPrice(), product.getDiscount()));

        // Save the product and return the DTO
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    // Delete Method
    @Override
    public ProductDTO deleteProduct(Long productId) {
        //check product exist in DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Fetch all cart that have this product
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        //delete product in DB
        productRepository.delete(productFromDb);
        return modelMapper.map(productFromDb, ProductDTO.class);
    }
}
