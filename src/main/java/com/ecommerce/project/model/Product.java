package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank
    @Size(min = 3, message = "product name must contain at least 3 characters", max = 30)
    private String productName;
    private String image;

    @NotBlank
    @Size(min = 6, message = "product description must contain at least 3 characters", max = 50)
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private double price;

    @NotNull(message = "Discount is required")
    @Min(value = 0, message = "Discount cannot be less than 0")
    @Max(value = 100, message = "Discount cannot be more than 100")
    private double discount;
    private double specialPrice;

    //many product to One category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    //many products to one Seller
    @ManyToOne
    @JoinColumn(name = "seller_Id")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<CartItem> products = new ArrayList<>();
}
