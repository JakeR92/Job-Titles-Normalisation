package com.feefo.title_normaliser;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The {@code SynonymMapper} class manages the mapping between normalised titles and their associated synonyms.
 * It provides functionality for adding new title-synonym mappings, cleaning input tokens, and retrieving titles based on token matches.
 * It also supports the cleaning of special characters (e.g., punctuation) from tokens.
 */
public class SynonymMapper {

  // The normalised map of titles and their associated synonyms
  private final Map<String, Set<String>> normalisedMap;

  // Pattern used to remove punctuation from input tokens
  private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");

  // Default synonym mappings for common job titles
  private static final Map<String, Set<String>> DEFAULT_MAPPINGS = Map.of(
    "Software Engineer", new HashSet<>(Set.of("java", "c#", "python", "developer", "programmer", "coder")),
    "Architect", new HashSet<>(Set.of("designer")),
    "Accountant", new HashSet<>(Set.of("financial", "bookkeeper")),
    "Quantity Surveyor", new HashSet<>(Set.of("construction"))
  );

  // Index for mapping synonyms to their corresponding titles
  private final Map<String, Set<String>> synonymToTitleIndex = new HashMap<>();

  // Flag indicating whether to clean special characters (e.g., punctuation) from input tokens
  private boolean shouldCleanSpecialChars = false;

  /**
   * Default constructor that initializes the SynonymMapper with default mappings.
   */
  public SynonymMapper() {
    this.normalisedMap = new HashMap<>(DEFAULT_MAPPINGS);
    initialiseSynonymToTitle();
  }

  /**
   * Constructor that initializes the SynonymMapper with custom mappings,
   * while also including the default mappings.
   *
   * @param initialMappings A map of custom title-synonym mappings.
   */
  public SynonymMapper(Map<String, Set<String>> initialMappings) {
    this.normalisedMap = new HashMap<>(initialMappings);
    DEFAULT_MAPPINGS.forEach((key, value) -> this.normalisedMap.merge(key, value, (existingSet, newSet) -> {
      Set<String> combinedSet = new HashSet<>(existingSet);
      combinedSet.addAll(newSet);
      return combinedSet;
    }));
    initialiseSynonymToTitle();
  }

  /**
   * Retrieves the set of normalised titles managed by this SynonymMapper.
   *
   * @return A set containing the normalised titles.
   */
  public Set<String> getNormalisedTitles() {
    return normalisedMap.keySet();
  }

  /**
   * Sets the flag for whether special characters should be cleaned from input tokens.
   *
   * @param shouldCleanSpecialChars Boolean flag to enable or disable cleaning special characters.
   */
  public void setShouldCleanSpecialChars(boolean shouldCleanSpecialChars) {
    this.shouldCleanSpecialChars = shouldCleanSpecialChars;
  }

  /**
   * Initializes the mapping between synonyms and their associated titles.
   * This method is called during construction to populate the synonymToTitleIndex.
   */
  private void initialiseSynonymToTitle() {
    synonymToTitleIndex.clear();

    // Iterate over each title and its associated synonyms
    for (Map.Entry<String, Set<String>> entry : normalisedMap.entrySet()) {
      String title = entry.getKey();
      for (String synonym : entry.getValue()) {
        String cleanedSynonym = clean(synonym);  // Clean the synonym to handle special characters
        synonymToTitleIndex.computeIfAbsent(cleanedSynonym, k -> new HashSet<>()).add(title);
      }
    }
  }

  /**
   * Adds a new mapping between a title and a list of synonyms.
   * This will update the normalised map and the synonym-to-title index.
   *
   * @param title The title to associate with the synonyms.
   * @param synonyms A list of synonyms to associate with the title.
   */
  public void addMapping(String title, List<String> synonyms) {
    normalisedMap.putIfAbsent(title, new HashSet<>());
    normalisedMap.get(title).addAll(synonyms);

    // Update the synonym-to-title index
    for (String synonym : synonyms) {
      String cleanedSynonym = clean(synonym);
      synonymToTitleIndex.computeIfAbsent(cleanedSynonym, k -> new HashSet<>()).add(title);
    }
  }

  /**
   * Cleans an input string by removing special characters and converting it to lowercase.
   * The cleaning behavior is determined by the {@code shouldCleanSpecialChars} flag.
   *
   * @param input The input string to clean.
   * @return The cleaned string.
   */
  public String clean(String input) {
    if (input == null) {
      return null;
    }
    if (!shouldCleanSpecialChars) {
      return input.toLowerCase().trim();  // Only lowercase and trim if special chars are not to be cleaned
    }
    return PUNCTUATION_PATTERN.matcher(input.toLowerCase()).replaceAll("").trim();  // Clean punctuation and lowercase
  }

  /**
   * Retrieves the set of titles associated with a given token.
   * The token is first cleaned before looking up matching titles in the index.
   *
   * @param token The token to match against the synonym-to-title index.
   * @return A set of titles associated with the token, or an empty set if no matches are found.
   */
  public Set<String> getTitlesForToken(String token) {
    return synonymToTitleIndex.getOrDefault(clean(token), Collections.emptySet());
  }
}
