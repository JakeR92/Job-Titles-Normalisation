package com.feefo.title_normaliser;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Normaliser Tests")
class NormaliserTest {

  @Nested
  @DisplayName("When valid synonyms and titles are provided")
  class ValidNormalisationTests {

    @Test
    @DisplayName("Should normalise valid synonyms to the correct title based on scoring")
    void shouldNormaliseValidSynonymToCorrectTitle() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));

      // Act
      Optional<String> normalised1 = normaliser.normalise("developer");
      Optional<String> normalised2 = normaliser.normalise("bookkeeper");

      // Assert
      assertThat(normalised1).isEqualTo(Optional.of("Software Engineer"));
      assertThat(normalised2).isEqualTo(Optional.of("Accountant"));
    }

    @Test
    @DisplayName("Should prioritize exact title word matches over synonyms")
    void shouldPrioritizeExactTitleMatchOverSynonyms() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Engineer", Set.of("technician", "operator")
      ));

      // Act
      Optional<String> result = normaliser.normalise("Engineer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Engineer"));
    }

    @Test
    @DisplayName("Should normalise synonyms with punctuation to correct title")
    void shouldNormaliseSynonymsWithPunctuation() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));

      // Act
      Optional<String> normalised = normaliser.normalise("developer coder!");

      // Assert
      assertThat(normalised).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should normalise mixed case input")
    void shouldNormaliseMixedCaseInput() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder")
      ));

      // Act
      Optional<String> normalised = normaliser.normalise("DeVelOper");

      // Assert
      assertThat(normalised).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should match partial titles")
    void shouldMatchPartialTitles() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Engineer", Set.of("technician")
      ));

      // Act
      Optional<String> result = normaliser.normalise("sda engineer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Engineer"));
    }
  }

  @Nested
  @DisplayName("When input has unknown or no synonyms")
  class UnknownOrNoSynonymsTests {

    @Test
    @DisplayName("Should return 'No Match' for unknown title")
    void shouldReturnNoMatchForUnknownTitle() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder")
      ));

      // Act
      Optional<String> result = normaliser.normalise("unknown");

      // Assert
      assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should return 'No Match' for empty synonym set")
    void shouldReturnNoMatchForEmptySynonymSet() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of()
      ));

      // Act
      Optional<String> result = normaliser.normalise("dev");

      // Assert
      assertThat(result).isEqualTo(Optional.empty());
    }
  }

  @Nested
  @DisplayName("When input is null or empty")
  class NullOrEmptyInputTests {

    @Test
    @DisplayName("Should return 'No Match' when input is null")
    void shouldReturnNoMatchWhenInputIsNull() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder")
      ));

      // Act
      Optional<String> result = normaliser.normalise(null);

      // Assert
      assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should return 'No Match' when input is empty")
    void shouldReturnNoMatchWhenInputIsEmpty() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder")
      ));

      // Act
      Optional<String> result = normaliser.normalise("");

      // Assert
      assertThat(result).isEqualTo(Optional.empty());
    }
  }

  @Nested
  @DisplayName("When input contains multiple valid tokens")
  class MultipleTokensTests {

    @Test
    @DisplayName("Should select title with highest score")
    void shouldSelectTitleWithHighestScore() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Engineer", Set.of()
      ));

      // Act
      Optional<String> result = normaliser.normalise("engineer developer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer")); // Developer scores higher overall
    }
  }

  @Nested
  @DisplayName("When multiple conditions are met")
  class ComplexScenarioTests {

    @Test
    @DisplayName("Should handle synonym and partial title match together")
    void shouldHandleSynonymAndPartialTitleMatch() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Engineer", Set.of("technician", "operator")
      ));

      // Act
      Optional<String> result = normaliser.normalise("developer engineer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should handle give consecutive partial title matches bonus score")
    void shouldHandleSynonymAndPartialMatchAcrossMultipleTitles() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Architect", Set.of("developer", "coder"),
        "Engineer", Set.of("technician", "operator"),
        "Code Reviewer", Set.of("tcl", "Prolog", "perl", "VB")
      ));

      // Act
      Optional<String> result = normaliser.normalise("software Architect - tcl prolog perl vb");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Architect"));
    }

    @Test
    @DisplayName("Should ignore extra words")
    void shouldRemoveUnnecessaryGrammarBeforeMatching() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Engineer", Set.of("technician")
      ));

      // Act
      Optional<String> result = normaliser.normalise("the best developer ever");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }
  }

  @Nested
  @DisplayName("When multiple valid synonyms lead to different titles")
  class MultipleValidSynonymsTests {

    @Test
    @DisplayName("Should return 'Database' if Manager' is a synonym for both but scores higher")
    void shouldReturnSoftwareEngineerForCommonSynonym() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Senior Software Engineer", Set.of("Manager", "coder"),
        "Database", Set.of("Manager", "bookkeeper")
      ));

      // Act
      Optional<String> result = normaliser.normalise("Database Manager");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Database"));      }

    @Test
    @DisplayName("Should handle case where multiple valid synonyms apply with similar scoring")
    void shouldHandleMultipleValidSynonymsWithSimilarScoring() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Programmer", Set.of("coder", "developer")
      ));

      // Act
      Optional<String> result = normaliser.normalise("developer coder");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Programmer"));
    }
  }

  @Nested
  @DisplayName("When input contains punctuation characters")
  class PunctuationCleaningTests {

    @Test
    @DisplayName("Should clean punctuation from input before normalising")
    void shouldCleanPunctuationBeforeNormalising() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.shouldCleanSpecialChars(true);  // Enable punctuation cleaning

      // Act
      Optional<String> result = normaliser.normalise("developer, coder!");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));  // Punctuation is cleaned, and "developer" matches
    }

    @Test
    @DisplayName("Should clean punctuation and return correct title when input has trailing punctuation")
    void shouldCleanPunctuationAndReturnCorrectTitleWithTrailingPunctuation() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.shouldCleanSpecialChars(true);  // Enable punctuation cleaning

      // Act
      Optional<String> result = normaliser.normalise("coder! developer.");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));  // Punctuation cleaned and synonym "coder" matches
    }

    @Test
    @DisplayName("Should clean punctuation from input with multiple punctuations and return correct title")
    void shouldCleanPunctuationFromInputWithMultiplePunctuations() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.shouldCleanSpecialChars(true);  // Enable punctuation cleaning

      // Act
      Optional<String> result = normaliser.normalise("!developer, @coder#");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));  // Punctuation is cleaned, and "developer" matches
    }

    @Test
    @DisplayName("Should clean punctuation and handle case insensitivity")
    void shouldCleanPunctuationAndHandleCaseInsensitivity() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder")
      ));
      normaliser.shouldCleanSpecialChars(true);  // Enable punctuation cleaning

      // Act
      Optional<String> result = normaliser.normalise("DeVeLoPer!");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));  // Case-insensitive match after cleaning punctuation
    }

    @Test
    @DisplayName("Should clean punctuation from input and match partial titles correctly")
    void shouldCleanPunctuationAndMatchPartialTitles() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.shouldCleanSpecialChars(true);  // Enable punctuation cleaning

      // Act
      Optional<String> result = normaliser.normalise("the best coder, developer...");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));  // "coder" and "developer" match after cleaning punctuation
    }
  }

  @Nested
  @DisplayName("When input contains typos")
  class TypoTests {

    @Test
    @DisplayName("Should normalise title even with minor typos")
    void shouldNormaliseWithMinorTypos() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.setAllowTypos(true);  // Enable typo matching

      // Act
      Optional<String> result = normaliser.normalise("engneer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should normalise title even with medium typos")
    void shouldNormaliseWithMediumTypo() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.setAllowTypos(true);  // Enable typo matching

      // Act
      Optional<String> result = normaliser.normalise("sftwre");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should not match if typo is too severe")
    void shouldNotMatchWithSevereTypos() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.setAllowTypos(true);  // Enable typo matching

      // Act
      Optional<String> result = normaliser.normalise("sofwise engonoor");

      // Assert
      assertThat(result).isEqualTo(Optional.empty());  // Too far from any valid title
    }

    @Test
    @DisplayName("Should not handle common spelling mistakes in synonyms")
    void shouldHandleCommonSpellingMistakesInSynonyms() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Software Engineer", Set.of("developer", "coder"),
        "Accountant", Set.of("bookkeeper", "finance")
      ));
      normaliser.setAllowTypos(true);  // Enable typo matching

      // Act
      Optional<String> result = normaliser.normalise("finace");

      // Assert
      assertThat(result).isEqualTo(Optional.empty());  // We only match partial titles with typos
    }
  }

  @Nested
  @DisplayName("When using the default Normaliser")
  class DefaultNormaliserTests {

    @Test
    @DisplayName("Should normalise a single matching synonym to its default title")
    void shouldNormaliseSingleMatchingSynonym() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("java");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should normalise multiple matching synonyms to the same default title")
    void shouldNormaliseMultipleMatchingSynonymsToSameTitle() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("programmer coder");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should prefer the title with the most matches")
    void shouldPreferTitleWithMostMatches() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("coder architect designer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Architect"));
    }

    @Test
    @DisplayName("Should handle titles with unique synonyms correctly")
    void shouldHandleUniqueSynonyms() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("financial bookkeeper");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Accountant"));
    }

    @Test
    @DisplayName("Should normalise to Quantity Surveyor for construction-related terms")
    void shouldNormaliseToQuantitySurveyor() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("surveyor construction");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Quantity Surveyor"));
    }

    @Test
    @DisplayName("Should return empty result for no matches")
    void shouldReturnEmptyForNoMatches() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("artist musician");

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should normalise based on consecutive matches in default mappings")
    void shouldNormaliseBasedOnConsecutiveMatches() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("programmer developer");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }

    @Test
    @DisplayName("Should ignore irrelevant words and normalise based on matches")
    void shouldIgnoreIrrelevantWords() {
      // Arrange
      Normaliser normaliser = new Normaliser();

      // Act
      Optional<String> result = normaliser.normalise("expert programmer in java with experience");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Software Engineer"));
    }
  }

  @Nested
  @DisplayName("Partial Title Consecutive Bonus")
  class PartialTitleConsecutiveBonus {

    @Test
    @DisplayName("Should match multiple consecutive partial titles")
    void shouldMatchMultipleConsecutiveTitles() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Senior Full Stack Software Engineer specializing in Cloud Computing", Set.of("developer", "coder"),
        "Software Engineer", Set.of("programmer", "full", "stack", "cloud"),
        "Cloud Computing Specialist", Set.of("cloud", "computing")
      ));

      // Act
      Optional<String> result = normaliser.normalise("Senior Full Stack Software Engineer with Cloud Computing expertise");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Senior Full Stack Software Engineer specializing in Cloud Computing"));
    }

    @Test
    @DisplayName("Should match multiple consecutive partial titles - beats lots of synonyms")
    void shouldMatchMultipleConsecutiveTitles_beats_synonyms() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Senior Full Stack Software Engineer specializing in Cloud Computing", Set.of("developer", "coder"),
        "Software Engineer", Set.of("programmer", "full", "stack", "cloud"),
        "Cloud Computing Specialist", Set.of("senior", "cloud", "computing", "full", "stack", "engineer", "expertise")
      ));

      // Act
      Optional<String> result = normaliser.normalise("Senior Full Stack Software Engineer with Cloud Computing expertise");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Senior Full Stack Software Engineer specializing in Cloud Computing"));
    }

    @Test
    @DisplayName("Should match consecutive partial titles with typos")
    void shouldMatchConsecutiveTitlesWithTypos() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Senior Full Stack Software Engineer specializing in Cloud Computing", Set.of("developer", "coder"),
        "Software Engineer", Set.of("programmer", "full", "stack", "cloud"),
        "Cloud Computing Specialist", Set.of("cloud", "computing")
      ));
      normaliser.setAllowTypos(true);  // Enable typo matching

      // Act
      Optional<String> result = normaliser.normalise("Senior Ful Stack Sofware Engineer with Clud Computing expertise");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Senior Full Stack Software Engineer specializing in Cloud Computing"));
    }

    @Test
    @DisplayName("Should match multiple seperate consecutive partial titles")
    void shouldMatchMultipleSeperateConsecutiveTitles() {
      // Arrange
      Normaliser normaliser = new Normaliser(Map.of(
        "Senior Full Stack Software Engineer specializing in Cloud Computing", Set.of("developer", "coder"),
        "Software Engineer", Set.of("programmer", "full", "stack", "cloud"),
        "Cloud Computing Specialist", Set.of("cloud", "computing")
      ));

      // Act
      Optional<String> result = normaliser.normalise("New! Senior Full Stck Software Engineer with Cloud Computing expertise");

      // Assert
      assertThat(result).isEqualTo(Optional.of("Senior Full Stack Software Engineer specializing in Cloud Computing"));
    }
  }
}
