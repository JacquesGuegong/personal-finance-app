package com.financetracker.controller;

import com.financetracker.dto.BudgetAdviceResponse;
import com.financetracker.dto.CategorizeRequest;
import com.financetracker.dto.CategorizeResponse;
import com.financetracker.dto.ReceiptScanResponse;
import com.financetracker.dto.SpendingSummaryResponse;
import com.financetracker.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/**
 * HTTP layer for the AI features. Per the project's architecture rule, this class
 * does NO business logic — it only maps requests to {@link AiService} calls and
 * wraps the results in response DTOs.
 *
 * Every endpoint is authenticated (SecurityConfig protects everything except
 * /api/auth/**). We read the logged-in user's id from the JWT principal exactly
 * like the other controllers do.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // GET /api/ai/spending-summary
    @GetMapping("/spending-summary")
    public ResponseEntity<SpendingSummaryResponse> spendingSummary(Principal principal) {
        String summary = aiService.spendingSummary(userId(principal));
        return ResponseEntity.ok(new SpendingSummaryResponse(summary));
    }

    // POST /api/ai/categorize  body: { "description": "Netflix 15.99" }
    @PostMapping("/categorize")
    public ResponseEntity<CategorizeResponse> categorize(@Valid @RequestBody CategorizeRequest request) {
        String category = aiService.categorize(request.description());
        return ResponseEntity.ok(new CategorizeResponse(category));
    }

    // GET /api/ai/budget-advice
    @GetMapping("/budget-advice")
    public ResponseEntity<BudgetAdviceResponse> budgetAdvice(Principal principal) {
        String advice = aiService.budgetAdvice(userId(principal));
        return ResponseEntity.ok(new BudgetAdviceResponse(advice));
    }

    // The image formats Claude's vision API accepts — anything else is rejected
    // here, before we waste a network call.
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp", "image/gif");

    // Anthropic caps images at 5 MB; enforcing it ourselves gives the client a
    // clear 400 instead of a confusing upstream error.
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    // POST /api/ai/scan-receipt — multipart upload, field name "file".
    // Returns a DRAFT transaction; nothing is saved until the user confirms it
    // through the normal POST /api/transactions.
    @PostMapping(value = "/scan-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptScanResponse> scanReceipt(@RequestParam("file") MultipartFile file) {
        // Input checks are HTTP-shape concerns, so they live here, not in the
        // service. Each failure throws IllegalArgumentException → 400 with a
        // message that tells the client exactly what to fix.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No image uploaded. Send the photo as form field 'file'.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported image type '" + contentType + "'. Use JPEG, PNG, WebP or GIF.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image is too large. Maximum size is 5 MB.");
        }

        try {
            return ResponseEntity.ok(aiService.scanReceipt(file.getBytes(), contentType));
        } catch (IOException e) {
            // Reading an in-memory upload basically can't fail; if it somehow does,
            // it's a server problem → let the 500 catch-all handle it.
            throw new UncheckedIOException("Failed to read uploaded image", e);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
