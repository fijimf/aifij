package com.fijimf.deepfij.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fijimf.deepfij.model.quote.InspirationalQuote;
import com.fijimf.deepfij.response.ApiResponse;
import com.fijimf.deepfij.service.InspirationalQuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(InspirationalQuoteController.class)
class InspirationalQuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InspirationalQuoteService quoteService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void getAllQuotes_ShouldReturnAllQuotes() throws Exception {
        when(quoteService.getAllQuotes()).thenReturn(testQuotes);

        mockMvc.perform(get("/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].quote").value("Test quote"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].quote").value("Another test quote"));

        verify(quoteService).getAllQuotes();
    }

    @Test
    void getAllQuotes_ShouldReturnEmptyListWhenNoQuotes() throws Exception {
        when(quoteService.getAllQuotes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(quoteService).getAllQuotes();
    }

    @Test
    void getQuoteById_ShouldReturnQuoteWhenExists() throws Exception {
        when(quoteService.getQuoteById(1L)).thenReturn(Optional.of(testQuote));

        mockMvc.perform(get("/quotes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.quote").value("Test quote"))
                .andExpect(jsonPath("$.data.source").value("Test source"))
                .andExpect(jsonPath("$.data.tag").value("test"));

        verify(quoteService).getQuoteById(1L);
    }

    @Test
    void getQuoteById_ShouldReturn404WhenNotExists() throws Exception {
        when(quoteService.getQuoteById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/quotes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("error"))
                .andExpect(jsonPath("$.message").value("Quote not found"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(quoteService).getQuoteById(1L);
    }

    @Test
    void getQuotesByTag_ShouldReturnQuotesWithSpecificTag() throws Exception {
        List<InspirationalQuote> motivationQuotes = Arrays.asList(testQuotes.get(1));
        when(quoteService.getQuotesByTag("motivation")).thenReturn(motivationQuotes);

        mockMvc.perform(get("/quotes/tag/motivation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].tag").value("motivation"));

        verify(quoteService).getQuotesByTag("motivation");
    }

    @Test
    void getQuotesWithoutTag_ShouldReturnQuotesWithNullTag() throws Exception {
        InspirationalQuote quoteWithoutTag = new InspirationalQuote();
        quoteWithoutTag.setId(3L);
        quoteWithoutTag.setQuote("Quote without tag");
        quoteWithoutTag.setSource("Source");
        quoteWithoutTag.setTag(null);
        
        List<InspirationalQuote> quotesWithoutTag = Arrays.asList(quoteWithoutTag);
        when(quoteService.getQuotesWithoutTag()).thenReturn(quotesWithoutTag);

        mockMvc.perform(get("/quotes/no-tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].tag").isEmpty());

        verify(quoteService).getQuotesWithoutTag();
    }

    @Test
    void getRandomQuote_ShouldReturnRandomQuoteWhenExists() throws Exception {
        when(quoteService.getRandomQuote()).thenReturn(Optional.of(testQuote));

        mockMvc.perform(get("/quotes/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.quote").value("Test quote"));

        verify(quoteService).getRandomQuote();
    }

    @Test
    void getRandomQuote_ShouldReturn404WhenNoQuotes() throws Exception {
        when(quoteService.getRandomQuote()).thenReturn(Optional.empty());

        mockMvc.perform(get("/quotes/random"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("error"))
                .andExpect(jsonPath("$.message").value("No quotes found"));

        verify(quoteService).getRandomQuote();
    }

    @Test
    void getRandomQuoteByTag_ShouldReturnRandomQuoteWithTag() throws Exception {
        when(quoteService.getRandomQuoteByTag("test")).thenReturn(Optional.of(testQuote));

        mockMvc.perform(get("/quotes/random?tag=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.tag").value("test"));

        verify(quoteService).getRandomQuoteByTag("test");
    }

    @Test
    void getRandomQuoteByTag_ShouldReturn404WhenNoQuotesWithTag() throws Exception {
        when(quoteService.getRandomQuoteByTag("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/quotes/random?tag=nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("error"))
                .andExpect(jsonPath("$.message").value("No quotes found with tag: nonexistent"));

        verify(quoteService).getRandomQuoteByTag("nonexistent");
    }

    @Test
    void createQuote_ShouldCreateAndReturnQuote() throws Exception {
        InspirationalQuote newQuote = new InspirationalQuote();
        newQuote.setQuote("New quote");
        newQuote.setSource("New source");
        newQuote.setTag("new");

        InspirationalQuote createdQuote = new InspirationalQuote();
        createdQuote.setId(3L);
        createdQuote.setQuote("New quote");
        createdQuote.setSource("New source");
        createdQuote.setTag("new");

        when(quoteService.createQuote(any(InspirationalQuote.class))).thenReturn(createdQuote);

        mockMvc.perform(post("/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newQuote)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.quote").value("New quote"))
                .andExpect(jsonPath("$.data.source").value("New source"))
                .andExpect(jsonPath("$.data.tag").value("new"));

        verify(quoteService).createQuote(any(InspirationalQuote.class));
    }

    @Test
    void updateQuote_ShouldUpdateAndReturnQuoteWhenExists() throws Exception {
        InspirationalQuote updateData = new InspirationalQuote();
        updateData.setQuote("Updated quote");
        updateData.setSource("Updated source");
        updateData.setTag("updated");

        InspirationalQuote updatedQuote = new InspirationalQuote();
        updatedQuote.setId(1L);
        updatedQuote.setQuote("Updated quote");
        updatedQuote.setSource("Updated source");
        updatedQuote.setTag("updated");

        when(quoteService.updateQuote(eq(1L), any(InspirationalQuote.class))).thenReturn(Optional.of(updatedQuote));

        mockMvc.perform(put("/quotes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.quote").value("Updated quote"))
                .andExpect(jsonPath("$.data.source").value("Updated source"))
                .andExpect(jsonPath("$.data.tag").value("updated"));

        verify(quoteService).updateQuote(eq(1L), any(InspirationalQuote.class));
    }

    @Test
    void updateQuote_ShouldReturn404WhenNotExists() throws Exception {
        InspirationalQuote updateData = new InspirationalQuote();
        updateData.setQuote("Updated quote");
        updateData.setSource("Updated source");
        updateData.setTag("updated");

        when(quoteService.updateQuote(eq(1L), any(InspirationalQuote.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/quotes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("error"))
                .andExpect(jsonPath("$.message").value("Quote not found"));

        verify(quoteService).updateQuote(eq(1L), any(InspirationalQuote.class));
    }

    @Test
    void deleteQuote_ShouldDeleteAndReturnSuccessWhenExists() throws Exception {
        when(quoteService.deleteQuote(1L)).thenReturn(true);

        mockMvc.perform(delete("/quotes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.message").value("Quote deleted successfully"));

        verify(quoteService).deleteQuote(1L);
    }

    @Test
    void deleteQuote_ShouldReturn404WhenNotExists() throws Exception {
        when(quoteService.deleteQuote(1L)).thenReturn(false);

        mockMvc.perform(delete("/quotes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("error"))
                .andExpect(jsonPath("$.message").value("Quote not found"));

        verify(quoteService).deleteQuote(1L);
    }
}