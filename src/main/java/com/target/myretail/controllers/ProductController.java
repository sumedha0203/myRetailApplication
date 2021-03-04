package com.target.myretail.controllers;

import com.target.myretail.domain.Price;
import com.target.myretail.domain.Product;
import com.target.myretail.exceptions.InputMismatchException;
import com.target.myretail.exceptions.NoSuchElementFoundException;
import com.target.myretail.service.ProductService;
import com.target.myretail.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * ProductController.java - This class contains API methods for Product related details
 * @author Sumedha Kamra
 */
@RestController
@RequestMapping("/products")
class ProductController {
    private static final String PRODUCT_RESOURCE_URL = "https://redsky.target.com/v2/pdp/tcin";
    private static final String PRODUCT_NAME_JSON_KEY = "title";

    @Autowired
    private ProductService productService;

    /**
     * to get product title from external api id
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}/name")
    public ResponseEntity<?> getNameByExternalId(@PathVariable("id") int id) {

        String response
                = restTemplate().getForObject(PRODUCT_RESOURCE_URL+ "/" + id + "?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics", String.class);

        if (response == null) {
            throw new NoSuchElementFoundException("Product: " + id + " not found.");
        }

        JsonUtils.getValueFromResponse(response, PRODUCT_NAME_JSON_KEY);

        return new ResponseEntity<>(JsonUtils.getProductTitle(), HttpStatus.OK);
    }

    /**
     * to get product details from external id
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getExternalProductDetails(@PathVariable("id") int id) {

        // get product name
        ResponseEntity<?> response = getNameByExternalId(id);

        if (response == null || response.getBody() == null) {
            throw new NoSuchElementFoundException("Product: " + id + " not found.");
        }
        Product product = new Product();
        product.setProductName(response.getBody().toString());
        product.setProductId(id);

        // get product price
        Optional<Price> optional = productService.findPriceById(id);
        optional.ifPresent(price -> product.setCurrentPrice(optional.get()));

        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    /**
     * add product api
     * @param id
     * @param product
     * @return
     */
    @PutMapping(value = "/{id}/product")
    ResponseEntity<?> addProduct(@PathVariable("id") int id, @RequestBody Product product) {
        if(id != product.getProductId())
            throw new InputMismatchException("id mismatch");
        Product response = productService.addProduct(product);

        if (response == null) {
            throw new NoSuchElementFoundException("Price: " + product.getCurrentPrice().getId() + " in the Product doesn't exist");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/price")
    ResponseEntity<?> updatePrice(@PathVariable("id") int id, @RequestBody Price price) {
        if(id != price.getId())
            throw new InputMismatchException("id mismatch");
        Price response = productService.updateProductPrice(price);

        if (response == null) {
            throw new NoSuchElementFoundException("Product: " + id + " not found.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/prices")
    ResponseEntity<?> getAllPrices() {
        List<Price> response = productService.findAll();

        if (response == null) {
            throw new NoSuchElementFoundException("No products available");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/price")
    ResponseEntity<?> getPriceById(@PathVariable("id") int id) {
        Optional<Price> response = productService.findPriceById(id);

        if (!response.isPresent()){
            throw new NoSuchElementFoundException("Product: " + id + " not found.");
        }

        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}/price")
    void deletePriceById(@PathVariable("id") int id) {
       productService.deletePriceById(id);
    }

    @DeleteMapping(value = "/prices")
    void deletePrices() {
        productService.deletePrices();
    }

    @DeleteMapping(value = "/products")
    void deleteProducts() {
        productService.deleteProducts();
    }

    @ExceptionHandler(NoSuchElementFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFoundException(NoSuchElementFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InputMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleInputMismatchException(InputMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleAllUncaughtException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error occurred");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
