Feature: Completing of new palette
  to deliver in fifo rule and track suspected products
  we need to track location of stored palettes in warehouse

  Domain story: registering newly produced parts
  - warehouseman registers new palette
  - validation of palette content
  in case of invalid, command is processed but warning is kept for QA department
  - computation of preferred location to store that palette
  - new palette is registered in system (persisted)

  Scenario: new palette completed in production
    # once upon a time
    Given label for new palette is printed
    # some time later
    # palette is stretched
    Given box is scanned
    Given box is scanned
    Given box is scanned
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


  Scenario: new palette completed in production (label is printed online)
    # palette is stretched
    Given box is scanned
    Given box is scanned
    Given box is scanned
    Given label for new palette is printed
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


  Scenario: new palette completed in production, but we don't scan all boxes
    # once upon a time
    Given label for new palette is printed
    # some time later
    # palette is stretched
    Given box is scanned
    Given box amount 20
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


#  Scenario: new palette with partial box is completed in production
#    # once upon a time
#    Given label for new palette is printed
#    # some time later
#    # palette is stretched
#    Given box is scanned
#    Given box amount 20
#    Given partial box is scanned
#    # palette label is glued to palette
#    When palette label is scanned
#    Then new palette is ready to store
#    Then preferred location is proposed

