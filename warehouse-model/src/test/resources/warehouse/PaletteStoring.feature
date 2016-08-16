Feature: Storing of newly completed palette
  to deliver in fifo rule and track suspected products
  we need to track location of stored palettes in warehouse

  Domain story: picking and storing
  after pick, during transport palette is assigned to user
  after store, new location is known

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

  Scenario: palette stored at different location
    When label of location A-31-3 is scanned and palette is stored at that location
    Then palette is on location A-31-3


  Scenario: palette stored at temporary location without label
    When label of location A-31-3 is scanned and palette is stored at that location
    When label of location A-33-3 is scanned and palette is stored at that location
    Then palette is on location A-33-3


  Scenario: palette stored at temporary location without label
    When label of location A-31-3 is scanned and palette is stored at that location
    When palette is picked by user michal
    When label of location A-33-3 is scanned and palette is stored at that location
    Then palette is on location A-33-3


  Scenario: palette stored at temporary location without label
    When label of location A-31-3 is scanned and palette is stored at that location
    When palette is picked by user michal
    Then palette is on the move
