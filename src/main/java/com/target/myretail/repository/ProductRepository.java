package com.target.myretail.repository;

import com.target.myretail.domain.Price;
import com.target.myretail.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * This is the custom Spring Data implementation for CRUD operations on MongoDB document Price
 * @author Sumedha Kamra
 */
public interface ProductRepository extends MongoRepository<Product, Integer> {

}

