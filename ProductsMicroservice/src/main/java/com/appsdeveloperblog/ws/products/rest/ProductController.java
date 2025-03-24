package com.appsdeveloperblog.ws.products.rest;

import com.appsdeveloperblog.ws.products.dto.ProductCreateDto;
import com.appsdeveloperblog.ws.products.error.ErrorMessage;
import com.appsdeveloperblog.ws.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(dto));
    }

    @PostMapping("/sync")
    public ResponseEntity<Object> createProductSync(@RequestBody ProductCreateDto dto) {
        String result;
        try {
            result = productService.createProductSync(dto);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(new Date(), e.getMessage(), "/products/sync"));
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

}
