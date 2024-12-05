package com.feefo.title_normaliser;

import static com.feefo.title_normaliser.ConsecutiveMatchBonusCalculator.calculateConsecutiveMatchBonus;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.JaroWinklerDistance;

/**
 * The {@code Normaliser} class provides a mechanism to normalise input titles by mapping them to their
 * most likely equivalent standard titles using exact matches, synonym matches, and partial matches.
 * It supports cleaning special characters and case insensitivity.
 */
public class Normaliser {

  // Mapper for synonyms and cleaning options
  private final SynonymMapper synonymMapper;

  // Index for partial title matches
  private final Map<String, Set<String>> partialTitleIndex = new HashMap<>();

  // Cache for normalised titles
  private final Map<String, String> normalisedTitlesSet = new HashMap<>();

  // Scoring constants for match types
  public static final int TITLE_MATCH_SCORE = 4;
  private static final int SYNONYM_MATCH_SCORE = 2;
  private static final int TYPO_MATCH_SCORE = 1;

  // Typo tolerance
  private static final double SIMILARITY_THRESHOLD = 0.15;  // Allows for 15% dissimilarity
  private final JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
  private boolean allowTypos = false;

  /**
   * Constructor that initializes the normaliser with a map of normalised titles and their synonyms.
   *
   * @param normalisedMap A map where the key is a normalised title and the value is a set of synonyms for that title.
   */
  public Normaliser(Map<String, Set<String>> normalisedMap) {
    this.synonymMapper = new SynonymMapper(normalisedMap);
    initialiseTitleIndex();
  }

  /**
   * Default constructor that initializes the normaliser with default mappings.
   */
  public Normaliser() {
    this.synonymMapper = new SynonymMapper();
    initialiseTitleIndex();
  }

  /**
   * Configures whether special characters (e.g., punctuation) should be cleaned from input titles.
   *
   * @param shouldCleanSpecialChars Boolean flag indicating whether to clean special characters.
   */
  public void shouldCleanSpecialChars(boolean shouldCleanSpecialChars) {
    synonymMapper.setShouldCleanSpecialChars(shouldCleanSpecialChars);
  }

  /**
   * Sets whether partial title matches should allow typos.
   *
   * @param allowTypos Boolean flag indicating whether to allow typos.
   */
  public void setAllowTypos(boolean allowTypos) {
    this.allowTypos = allowTypos;
  }

  /**
   * Initializes the index for partial title matches by breaking down each title into individual words.
   * These words are indexed for faster partial matching later.
   */
  private void initialiseTitleIndex() {
    partialTitleIndex.clear();
    for (String title : synonymMapper.getNormalisedTitles()) {
      normalisedTitlesSet.put(title.toLowerCase(), title); // Cache lowercase normalised titles
      indexPartialTitleWords(title);  // Index the words for partial matching
    }
  }

  /**
   * Indexes the words of a given title to support partial title matching.
   *
   * @param title The title whose words are to be indexed.
   */
  private void indexPartialTitleWords(String title) {
    String[] words = title.toLowerCase().split("\\s+");
    for (String word : words) {
      partialTitleIndex.computeIfAbsent(word, k -> new HashSet<>()).add(title);
    }
  }

  /**
   * Normalises an input title by matching it against known titles, considering exact matches, synonym matches,
   * and partial title matches. The input is cleaned based on configured settings.
   *
   * @param inputTitle The input title to normalise.
   * @return The normalised title if a match is found, or "No Match" if no suitable match is found.
   */
  public Optional<String> normalise(String inputTitle) {
    if (inputTitle == null || inputTitle.isEmpty()) {
      return Optional.empty();  // Return empty if the input is null or empty
    }

    String cleanedInput = synonymMapper.clean(inputTitle);  // Clean the input based on configured settings
    String exactMatch = findExactMatch(cleanedInput);  // Check for exact match
    if (exactMatch != null) {
      return Optional.of(exactMatch);  // Return the exact match if found
    }

    String[] tokens = cleanedInput.split("\\s+");
    Map<String, Integer> scores = calculateScores(tokens);  // Calculate scores based on tokens

    return findBestMatch(scores);  // Return the best matching title based on calculated scores
  }

