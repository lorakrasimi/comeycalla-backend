package com.loraadova.comeycalla.ocr.controller;

import com.loraadova.comeycalla.ocr.RecipeScanService;
import com.loraadova.comeycalla.ocr.dto.RecipeScanResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "http://localhost:4200")
public class RecipeScanController {

    @Autowired
    RecipeScanService recipeScanService;

    public RecipeScanController(RecipeScanService recipeScanService) {
        this.recipeScanService = recipeScanService;
    }

    @PostMapping("/scan-text")
    public ResponseEntity<RecipeScanResponseDto> scanRecipeImages(
            @RequestParam("images") List<MultipartFile> images
    ) {
        RecipeScanResponseDto result = this.recipeScanService.scanImages(images);
        return ResponseEntity.ok(result);
    }
}