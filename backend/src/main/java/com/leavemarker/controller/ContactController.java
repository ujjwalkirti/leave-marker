package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.contact.ContactEmailRequest;
import com.leavemarker.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendContactEmail(
            @Valid @RequestBody ContactEmailRequest request) {
        log.info("Received contact form submission from: {}", request.getEmail());

        emailService.sendContactEmail(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getMessage()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Message sent successfully. We'll get back to you soon!"));
    }
}
