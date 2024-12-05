package com.feefo.title_normaliser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SynonymMapperTest {

  private SynonymMapper synonymMapper;

  @BeforeEach
  void setUp() {
    synonymMapper = new SynonymMapper();
  }

  @Nested
  @DisplayName("Tests for getNormalisedTitles()")
  class GetNormalisedTitlesTests {

    @Test
    @DisplayName("Should return all titles")
    void shouldReturnAllTitles() {
      // Arrange
      Set<String> expectedTitles = Set.of("Software Engineer", "Architect", "Accountant", "Quantity Surveyor");

      // Act
      Set<String> result = synonymMapper.getNormalisedTitles();

      // Assert
      assertEquals(expectedTitles, result);
    }
  }

  @Nested
  @DisplayName("Tests for addMapping()")
  class AddMappingTests {

    @Test
    @DisplayName("Should add new mapping correctly")
    void shouldAddNewMapping() {
      // Arrange
      String newTitle = "Data Scientist";
      List<String> newSynonyms = List.of("data analyst", "data engineer");

      // Act
      synonymMapper.addMapping(newTitle, newSynonyms);

      // Assert
      assertTrue(synonymMapper.getTitlesForToken("data analyst").contains("Data Scientist"));
      assertTrue(synonymMapper.getTitlesForToken("data engineer").contains("Data Scientist"));
    }

    @Test
    @DisplayName("Should not override existing titles")
    void shouldNotOverrideExistingTitles() {
      // Arrange
      String newTitle = "Accountant";
      List<String> newSynonyms = List.of("financial consultant");

      // Act
      synonymMapper.addMapping(newTitle, newSynonyms);

      // Assert
      assertTrue(synonymMapper.getTitlesForToken("financial consultant").contains("Accountant"));
    }
  }

  @Nested
  @DisplayName("Tests for getTitlesForToken()")
  class GetTitlesForTokenTests {

    @Test
    @DisplayName("Should return titles for valid synonym")
    void shouldReturnTitlesForSynonym() {
      // Act
      Set<String> result = synonymMapper.getTitlesForToken("java");

      // Assert
      assertTrue(result.contains("Software Engineer"));
    }

    @Test
    @DisplayName("Should return empty for non-existent token")
    void shouldReturnEmptyForNonExistentToken() {
      // Act
      Set<String> result = synonymMapper.getTitlesForToken("unknown");

      // Assert
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("Tests for clean()")
  class CleanTests {

    @Test
    @DisplayName("Should clean string by removing punctuation and converting to lowercase")
    void shouldCleanStringByRemovingPunctuation() {
      // Act
      synonymMapper.setShouldCleanSpecialChars(true);
      String result = synonymMapper.clean("  Java, C# @!  ");

      // Assert
      assertEquals("java c", result);
    }

    @Test
    @DisplayName("Should return empty string for only punctuation")
    void shouldReturnEmptyForOnlyPunctuation() {
      // Act
      synonymMapper.setShouldCleanSpecialChars(true);
      String result = synonymMapper.clean("!@#$$%^&*()");

      // Assert
      assertEquals("", result);
    }
  }

  @Nested
  @DisplayName("Tests for synonym to title index initialization")
  class InitialiseSynonymToTitleTests {

    @Test
    @DisplayName("Should initialize synonym-to-title mapping correctly")
    void shouldInitializeSynonymToTitleMappingCorrectly() {
      // Act
      Set<String> titlesForJava = synonymMapper.getTitlesForToken("java");

      // Assert
      assertTrue(titlesForJava.contains("Software Engineer"));
    }

    @Test
    @DisplayName("Should return empty set for unmapped token")
    void shouldReturnEmptySetForUnmappedToken() {
      // Act
      Set<String> titlesForNonExistentToken = synonymMapper.getTitlesForToken("nonexistent");

      // Assert
      assertTrue(titlesForNonExistentToken.isEmpty());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should return empty set for empty token")
    void shouldReturnEmptyForEmptyToken() {
      // Act
      Set<String> result = synonymMapper.getTitlesForToken("");

      // Assert
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty set for null token")
    void shouldReturnEmptyForNullToken() {
      // Act
      Set<String> result = synonymMapper.getTitlesForToken(null);

      // Assert
      assertTrue(result.isEmpty());
    }
  }
}
