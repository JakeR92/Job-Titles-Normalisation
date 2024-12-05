# Standardising Job Titles Process Documentation

## Overview

The **Standardising Job Titles Process** aims to normalise job titles by providing a mechanism that matches an input string to its best equivalent standardised title. The process utilises exact matches, partial matches, and synonym matches, along with typo tolerance to determine the most likely standardised job title from a predefined list.

### Example Use Case

Given a list of Normalised job titles:
- "Architect"
- "Software engineer"
- "Quantity surveyor"
- "Accountant"

The system should map input job titles as follows:
- `"Java engineer"` → `"Software engineer"`
- `"C# engineer"` → `"Software engineer"`
- `"Accountant"` → `"Accountant"`
- `"Chief Accountant"` → `"Accountant"`

## Alternative Approaches and Optimisations

If I had more control over this project, I would have researched alternatives to more efficiently normalise the input titles. For instance, PostgreSQL has full-text search functionality, which, along with a custom synonym dictionary, could be used to enable more efficient matching of job titles and offload the logic from the Java code.

PostgreSQL's full-text search is a feature that helps to quickly search and rank text data. It breaks down text into smaller parts called "tokens" and creates an index to speed up searches. This would allow us to search large sets of data and get results ranked by how closely they match the query. Full-text search also has advanced features, such as recognising different forms of a word (for example, treating "run" and "running" as the same word) and using custom dictionaries for synonyms. With a custom synonym dictionary, "Java" could be treated as a synonym for "Software Engineer." So, when you search for "Java Engineer," PostgreSQL would recognise it as closely related to "Software Engineer" and return it as the top match.

The benefits of this approach include:

- **Scalability**: PostgreSQL is highly efficient at handling large datasets, making it easier to scale as the number of job titles increases.
- **Reduced Java Logic**: By moving the matching and synonym handling to the database, the Java code becomes simpler and easier to maintain, reducing complexity.
- **Better Accuracy**: Full-text search with synonym support ensures that even varied titles, like "Java Engineer" or "C# Developer," are correctly mapped to their normalised counterparts.
- **Flexibility**: The synonym dictionary can be easily updated to accommodate new job titles or variations, making the system adaptable over time.

## Default Mappings
The system includes a default set of normalised job titles and their synonyms to standardise input variations.
- **Software Engineer**: synonyms include "java", "c#", "python", "developer", "programmer", "coder"
- **Architect**: synonyms include "designer"
- **Accountant**: synonyms include "financial", "bookkeeper"
- **Quantity Surveyor**: synonyms include "construction"


## Features

- **Exact Match:** Direct matching of input with Normalised titles.
- **Partial Match:** Matches a word or consecutive words from the input title with a word or consecutive words from the Normalised title.
- **Synonym Match:** Matches input words with known synonyms for job titles.
- **Typo Tolerance:** Uses fuzzy matching (Jaro-Winkler distance) to allow for minor typos in input titles.
- **Cleaning Special Characters:** Option to remove special characters from the input string for cleaner matching.

**Note:** The Jaro-Winkler distance is a library used to measure the similarity between two strings. We use this library to compare partial titles, in scenarios where exact matches may not be possible, helping to identify close but not identical strings.
## Weighting System
- **Exact Match:** When the input exactly matches a Normalised title, the match is returned immediately. This is considered the highest-confidence match and has the highest priority.
- **Partial Match:** When parts of the input title match words from a Normalised title, the system awards 4 points per matched word. If the matches are in consecutive order (e.g., "Senior Full Stack"), a bonus is applied: 2^n (where n is the number of consecutive words matched). This bonus increases exponentially based on how many consecutive words match, encouraging more accurate matches in sequence.
- **Synonym Match:** This adds 2 points per matched synonym found in the input title. Synonyms help match job titles that are correlated but not identical, ensuring that the system can handle variations without producing ties or no matches.
- **Typo Tolerance:** Uses fuzzy matching (Jaro-Winkler distance) to account for minor typos in input titles. This type of match earns 1 point per matched partial title, allowing the system to still return valid matches despite slight spelling mistakes.

