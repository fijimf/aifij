package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.quote.InspirationalQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspirationalQuoteRepository extends JpaRepository<InspirationalQuote, Long> {
    List<InspirationalQuote> findByTag(String tag);
    
    List<InspirationalQuote> findByTagIsNull();
    
    @Query(value = "SELECT * FROM inspirational_quote ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<InspirationalQuote> findRandomQuote();
    
    @Query(value = "SELECT * FROM inspirational_quote WHERE tag = ?1 ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<InspirationalQuote> findRandomQuoteByTag(String tag);
}