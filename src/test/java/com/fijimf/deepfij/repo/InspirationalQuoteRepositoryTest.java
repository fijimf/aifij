package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.quote.InspirationalQuote;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InspirationalQuoteRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private InspirationalQuoteRepository quoteRepository;

    @Test
    public void testInsertQuote_HappyPath() {
        int initialSize = quoteRepository.findAll().size();
        InspirationalQuote quote = createDummyQuote();
        quote = quoteRepository.save(quote);
        assertThat(quote.getId()).isGreaterThan(0L);
        assertThat(quoteRepository.findById(quote.getId())).isPresent();
        assertThat(quoteRepository.findAll()).hasSize(initialSize + 1);
    }

    @Test
    public void testInsertQuote_WithTag() {
        InspirationalQuote quote = createDummyQuote();
        quote.setTag("motivation");
        quote = quoteRepository.save(quote);
        assertThat(quote.getId()).isGreaterThan(0L);
        assertThat(quote.getTag()).isEqualTo("motivation");
    }

    @Test
    public void testInsertQuote_WithoutTag() {
        InspirationalQuote quote = createDummyQuote();
        quote.setTag(null);
        quote = quoteRepository.save(quote);
        assertThat(quote.getId()).isGreaterThan(0L);
        assertThat(quote.getTag()).isNull();
    }

    @Test
    public void testBadInserts_Validation() {
        InspirationalQuote q1 = createDummyQuote();
        q1.setQuote("");
        assertThatThrownBy(() -> quoteRepository.save(q1))
                .isInstanceOf(ConstraintViolationException.class);

        InspirationalQuote q2 = createDummyQuote();
        q2.setQuote(null);
        assertThatThrownBy(() -> quoteRepository.save(q2))
                .isInstanceOf(ConstraintViolationException.class);

        InspirationalQuote q3 = createDummyQuote();
        q3.setSource("");
        assertThatThrownBy(() -> quoteRepository.save(q3))
                .isInstanceOf(ConstraintViolationException.class);

        InspirationalQuote q4 = createDummyQuote();
        q4.setSource(null);
        assertThatThrownBy(() -> quoteRepository.save(q4))
                .isInstanceOf(ConstraintViolationException.class);

        InspirationalQuote q5 = createDummyQuote();
        q5.setSource("x".repeat(256)); // Exceeds max length
        assertThatThrownBy(() -> quoteRepository.save(q5))
                .isInstanceOf(ConstraintViolationException.class);

        InspirationalQuote q6 = createDummyQuote();
        q6.setTag("x".repeat(101)); // Exceeds max length
        assertThatThrownBy(() -> quoteRepository.save(q6))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testFindByTag() {
        InspirationalQuote quote1 = createDummyQuote();
        quote1.setTag("motivation");
        quote1.setQuote("First motivational quote");
        quoteRepository.save(quote1);

        InspirationalQuote quote2 = createDummyQuote();
        quote2.setTag("motivation");
        quote2.setQuote("Second motivational quote");
        quoteRepository.save(quote2);

        InspirationalQuote quote3 = createDummyQuote();
        quote3.setTag("wisdom");
        quote3.setQuote("Wisdom quote");
        quoteRepository.save(quote3);

        List<InspirationalQuote> motivationQuotes = quoteRepository.findByTag("motivation");
        assertThat(motivationQuotes).hasSize(2);
        assertThat(motivationQuotes).allMatch(q -> "motivation".equals(q.getTag()));

        List<InspirationalQuote> wisdomQuotes = quoteRepository.findByTag("wisdom");
        assertThat(wisdomQuotes).hasSize(1);
        assertThat(wisdomQuotes.get(0).getQuote()).isEqualTo("Wisdom quote");

        List<InspirationalQuote> nonExistentTag = quoteRepository.findByTag("nonexistent");
        assertThat(nonExistentTag).isEmpty();
    }

    @Test
    public void testFindByTagIsNull() {
        int initialNullTagCount = quoteRepository.findByTagIsNull().size();
        
        InspirationalQuote quote1 = createDummyQuote();
        quote1.setTag(null);
        quote1.setQuote("Quote without tag");
        quoteRepository.save(quote1);

        InspirationalQuote quote2 = createDummyQuote();
        quote2.setTag("motivation");
        quote2.setQuote("Quote with tag");
        quoteRepository.save(quote2);

        List<InspirationalQuote> quotesWithoutTag = quoteRepository.findByTagIsNull();
        assertThat(quotesWithoutTag).hasSize(initialNullTagCount + 1);
        assertThat(quotesWithoutTag.stream().anyMatch(q -> "Quote without tag".equals(q.getQuote()))).isTrue();
    }

    @Test
    public void testFindRandomQuote() {
        // Since we have existing data, just verify a random quote is returned
        Optional<InspirationalQuote> randomQuote = quoteRepository.findRandomQuote();
        assertThat(randomQuote).isPresent();
        assertThat(randomQuote.get().getQuote()).isNotBlank();
    }

    @Test
    public void testFindRandomQuote_EmptyTable() {
        // Clear all quotes to test empty table scenario
        quoteRepository.deleteAll();
        Optional<InspirationalQuote> randomQuote = quoteRepository.findRandomQuote();
        assertThat(randomQuote).isEmpty();
    }

    @Test
    public void testFindRandomQuoteByTag() {
        InspirationalQuote quote1 = createDummyQuote();
        quote1.setTag("motivation");
        quote1.setQuote("Motivational quote 1");
        quoteRepository.save(quote1);

        InspirationalQuote quote2 = createDummyQuote();
        quote2.setTag("motivation");
        quote2.setQuote("Motivational quote 2");
        quoteRepository.save(quote2);

        InspirationalQuote quote3 = createDummyQuote();
        quote3.setTag("wisdom");
        quote3.setQuote("Wisdom quote");
        quoteRepository.save(quote3);

        Optional<InspirationalQuote> randomMotivationQuote = quoteRepository.findRandomQuoteByTag("motivation");
        assertThat(randomMotivationQuote).isPresent();
        assertThat(randomMotivationQuote.get().getTag()).isEqualTo("motivation");
        assertThat(randomMotivationQuote.get().getQuote()).isIn("Motivational quote 1", "Motivational quote 2");

        Optional<InspirationalQuote> randomWisdomQuote = quoteRepository.findRandomQuoteByTag("wisdom");
        assertThat(randomWisdomQuote).isPresent();
        assertThat(randomWisdomQuote.get().getQuote()).isEqualTo("Wisdom quote");

        Optional<InspirationalQuote> randomNonExistentTag = quoteRepository.findRandomQuoteByTag("nonexistent");
        assertThat(randomNonExistentTag).isEmpty();
    }

    @Test
    public void testFindRandomQuoteByTag_EmptyTable() {
        // Test with a non-existent tag
        Optional<InspirationalQuote> randomQuote = quoteRepository.findRandomQuoteByTag("nonexistent-tag-12345");
        assertThat(randomQuote).isEmpty();
    }

    private static @NotNull InspirationalQuote createDummyQuote() {
        InspirationalQuote quote = new InspirationalQuote();
        quote.setQuote("This is a test quote");
        quote.setSource("Test Source");
        quote.setTag("test");
        return quote;
    }
}