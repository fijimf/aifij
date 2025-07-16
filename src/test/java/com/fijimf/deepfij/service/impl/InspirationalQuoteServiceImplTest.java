package com.fijimf.deepfij.service.impl;

import com.fijimf.deepfij.model.quote.InspirationalQuote;
import com.fijimf.deepfij.repo.InspirationalQuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspirationalQuoteServiceImplTest {

    @Mock
    private InspirationalQuoteRepository repository;

    @InjectMocks
    private InspirationalQuoteServiceImpl quoteService;

    private InspirationalQuote testQuote;
    private List<InspirationalQuote> testQuotes;

    @BeforeEach
    void setUp() {
        testQuote = new InspirationalQuote();
        testQuote.setId(1L);
        testQuote.setQuote("Test quote");
        testQuote.setSource("Test source");
        testQuote.setTag("test");

        InspirationalQuote quote2 = new InspirationalQuote();
        quote2.setId(2L);
        quote2.setQuote("Another test quote");
        quote2.setSource("Another source");
        quote2.setTag("motivation");

        testQuotes = Arrays.asList(testQuote, quote2);
    }

    @Test
    void getAllQuotes_ShouldReturnAllQuotes() {
        when(repository.findAll()).thenReturn(testQuotes);

        List<InspirationalQuote> result = quoteService.getAllQuotes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testQuotes, result);
        verify(repository).findAll();
    }

    @Test
    void getAllQuotes_ShouldReturnEmptyListWhenNoQuotes() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<InspirationalQuote> result = quoteService.getAllQuotes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void getQuoteById_ShouldReturnQuoteWhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(testQuote));

        Optional<InspirationalQuote> result = quoteService.getQuoteById(1L);

        assertTrue(result.isPresent());
        assertEquals(testQuote, result.get());
        verify(repository).findById(1L);
    }

    @Test
    void getQuoteById_ShouldReturnEmptyWhenNotExists() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<InspirationalQuote> result = quoteService.getQuoteById(1L);

        assertFalse(result.isPresent());
        verify(repository).findById(1L);
    }

    @Test
    void getQuotesByTag_ShouldReturnQuotesWithSpecificTag() {
        List<InspirationalQuote> motivationQuotes = Arrays.asList(testQuotes.get(1));
        when(repository.findByTag("motivation")).thenReturn(motivationQuotes);

        List<InspirationalQuote> result = quoteService.getQuotesByTag("motivation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("motivation", result.get(0).getTag());
        verify(repository).findByTag("motivation");
    }

    @Test
    void getQuotesByTag_ShouldReturnEmptyListWhenNoQuotesWithTag() {
        when(repository.findByTag("nonexistent")).thenReturn(Collections.emptyList());

        List<InspirationalQuote> result = quoteService.getQuotesByTag("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findByTag("nonexistent");
    }

    @Test
    void getQuotesWithoutTag_ShouldReturnQuotesWithNullTag() {
        InspirationalQuote quoteWithoutTag = new InspirationalQuote();
        quoteWithoutTag.setQuote("Quote without tag");
        quoteWithoutTag.setSource("Source");
        quoteWithoutTag.setTag(null);
        
        List<InspirationalQuote> quotesWithoutTag = Arrays.asList(quoteWithoutTag);
        when(repository.findByTagIsNull()).thenReturn(quotesWithoutTag);

        List<InspirationalQuote> result = quoteService.getQuotesWithoutTag();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getTag());
        verify(repository).findByTagIsNull();
    }

    @Test
    void getRandomQuote_ShouldReturnRandomQuoteWhenExists() {
        when(repository.findRandomQuote()).thenReturn(Optional.of(testQuote));

        Optional<InspirationalQuote> result = quoteService.getRandomQuote();

        assertTrue(result.isPresent());
        assertEquals(testQuote, result.get());
        verify(repository).findRandomQuote();
    }

    @Test
    void getRandomQuote_ShouldReturnEmptyWhenNoQuotes() {
        when(repository.findRandomQuote()).thenReturn(Optional.empty());

        Optional<InspirationalQuote> result = quoteService.getRandomQuote();

        assertFalse(result.isPresent());
        verify(repository).findRandomQuote();
    }

    @Test
    void getRandomQuoteByTag_ShouldReturnRandomQuoteWithTag() {
        when(repository.findRandomQuoteByTag("test")).thenReturn(Optional.of(testQuote));

        Optional<InspirationalQuote> result = quoteService.getRandomQuoteByTag("test");

        assertTrue(result.isPresent());
        assertEquals(testQuote, result.get());
        verify(repository).findRandomQuoteByTag("test");
    }

    @Test
    void getRandomQuoteByTag_ShouldReturnEmptyWhenNoQuotesWithTag() {
        when(repository.findRandomQuoteByTag("nonexistent")).thenReturn(Optional.empty());

        Optional<InspirationalQuote> result = quoteService.getRandomQuoteByTag("nonexistent");

        assertFalse(result.isPresent());
        verify(repository).findRandomQuoteByTag("nonexistent");
    }

    @Test
    void createQuote_ShouldSaveAndReturnQuote() {
        InspirationalQuote newQuote = new InspirationalQuote();
        newQuote.setQuote("New quote");
        newQuote.setSource("New source");
        newQuote.setTag("new");

        InspirationalQuote savedQuote = new InspirationalQuote();
        savedQuote.setId(3L);
        savedQuote.setQuote("New quote");
        savedQuote.setSource("New source");
        savedQuote.setTag("new");

        when(repository.save(newQuote)).thenReturn(savedQuote);

        InspirationalQuote result = quoteService.createQuote(newQuote);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New quote", result.getQuote());
        assertEquals("New source", result.getSource());
        assertEquals("new", result.getTag());
        verify(repository).save(newQuote);
    }

    @Test
    void updateQuote_ShouldUpdateAndReturnQuoteWhenExists() {
        InspirationalQuote updateData = new InspirationalQuote();
        updateData.setQuote("Updated quote");
        updateData.setSource("Updated source");
        updateData.setTag("updated");

        InspirationalQuote updatedQuote = new InspirationalQuote();
        updatedQuote.setId(1L);
        updatedQuote.setQuote("Updated quote");
        updatedQuote.setSource("Updated source");
        updatedQuote.setTag("updated");

        when(repository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(repository.save(any(InspirationalQuote.class))).thenReturn(updatedQuote);

        Optional<InspirationalQuote> result = quoteService.updateQuote(1L, updateData);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Updated quote", result.get().getQuote());
        assertEquals("Updated source", result.get().getSource());
        assertEquals("updated", result.get().getTag());
        verify(repository).findById(1L);
        verify(repository).save(any(InspirationalQuote.class));
    }

    @Test
    void updateQuote_ShouldReturnEmptyWhenNotExists() {
        InspirationalQuote updateData = new InspirationalQuote();
        updateData.setQuote("Updated quote");
        updateData.setSource("Updated source");
        updateData.setTag("updated");

        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<InspirationalQuote> result = quoteService.updateQuote(1L, updateData);

        assertFalse(result.isPresent());
        verify(repository).findById(1L);
        verify(repository, never()).save(any(InspirationalQuote.class));
    }

    @Test
    void deleteQuote_ShouldReturnTrueWhenQuoteExists() {
        when(repository.existsById(1L)).thenReturn(true);

        boolean result = quoteService.deleteQuote(1L);

        assertTrue(result);
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteQuote_ShouldReturnFalseWhenQuoteNotExists() {
        when(repository.existsById(1L)).thenReturn(false);

        boolean result = quoteService.deleteQuote(1L);

        assertFalse(result);
        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(anyLong());
    }
}