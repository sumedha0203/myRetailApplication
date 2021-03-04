package com.target.myretail.service;

import com.target.myretail.domain.Price;
import com.target.myretail.domain.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ProductService.java - This class contains service methods that perform the main business logic and/or interact
 * with the database for various CRUD operations
 * @author Sumedha Kamra
 */
@Service
public interface ProductService {

    Optional<Price> findPriceById(int id);

    Product addProduct(Product product);

    Price updateProductPrice(Price price);

    void deletePriceById(int id);

    void deletePrices();

    void deleteProducts();

    List<Price> findAll();



}
