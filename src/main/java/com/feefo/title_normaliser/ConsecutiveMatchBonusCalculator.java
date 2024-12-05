package com.feefo.title_normaliser;

import static com.feefo.title_normaliser.Normaliser.TITLE_MATCH_SCORE;

import java.util.Map;

public class ConsecutiveMatchBonusCalculator {

  /**
   * Calculates the bonus for consecutive matches of tokens within a title.
   *
   * @param scores     A map of scores where titles are keys and their initial scores are values.
   * @param inputTitleTokens     The array of input tokens to match against.
   */
  protected static void calculateConsecutiveMatchBonus(Map<String, Integer> scores, String[] inputTitleTokens) {
    for (String normalisedTitle : scores.keySet()) {
      if (scores.get(normalisedTitle) < (TITLE_MATCH_SCORE * 2)) {
        continue; // Skip titles with that do not have a minimum score of 2 partial title matches
      }

      String[] normalisedTitleTokens = normalisedTitle.toLowerCase().split("\\s+");
      int bonus = 0;
      int normalisedIndex = 0;

      // Iterate through each token in the matchTitle
      while (normalisedIndex < normalisedTitleTokens.length) {
        String normalisedTitleToken = normalisedTitleTokens[normalisedIndex];
        int tokenIndex = findTokenIndex(inputTitleTokens, normalisedTitleToken);

        if (tokenIndex != -1) {
          // If a match is found, start checking for consecutive matches
          int consecutiveCount = calculateConsecutiveMatches(normalisedTitleTokens, normalisedIndex, tokenIndex, inputTitleTokens);
          bonus += calculateBonus(consecutiveCount);
          normalisedIndex += consecutiveCount; // Skip ahead by the number of consecutive matches
        } else {
          normalisedIndex++; // Move to the next token in matchTokens if no match was found
        }
      }

      // Update the score with the accumulated bonus
      scores.put(normalisedTitle, scores.getOrDefault(normalisedTitle, 0) + bonus);
    }
  }

  /**
   * Finds the index of the token in the input tokens list.
   *
   * @param inputTitleTokens The list of input tokens.
   * @param normalisedTitleToken  The token to find.
   * @return The index of the token in tokens, or -1 if not found.
   */
  private static int findTokenIndex(String[] inputTitleTokens, String normalisedTitleToken) {
    for (int i = 0; i < inputTitleTokens.length; i++) {
      if (inputTitleTokens[i].toLowerCase().equals(normalisedTitleToken)) {
        return i;
      }
    }
    return -1; // Token not found
  }

  /**
   * Calculates the number of consecutive matches starting from a given index.
   *
   * @param normalisedTitleTokens      The match tokens from the title.
   * @param normalisedTokenIndex       The current index in the matchTokens array.
   * @param inputTokenStartIndex  The current index in the tokens array.
   * @param inputTitleTokens           The input tokens.
   * @return The number of consecutive matches.
   */
  private static int calculateConsecutiveMatches(String[] normalisedTitleTokens, int normalisedTokenIndex, int inputTokenStartIndex, String[] inputTitleTokens) {
    int consecutiveCount = 0;
    int tokenIndex = inputTokenStartIndex;

    // Count consecutive matches from the current token index
    while (normalisedTokenIndex < normalisedTitleTokens.length && tokenIndex < inputTitleTokens.length && normalisedTitleTokens[normalisedTokenIndex].equals(inputTitleTokens[tokenIndex].toLowerCase())) {
      consecutiveCount++;
      normalisedTokenIndex++;
      tokenIndex++;
    }

    return consecutiveCount;
  }


  /**
   * Calculates the bonus for a given number of consecutive matches.
   *
   * @param consecutiveCount The number of consecutive matching tokens.
   * @return The calculated bonus.
   */
  private static int calculateBonus(int consecutiveCount) {
    if (consecutiveCount > 1) {
      return (int) Math.pow(2, consecutiveCount);  // Bonus is 2^consecutiveCount
    }
    return 0;  // No bonus for a single match
  }
}