  /**
   * Finds an exact match for the cleaned input title.
   *
   * @param cleanedInput The cleaned input title to search for an exact match.
   * @return The exact matched title or null if no match is found.
   */
  private String findExactMatch(String cleanedInput) {
    return normalisedTitlesSet.get(cleanedInput.toLowerCase());
  }

  /**
   * Calculates the scores for each potential match based on input tokens.
   *
   * @param tokens The tokens of the cleaned input title.
   * @return A map of titles and their corresponding match scores.
   */
  private Map<String, Integer> calculateScores(String[] tokens) {
    Map<String, Integer> scores = new HashMap<>();

    // Loop through each token to calculate matches
    for (String token : tokens) {
      matchSynonyms(scores, token);  // Check for synonym matches
      matchPartialTitles(scores, token);  // Check for partial title matches
    }

    calculateConsecutiveMatchBonus(scores, tokens);  // Apply bonus for consecutive matches
    return scores;
  }

  /**
   * Matches the current token against known synonyms and updates the scores.
   *
   * @param scores The map of titles and their current scores.
   * @param token The token to match against known synonyms.
   */
  private void matchSynonyms(Map<String, Integer> scores, String token) {
    Set<String> matchedTitles = synonymMapper.getTitlesForToken(token);  // Get titles matching the token's synonyms
    for (String matchedTitle : matchedTitles) {
      scores.put(matchedTitle, scores.getOrDefault(matchedTitle, 0) + SYNONYM_MATCH_SCORE);  // Update scores
    }
  }

  /**
   * Matches the current token against known partial titles and updates the scores.
   *
   * @param scores The map of titles and their current scores.
   * @param token The token to match against known partial titles.
   */
  private void matchPartialTitles(Map<String, Integer> scores, String token) {
    Set<String> partialMatches = partialTitleIndex.getOrDefault(token, Collections.emptySet());  // Get partial matches

    // If no exact partial matches are found and typos are allowed, apply fuzzy matching
    if (partialMatches.isEmpty() && allowTypos) {
      applyTypoMatches(scores, token);
    } else {
      applyExactPartialMatches(scores, partialMatches);  // Apply exact partial matches if found
    }
  }

  /**
   * Applies typo-based fuzzy matches to the scores if partial matches are not found.
   *
   * @param scores The map of titles and their current scores.
   * @param token The token to match with fuzzy logic.
   */
  private void applyTypoMatches(Map<String, Integer> scores, String token) {
    Set<String> typoMatches = getFuzzyPartialMatches(token);  // Get fuzzy matches using Jaro-Winkler
    for (String typoMatch : typoMatches) {
      scores.put(typoMatch, scores.getOrDefault(typoMatch, 0) + TYPO_MATCH_SCORE);  // Update scores with typo matches
    }
  }

  /**
   * Applies exact partial title matches to the scores.
   *
   * @param scores The map of titles and their current scores.
   * @param partialMatches The set of exact partial title matches.
   */
  private void applyExactPartialMatches(Map<String, Integer> scores, Set<String> partialMatches) {
    for (String typoMatch : partialMatches) {
      scores.put(typoMatch, scores.getOrDefault(typoMatch, 0) + TITLE_MATCH_SCORE);  // Update scores with exact matches
    }
  }

  /**
   * Finds the best matching title based on the calculated scores.
   *
   * @param scores The map of titles and their calculated scores.
   * @return The best matched title or empty if no suitable match is found.
   */
  private Optional<String> findBestMatch(Map<String, Integer> scores) {
    return scores.entrySet().stream()
      .max(Map.Entry.comparingByValue())  // Get the entry with the highest score
      .filter(entry -> entry.getValue() > 0)  // Filter out matches with a score of 0
      .map(Map.Entry::getKey);  // Return the best match
  }

  /**
   * Helper method to find fuzzy matches for a given token using Jaro-Winkler distance.
   *
   * @param token the token to match against the partial titles.
   * @return a Set of matched titles based on fuzzy matching.
   */
  private Set<String> getFuzzyPartialMatches(String token) {
    return partialTitleIndex.entrySet().stream()
      .filter(entry -> jaroWinklerDistance.apply(token, entry.getKey()) <= SIMILARITY_THRESHOLD)  // Apply fuzzy matching
      .map(Map.Entry::getValue)  // Get the normalised titles
      .flatMap(Set::stream)
      .collect(Collectors.toSet());  // Collect and return the matched titles
  }
}
