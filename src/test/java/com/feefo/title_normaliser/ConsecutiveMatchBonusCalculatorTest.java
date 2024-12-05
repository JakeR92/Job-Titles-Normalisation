package com.feefo.title_normaliser;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsecutiveMatchBonusCalculatorTest {

  @Test
  public void testCalculateConsecutiveMatchBonus_MultipleSequences() {
    // Arrange
    String[] tokens = {"Senior", "full", "stack", "software", "engineer", "with", "cloud", "computing", "expertise"};
    Map<String, Integer> scores = new HashMap<>();
    scores.put("Senior Full Stack Software Engineer specializing in Cloud Computing", 28);

    // Act
    ConsecutiveMatchBonusCalculator.calculateConsecutiveMatchBonus(scores, tokens);

    // Assert
    assertThat(scores.get("Senior Full Stack Software Engineer specializing in Cloud Computing"))
      .isEqualTo(64);
  }

  @Test
  public void testCalculateConsecutiveMatchBonus_SingleSequence() {
    // Arrange
    String[] tokens = {"new!", "senior", "full", "stck", "software", "engineer", "with", "cloud", "computing", "expertise"};
    Map<String, Integer> scores = new HashMap<>();
    scores.put("Senior Full Stack Software Engineer specializing in Cloud Computing", 24);

    // Act
    ConsecutiveMatchBonusCalculator.calculateConsecutiveMatchBonus(scores, tokens);

    // Assert
    assertThat(scores.get("Senior Full Stack Software Engineer specializing in Cloud Computing"))
      .isEqualTo(36);
  }

  @Test
  public void testCalculateConsecutiveMatchBonus_NoConsecutiveMatch() {
    // Arrange
    String[] tokens = {"Full", "Senior", "Engineer", "Stack"};
    Map<String, Integer> scores = new HashMap<>();
    scores.put("Senior Full Stack Software Engineer specializing in Cloud Computing", 16);

    // Act
    ConsecutiveMatchBonusCalculator.calculateConsecutiveMatchBonus(scores, tokens);

    // Assert
    assertThat(scores.get("Senior Full Stack Software Engineer specializing in Cloud Computing"))
      .isEqualTo(16);
  }

  @Test
  public void testCalculateConsecutiveMatchBonus_NoPartialMatch() {
    // Arrange
    String[] tokens = {"Does", "Not", "match"};
    Map<String, Integer> scores = new HashMap<>();
    scores.put("Senior Full Stack Software Engineer specializing in Cloud Computing", 0);

    // Act
    ConsecutiveMatchBonusCalculator.calculateConsecutiveMatchBonus(scores, tokens);

    // Assert
    assertThat(scores.get("Senior Full Stack Software Engineer specializing in Cloud Computing"))
      .isEqualTo(0);
  }
}
