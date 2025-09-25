package com.setronica.unify.application.rest;

import com.setronica.unify.application.dto.ProductDto;
import com.setronica.unify.application.dto.ValidationResultDto;
import com.setronica.unify.application.service.ProductWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("catalog/product")
public class ProductController {
    @Autowired
    private ProductWebService service;

    @GetMapping("{identifier}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String identifier) {
        ProductDto dto = service.getProduct(identifier);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "{identifier}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResultDto> setProduct(@PathVariable String identifier,
                                                          @RequestBody ProductDto content) {
        ValidationResultDto dto = service.setProduct(identifier, content);
        if (dto.isSuccess()) {
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<>(dto, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
