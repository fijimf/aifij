package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.model.quote.InspirationalQuote;
import com.fijimf.deepfij.response.ApiResponse;
import com.fijimf.deepfij.service.InspirationalQuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/quotes")
public class InspirationalQuoteController {
    private static final Logger logger = LoggerFactory.getLogger(InspirationalQuoteController.class);

    private final InspirationalQuoteService quoteService;

    @Autowired
    public InspirationalQuoteController(InspirationalQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InspirationalQuote>>> getAllQuotes() {
        logger.info("Fetching all quotes");
        List<InspirationalQuote> quotes = quoteService.getAllQuotes();
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InspirationalQuote>> getQuoteById(@PathVariable Long id) {
        logger.info("Fetching quote with id: {}", id);
        Optional<InspirationalQuote> quote = quoteService.getQuoteById(id);
        if (quote.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(quote.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Quote not found"));
        }
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<ApiResponse<List<InspirationalQuote>>> getQuotesByTag(@PathVariable String tag) {
        logger.info("Fetching quotes with tag: {}", tag);
        List<InspirationalQuote> quotes = quoteService.getQuotesByTag(tag);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/no-tag")
    public ResponseEntity<ApiResponse<List<InspirationalQuote>>> getQuotesWithoutTag() {
        logger.info("Fetching quotes without tag");
        List<InspirationalQuote> quotes = quoteService.getQuotesWithoutTag();
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<InspirationalQuote>> getRandomQuote(@RequestParam(required = false) String tag) {
        logger.info("Fetching random quote" + (tag != null ? " with tag: " + tag : ""));
        
        Optional<InspirationalQuote> quote;
        if (tag != null && !tag.isEmpty()) {
            quote = quoteService.getRandomQuoteByTag(tag);
        } else {
            quote = quoteService.getRandomQuote();
        }
        
        if (quote.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(quote.get()));
        } else {
            String message = tag != null ? "No quotes found with tag: " + tag : "No quotes found";
            return ResponseEntity.status(404).body(ApiResponse.error(message));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InspirationalQuote>> createQuote(@RequestBody InspirationalQuote quote) {
        logger.info("Creating new quote");
        InspirationalQuote createdQuote = quoteService.createQuote(quote);
        return ResponseEntity.ok(ApiResponse.success(createdQuote));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InspirationalQuote>> updateQuote(@PathVariable Long id, @RequestBody InspirationalQuote quote) {
        logger.info("Updating quote with id: {}", id);
        Optional<InspirationalQuote> updatedQuote = quoteService.updateQuote(id, quote);
        if (updatedQuote.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(updatedQuote.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Quote not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuote(@PathVariable Long id) {
        logger.info("Deleting quote with id: {}", id);
        boolean deleted = quoteService.deleteQuote(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Quote deleted successfully", null));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Quote not found"));
        }
    }
}