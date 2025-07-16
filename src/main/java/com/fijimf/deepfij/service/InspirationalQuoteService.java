package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.quote.InspirationalQuote;

import java.util.List;
import java.util.Optional;

public interface InspirationalQuoteService {
    /**
     * Retrieves all inspirational quotes.
     *
     * @return List of all quotes
     */
    List<InspirationalQuote> getAllQuotes();

    /**
     * Retrieves a quote by its ID.
     *
     * @param id The ID of the quote
     * @return Optional containing the quote if found
     */
    Optional<InspirationalQuote> getQuoteById(Long id);

    /**
     * Retrieves quotes by tag.
     *
     * @param tag The tag to filter by
     * @return List of quotes with the specified tag
     */
    List<InspirationalQuote> getQuotesByTag(String tag);

    /**
     * Retrieves quotes without a tag.
     *
     * @return List of quotes without a tag
     */
    List<InspirationalQuote> getQuotesWithoutTag();

    /**
     * Retrieves a random quote.
     *
     * @return Optional containing a random quote if any exist
     */
    Optional<InspirationalQuote> getRandomQuote();

    /**
     * Retrieves a random quote filtered by tag.
     *
     * @param tag The tag to filter by
     * @return Optional containing a random quote with the specified tag if any exist
     */
    Optional<InspirationalQuote> getRandomQuoteByTag(String tag);

    /**
     * Creates a new inspirational quote.
     *
     * @param quote The quote to create
     * @return The created quote
     */
    InspirationalQuote createQuote(InspirationalQuote quote);

    /**
     * Updates an existing inspirational quote.
     *
     * @param id The ID of the quote to update
     * @param quote The updated quote data
     * @return The updated quote if found
     */
    Optional<InspirationalQuote> updateQuote(Long id, InspirationalQuote quote);

    /**
     * Deletes an inspirational quote by ID.
     *
     * @param id The ID of the quote to delete
     * @return true if the quote was deleted, false if not found
     */
    boolean deleteQuote(Long id);
}