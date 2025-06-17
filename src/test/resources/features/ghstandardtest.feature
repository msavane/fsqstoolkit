Feature: Wikipedia Search

  Scenario: Search for Mansa Musa
    Given I open the Wikipedia main page
    When I enter "mansa musa" into the "search" field
    And I press the "ENTER" key in the "search" field
    Then I should see the "Mansa Musa" article page