### Reasoning Behind the Weighting System
The weighting system is designed to give greater confidence to partial matches that align closely with the Normalised title, especially when the matches are in consecutive sequence. The bonus system rewards higher accuracy in matching contiguous parts of the title, ensuring that sequences of words are prioritised. Although synonyms are useful for matching job titles that are correlated but not identical, we want to prioritise normalising against the actual Normalised title rather than allowing a large number of synonym matches to overshadow the exact match logic. This ensures more relevant and accurate title matching.

## Configuration Options
- **Allow Typos**: Set whether to allow typos in partial title matches.  
  Example: `setAllowTypos(true)` allows partial matches even with minor typos.
- **Clean Special Characters**: Set whether to clean special characters from input titles.  
  Example: `shouldCleanSpecialChars(true)` removes special characters like commas or periods from input titles.

## Classes

### `Normaliser`

The `Normaliser` class is responsible for normalising job titles by matching input titles to their closest equivalent standardised titles.

<img src="./Title%20Normalisation.png" alt="Title Normalisation" width="50%" />

#### Constructor

- **`Normaliser(Map<String, Set<String>> normalisedMap)`**  
  Initialises the normaliser with a provided map of Normalised titles and their synonyms as well as the default mappings.

- **`Normaliser()`**  
  Initialises the normaliser with default mappings.

#### Methods

- **`public Optional<String> normalise(String inputTitle)`**  
  Normalises an input title by searching for exact matches, synonyms, and partial matches. Returns the best matching title or `Optional.empty()` if no match is found.

- **`public void shouldCleanSpecialChars(boolean shouldCleanSpecialChars)`**  
  Configures whether special characters (e.g., punctuation) should be cleaned from input titles.

- **`public void setAllowTypos(boolean allowTypos)`**  
  Configures whether typos should be tolerated during partial title matching.

#### Private Methods

- **`private String findExactMatch(String cleanedInput)`**  
  Finds an exact match for the cleaned input title.

- **`private Map<String, Integer> calculateScores(String[] tokens)`**  
  Calculates a match score for each Normalised title based on the input tokens.

- **`private void matchSynonyms(Map<String, Integer> scores, String token)`**  
  Matches a token against known synonyms and updates the scores.

- **`private void matchPartialTitles(Map<String, Integer> scores, String token)`**  
  Matches a token against partial titles and updates the scores.

- **`private Optional<String> findBestMatch(Map<String, Integer> scores)`**  
  Returns the best matching title based on calculated scores.

- **`private Set<String> getFuzzyPartialMatches(String token)`**  
  Returns fuzzy matches using Jaro-Winkler distance for token-based fuzzy matching.

### `ConsecutiveMatchBonusCalculator`

The `ConsecutiveMatchBonusCalculator` class calculates a bonus for consecutive matches of tokens within a title.

<img src="./Consecutive%20Match%20Bonus%20Calculator.png" alt="Consecutive Match Bonus Calculator.png" width="50%" />

#### Methods

- **`protected static void calculateConsecutiveMatchBonus(Map<String, Integer> scores, String[] tokens)`**  
  Calculates the bonus for consecutive matches of tokens within a title.

- **`private static int findTokenIndex(String[] tokens, String token)`**  
  Finds the index of a token in the input tokens list.

- **`private static int calculateConsecutiveMatches(String[] matchTokens, int matchIndex, int tokenStartIndex, String[] tokens)`**  
  Calculates the number of consecutive matches starting from a given index.

- **`private static int calculateBonus(int consecutiveCount)`**  
  Calculates the bonus for a given number of consecutive matches.

### `SynonymMapper`

The `SynonymMapper` class manages the mapping between Normalised titles and their associated synonyms. It also supports cleaning special characters from input tokens.

#### Constructor

- **`public SynonymMapper(Map<String, Set<String>> initialMappings)`**  
  Initialises the SynonymMapper with custom mappings, while also including the default mappings.

- **`public SynonymMapper()`**  
  Initialises the SynonymMapper with default mappings.

#### Methods

- **`public Set<String> getNormalisedTitles()`**  
  Retrieves the set of Normalised titles managed by the `SynonymMapper`.

- **`public  Set<String> getTitlesForToken(String token)`**  
  Retrieves the set of titles associated with a given token.

- **`public String clean(String token)`**  
  Cleans a token by removing special characters and converting it to lowercase.

- **`public void setShouldCleanSpecialChars(boolean shouldCleanSpecialChars)`**  
  Configures whether special characters should be cleaned from input tokens.

