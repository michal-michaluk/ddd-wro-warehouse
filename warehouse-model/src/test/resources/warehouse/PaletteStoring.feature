Feature: Storing of newly completed palette

  Background:
      # once upon a time
    When label for new palette is printed
      # some time later
      # palette is stretched
    When box is scanned
    When box amount 20
      # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


  Scenario: palette stored at preferred location
    When label of preferred location is scanned and palette is stored at that location
    Then new location of palette is known


  Scenario: palette stored at different location
    When label of location A-31-3 is scanned and palette is stored at that location
    Then new location of palette is known


  Scenario: palette stored at temporary location without label
    When location is chosen palette dropped at that location
    Then new location of palette is known
