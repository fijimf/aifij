package com.fijimf.deepfij.service.impl;

import com.fijimf.deepfij.model.quote.InspirationalQuote;
import com.fijimf.deepfij.repo.InspirationalQuoteRepository;
import com.fijimf.deepfij.service.InspirationalQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InspirationalQuoteServiceImpl implements InspirationalQuoteService {

    private final InspirationalQuoteRepository repository;

    @Autowired
    public InspirationalQuoteServiceImpl(InspirationalQuoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<InspirationalQuote> getAllQuotes() {
        return repository.findAll();
    }

    @Override
    public Optional<InspirationalQuote> getQuoteById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<InspirationalQuote> getQuotesByTag(String tag) {
        return repository.findByTag(tag);
    }

    @Override
    public List<InspirationalQuote> getQuotesWithoutTag() {
        return repository.findByTagIsNull();
    }

    @Override
    public Optional<InspirationalQuote> getRandomQuote() {
        return repository.findRandomQuote();
    }

    @Override
    public Optional<InspirationalQuote> getRandomQuoteByTag(String tag) {
        return repository.findRandomQuoteByTag(tag);
    }

    @Override
    public InspirationalQuote createQuote(InspirationalQuote quote) {
        return repository.save(quote);
    }

    @Override
    public Optional<InspirationalQuote> updateQuote(Long id, InspirationalQuote quote) {
        return repository.findById(id)
                .map(existingQuote -> {
                    existingQuote.setQuote(quote.getQuote());
                    existingQuote.setSource(quote.getSource());
                    existingQuote.setTag(quote.getTag());
                    return repository.save(existingQuote);
                });
    }

    @Override
    public boolean deleteQuote(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}