- **`public void addMapping(String title, Set<String> synonyms)`**  
  Adds a new mapping between a title and its associated synonyms.

- **`private void initialiseSynonymToTitle()`**  
  Initialises the mapping between synonyms and their associated titles.

## Usage Example

### normalising a Job Title

```java

// Initialise normaliser with default mappings
Normaliser normaliser = new Normaliser();

// Normalise an input job title
Optional<String> NormalisedTitle = normaliser.normalise("Real Estate Agent");

// Output the result
System.out.println(NormalisedTitle.orElse("No match found"));
```
### Customising Synonym Mappings

```java
// Custom job title mappings
Map<String, Set<String>> customMappings = new HashMap<>();
customMappings.put("Full Stack Software Engineer", new HashSet<>(Set.of("developer", "programmer", "coder")));
customMappings.put("Cloud Computing Specialist", new HashSet<>(Set.of("AWS")));

// Initialise normaliser with custom mappings
Normaliser customNormaliser = new Normaliser(customMappings);

// Normalise a title with custom mappings
Optional<String> customNormalisedTitle = customNormaliser.normalise("Full Stack Developer");

// Output the result
System.out.println(customNormalisedTitle.orElse("No match found"));

```

## Test Scenarios

### Normaliser Tests

The following test scenarios cover various conditions for the `Normaliser` class, ensuring it behaves as expected under different input situations.

#### 1. Valid Synonyms and Titles

##### Scenario: Normalising Valid Synonyms to Correct Title
- **Description**: Tests the normalisation of synonyms to their corresponding titles.
- **Example**:
    - Input: "developer"
    - Expected Output: "Software Engineer"

##### Scenario: Prioritising Exact Title Matches
- **Description**: Tests that exact title matches take precedence over synonyms.
- **Example**:
    - Input: "Engineer"
    - Expected Output: "Engineer" (exact match)

##### Scenario: Normalising Synonyms with Punctuation
- **Description**: Tests the handling of synonyms that include punctuation.
- **Example**:
    - Input: "developer coder!"
    - Expected Output: "Software Engineer"

##### Scenario: Normalising Mixed Case Input
- **Description**: Tests the normalisation of input with mixed case.
- **Example**:
    - Input: "DeVelOper"
    - Expected Output: "Software Engineer"

##### Scenario: Matching Partial Titles
- **Description**: Tests the matching of partial titles.
- **Example**:
    - Input: "soft engineer"
    - Expected Output: "Engineer"

#### 2. Unknown or No Synonyms

##### Scenario: Returning No Match for Unknown Titles
- **Description**: Tests the handling of unknown titles with no synonyms.
- **Example**:
    - Input: "unknown"
    - Expected Output: `Optional.empty()`

##### Scenario: Returning No Match for Empty Synonym Set
- **Description**: Tests the handling of synonyms with an empty set.
- **Example**:
    - Input: "dev" (empty synonym set)
    - Expected Output: `Optional.empty()`

#### 3. Null or Empty Input

##### Scenario: Returning No Match for Null Input
- **Description**: Tests that null inputs return no match.
- **Example**:
    - Input: `null`
    - Expected Output: `Optional.empty()`

##### Scenario: Returning No Match for Empty String
- **Description**: Tests that empty strings return no match.
- **Example**:
    - Input: ""
    - Expected Output: `Optional.empty()`

#### 4. Multiple Tokens

##### Scenario: Selecting Title with Highest Score
- **Description**: Tests that the title with the highest score is selected when multiple tokens match.
- **Example**:
    - Input: "engineer developer"
    - Expected Output: "Software Engineer" (due to higher scoring for "developer")

##### Scenario: Handling Ties Between Titles
- **Description**: Tests the resolution of ties between multiple titles with equal scores.
- **Example**:
    - Input: "engineer software"
    - Expected Output: Either "Engineer" or "Software Engineer"

#### 5. Complex Scenarios

##### Scenario: Handling Synonym and Partial Title Match Together
- **Description**: Tests the handling of both synonym and partial title matches.
- **Example**:
    - Input: "developer engineer"
    - Expected Output: "Software Engineer"

##### Scenario: Handling Consecutive Partial Title Matches
- **Description**: Tests that multiple partial matches are handled with bonus scoring.
- **Example**:
    - Input: "software Architect - tcl prolog perl vb"
    - Expected Output: "Software Architect"

