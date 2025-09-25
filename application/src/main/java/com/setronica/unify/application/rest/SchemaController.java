package com.setronica.unify.application.rest;

import com.setronica.unify.application.dto.ValidationResultDto;
import com.setronica.unify.application.service.SchemaWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("catalog/schema")
public class SchemaController {
    @Autowired
    private SchemaWebService service;

    @GetMapping("{category}")
    public ResponseEntity<String> getSchema(@PathVariable String category) {
        String dto = service.getSchema(category);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("{category}")
    public ResponseEntity<ValidationResultDto> setSchema(@PathVariable String category,
                                                         @RequestBody String schema) {
        ValidationResultDto dto = service.setSchema(category, schema);
        if (dto.isSuccess()) {
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<>(dto, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
