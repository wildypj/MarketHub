package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 character")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be at least 5 character")
    private String BuildingName;

    @NotBlank
    @Size(min = 4, message = "city name must be at least 4 character")
    private String city;

    @NotBlank
    @Size(min = 2, message = "state name must be at least 2 character")
    private String state;


    @NotBlank
    @Size(min = 2, message = "Country name must be at least 2 character")
    private String country;

    @NotBlank
    @Size(min = 5, message = "zipCode name must be at least 2 character")
    private String zipCode;

    //link to users
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String street, String buildingName, String zipCode, String city, String state, String country) {
        this.street = street;
        this.BuildingName = buildingName;
        this.zipCode = zipCode;
        this.city = city;
        this.state = state;
        this.country = country;
    }
}