##### Scenario: Removing Unnecessary Grammar or Extra Words
- **Description**: Tests the removal of unnecessary words or grammar before matching.
- **Example**:
    - Input: "the best developer ever"
    - Expected Output: "Software Engineer"

##### Scenario: Handling Tie Between Multiple Titles
- **Description**: Tests that a tie between multiple titles with equal scores is resolved.
- **Example**:
    - Input: "engineer technician"
    - Expected Output: Either "Engineer" or "Technician"

#### 6. Multiple Valid Synonyms

##### Scenario: Returning Title for Common Synonym
- **Description**: Tests that the title for a common synonym is returned when multiple valid synonyms apply.
- **Example**:
    - Input: "Database Manager"
    - Expected Output: "Database"

##### Scenario: Handling Multiple Valid Synonyms with Similar Scoring
- **Description**: Tests that when multiple valid synonyms have similar scores, any matching title can be returned.
- **Example**:
    - Input: "developer coder"
    - Expected Output: Either "Software Engineer" or "Programmer"

#### 7. Punctuation Cleaning

##### Scenario: Cleaning Punctuation Before Normalising
- **Description**: Tests that punctuation is cleaned before normalisation.
- **Example**:
    - Input: "developer, coder!"
    - Expected Output: "Software Engineer"

##### Scenario: Cleaning Punctuation and Handling Trailing Punctuation
- **Description**: Tests that trailing punctuation is cleaned properly.
- **Example**:
    - Input: "coder! developer."
    - Expected Output: "Software Engineer"

##### Scenario: Cleaning Punctuation with Multiple Characters
- **Description**: Tests cleaning of multiple punctuation characters from input.
- **Example**:
    - Input: "!developer, @coder#"
    - Expected Output: "Software Engineer"

#### 8. Typo Handling

##### Scenario: Normalising with Minor Typos
- **Description**: Tests normalisation with minor typos in the input.
- **Example**:
    - Input: "engneer"
    - Expected Output: "Software Engineer"

##### Scenario: Normalising with Medium Typos
- **Description**: Tests normalisation with medium-level typos.
- **Example**:
    - Input: "sftwre"
    - Expected Output: "Software Engineer"

##### Scenario: Not Matching Severe Typos
- **Description**: Tests that severe typos do not result in a match.
- **Example**:
    - Input: "sofwise engonoor"
    - Expected Output: `Optional.empty()`

#### 9. Default Normaliser

##### Scenario: Normalising Single Matching Synonym to Default Title
- **Description**: Tests normalisation using the default `Normaliser` configuration.
- **Example**:
    - Input: "java"
    - Expected Output: "Software Engineer"

##### Scenario: Normalising Multiple Matching Synonyms to Same Title
- **Description**: Tests normalisation when multiple synonyms match the same title.
- **Example**:
    - Input: "programmer coder"
    - Expected Output: "Software Engineer"


### SynonymMapper Tests

The following test scenarios cover various conditions for the `SynonymMapper` class, ensuring it behaves as expected under different input situations.

#### 1. Tests for `getNormalisedTitles()`

##### Scenario: Should Return All Titles
- **Description**: Verifies that the method returns all the available titles in the synonym mapping.
- **Example**:
    - Input: `synonymMapper.getNormalisedTitles()`
    - Expected Output: A set containing `["Software Engineer", "Architect", "Accountant", "Quantity Surveyor"]`

#### 2. Tests for `addMapping()`

##### Scenario: Should Add New Mapping Correctly
- **Description**: Tests that new synonyms are correctly mapped to a title when `addMapping()` is called.
- **Example**:
    - Input: `synonymMapper.addMapping("Data Scientist", ["data analyst", "data engineer"])`
    - Expected Output:
        - "Data Scientist" is associated with both "data analyst" and "data engineer".

##### Scenario: Should Not Override Existing Titles
- **Description**: Verifies that the method does not override existing title mappings when adding new ones.
- **Example**:
    - Input: `synonymMapper.addMapping("Accountant", ["financial consultant"])`
    - Expected Output:
        - "Accountant" is associated with "financial consultant", and no existing mappings are overridden.

#### 3. Tests for `getTitlesForToken()`

##### Scenario: Should Return Titles for Valid Synonym
- **Description**: Verifies that the method returns the correct titles for a valid synonym.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken("java")`
    - Expected Output: `["Software Engineer"]`

##### Scenario: Should Return Empty for Non-Existent Token
- **Description**: Verifies that the method returns an empty set for tokens that have no synonyms mapped.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken("unknown")`
    - Expected Output: `[]` (empty set)

#### 4. Tests for `clean()`

##### Scenario: Should Clean String by Removing Punctuation and Converting to Lowercase
- **Description**: Tests that punctuation is removed, and the string is converted to lowercase when cleaning.
- **Example**:
    - Input: `"  Java, C# @!  "`
    - Expected Output: `"java c"`

##### Scenario: Should Return Empty for Only Punctuation
- **Description**: Verifies that the method returns an empty string when the input consists only of punctuation.
- **Example**:
    - Input: `"!@#$$%^&*()"`
    - Expected Output: `""` (empty string)

#### 5. Tests for Synonym to Title Index Initialization

##### Scenario: Should Initialize Synonym-to-Title Mapping Correctly
- **Description**: Verifies that synonym-to-title mappings are initialized correctly.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken("java")`
    - Expected Output: `["Software Engineer"]`

##### Scenario: Should Return Empty Set for Unmapped Token
- **Description**: Verifies that the method returns an empty set for tokens that are not mapped to any titles.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken("nonexistent")`
    - Expected Output: `[]` (empty set)

#### 6. Edge Case Tests

##### Scenario: Should Return Empty Set for Empty Token
- **Description**: Verifies that the method returns an empty set when an empty string is provided as a token.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken("")`
    - Expected Output: `[]` (empty set)

##### Scenario: Should Return Empty Set for Null Token
- **Description**: Verifies that the method returns an empty set when a `null` token is provided.
- **Example**:
    - Input: `synonymMapper.getTitlesForToken(null)`
    - Expected Output: `[]` (empty set)

### ConsecutiveMatchBonusCalculator Tests

The following test scenarios cover various conditions for the `ConsecutiveMatchBonusCalculator` class, ensuring it calculates consecutive match bonuses as expected.

#### 1. Tests for `calculateConsecutiveMatchBonus()`

##### Scenario: Multiple Sequences
- **Description**: Verifies that the method correctly calculates the consecutive match bonus for multiple matching sequences.
- **Example**:
    - Input:
        - Tokens: `{"Senior", "full", "stack", "software", "engineer", "with", "cloud", "computing", "expertise"}`
        - Scores: `{"Senior Full Stack Software Engineer specializing in Cloud Computing": 28}`
    - Expected Output:
        - The score for `"Senior Full Stack Software Engineer specializing in Cloud Computing"` is updated to `64`.

##### Scenario: Single Sequence
- **Description**: Tests that the method correctly calculates the consecutive match bonus for a single matching sequence.
- **Example**:
    - Input:
        - Tokens: `{"new!", "senior", "full", "stck", "software", "engineer", "with", "cloud", "computing", "expertise"}`
        - Scores: `{"Senior Full Stack Software Engineer specializing in Cloud Computing": 024
    - Expected Output:
        - The score for `"Senior Full Stack Software Engineer specializing in Cloud Computing"` is updated to `36`.

##### Scenario: No Consecutive Match
- **Description**: Verifies that the method gets a bonus score of `0` when there is no consecutive match in the token sequence.
- **Example**:
    - Input:
        - Tokens: `{"Full", "Senior", "Engineer", "Stack"}`
        - Scores: `{"Senior Full Stack Software Engineer specializing in Cloud Computing": 16}`
    - Expected Output:
        - The score for `"Senior Full Stack Software Engineer specializing in Cloud Computing"` remains `16`.

##### Scenario: No Partial Match
- **Description**: Verifies that the method gets a bonus score of `0` and skips calculation due to score not being equal to a minimum score of 2 partial title matches
- **Example**:
    - Input:
        - Tokens: `{"Does", "Not", "match"}`
        - Scores: `{"Senior Full Stack Software Engineer specializing in Cloud Computing": 0}`
    - Expected Output:
        - The score for `"Senior Full Stack Software Engineer specializing in Cloud Computing"` remains `0`.