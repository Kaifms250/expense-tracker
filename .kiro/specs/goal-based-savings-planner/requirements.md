# Requirements Document

## Goal-Based Savings Planner

## Introduction

This document specifies requirements for the Goal-Based Savings Planner feature to be integrated into the Smart Expense Tracker application. The feature enables users to create, track, and manage financial savings goals with automated calculations and intelligent recommendations.

## Glossary

- **Goal**: A financial target with a name, target amount, current savings, and deadline
- **Target Amount**: The total amount the user wants to save for a specific goal
- **Current Savings**: The amount already saved toward the goal (optional, defaults to zero)
- **Target Date**: The specific date by which the goal should be achieved
- **Target Duration**: The number of months from creation until goal deadline
- **Goal Status**: The current state of a goal (On Track, Behind Schedule, Achieved)
- **Goal Planner**: The main feature module managing all goal-related operations
- **Goal Summary**: Aggregate statistics across all active goals
- **Goal Card**: UI component displaying individual goal progress and details
- **Required Monthly Savings**: The amount needed to save each month to reach the goal on time
- **Expected Monthly Savings**: The actual savings rate derived from current progress and remaining time
- **Remaining Amount**: Target Amount minus Current Savings
- **Completion Percentage**: (Current Savings / Target Amount) × 100

## Requirements

### Requirement 1: Goal Creation

**User Story:** As a user, I want to create savings goals with names, amounts, and deadlines, so that I can track progress toward my financial objectives.

#### Acceptance Criteria

1. WHEN a user provides a goal name (1-100 characters, non-empty after trimming), target amount (0.01-9,999,999.99), and either a target date or target duration (1-600 months), THE GoalManager SHALL create a new Goal entity and assign a unique identifier.

2. WHEN current savings are provided during creation, THE Goal SHALL store the initial savings amount between 0.00 and target amount; WHEN current savings are omitted, THE Goal SHALL initialize current savings to 0.00.

3. WHEN a target date is provided, THE GoalCalculator SHALL compute the deadline as the provided date; WHEN a target duration in months is provided, THE GoalCalculator SHALL compute the deadline as the creation date plus the specified number of months.

4. WHEN Goal is created, THE Goal SHALL generate a goal icon based on the goal name using predefined mappings (e.g., "laptop" → 💻, "trip" → ✈️, "emergency fund" → 🛡️, "car" → 🚗, "house" → 🏠, "vacation" → 🌴). Goals with unmapped names SHALL use a default icon (💰).

5. IF an invalid target date (null, malformed format, or past date) is provided, THE GoalManager SHALL throw an IllegalArgumentException with message "Target date must be a valid future date".

6. IF any required field is missing or empty, THE GoalManager SHALL throw an IllegalArgumentException with message "All fields are required: name, target amount, and either target date or duration".

7. IF target amount is not positive (zero or negative), THE GoalManager SHALL throw an IllegalArgumentException with message "Target amount must be greater than zero".

---

### Requirement 2: Savings Calculations

**User Story:** As a user, I want the system to automatically calculate my savings requirements, so that I know exactly how much to save to achieve my goals.

#### Acceptance Criteria

1. THE GoalCalculator SHALL compute Remaining Amount as: Target Amount minus Current Savings.

2. IF Target Amount equals zero, THEN THE GoalCalculator SHALL set Required Monthly Savings to 0.00 and emit an error indicator indicating division by zero.

3. IF the number of months between the current date and the Target Date is less than 1, THEN THE GoalCalculator SHALL set Required Monthly Savings to the Remaining Amount and emit a warning indicating insufficient time.

4. THE GoalCalculator SHALL compute Required Monthly Savings as: Remaining Amount divided by the number of full months between the current date and the Target Date.

5. THE GoalCalculator SHALL compute Daily Savings Target as: Required Monthly Savings divided by 30.

6. THE GoalCalculator SHALL compute Goal Completion Percentage as: (Current Savings divided by Target Amount) multiplied by 100.

7. IF Target Amount equals zero, THEN THE GoalCalculator SHALL set Goal Completion Percentage to 0% and emit an error indicator indicating division by zero.

8. THE GoalCalculator SHALL define "on track" as: Current Savings is greater than or equal to the Expected Savings at the current date, where Expected Savings equals Required Monthly Savings multiplied by the number of full months elapsed since the goal start date.

9. IF current progress is on track, THEN THE GoalCalculator SHALL set Estimated Completion Date to the Target Date.

10. IF current progress is not on track, THEN THE GoalCalculator SHALL set Estimated Completion Date to the Target Date plus (Remaining Amount divided by Daily Savings Target) days.

11. WHEN the current date is on or after the Target Date, THE GoalCalculator SHALL set the Estimated Completion Date to the Target Date.

---

### Requirement 3: Goal Status Determination

**User Story:** As a user, I want to see whether I am on track or behind schedule for my goals, so that I can adjust my savings behavior accordingly.

#### Acceptance Criteria

1. IF Goal Completion Percentage is greater than or equal to 100, THE GoalProgressTracker SHALL set Goal Status to "Achieved".

2. IF Goal Completion Percentage is less than 100, AND Expected Monthly Savings is greater than Required Monthly Savings by at least 10%, THE GoalProgressTracker SHALL set Goal Status to "Behind Schedule".

3. IF Goal Completion Percentage is less than 100, AND Expected Monthly Savings is greater than Required Monthly Savings by at least 0.01% AND less than 10%, THE GoalProgressTracker SHALL set Goal Status to "On Track".

4. IF Current Savings exactly equals Target Amount, THE GoalProgressTracker SHALL mark the goal as "Achieved" regardless of the current date.

5. THE GoalStatusBadge SHALL display the current status with appropriate color coding:
   - "Achieved" with background color #22C55E (Green)
   - "On Track" with background color #3B82F6 (Blue)
   - "Behind Schedule" with background color #F97316 (Orange)

---

### Requirement 4: Intelligent Recommendations

**User Story:** As a user, I want personalized recommendations about my savings progress, so that I can stay motivated and adjust my plans.

#### Acceptance Criteria

1. IF a goal status is "On Track" AND goal status is not "Achieved", THE GoalRecommendationService SHALL generate a message: "Save ₹[requiredMonthlySavings]/month to achieve your goal on time." WHERE requiredMonthlySavings = (Target Amount - Current Saved Amount) / Months Remaining, AND requiredMonthlySavings MUST be greater than 0.01.

2. IF a goal status is "Behind Schedule" (defined as: Percentage Complete < Percentage Time Elapsed), THE GoalRecommendationService SHALL generate a message: "Increase your monthly savings by ₹[difference] to stay on track." WHERE difference = (Required Monthly Savings at On Track Rate) - (Current Monthly Savings Rate), AND difference MUST be greater than 0.01.

3. WHEN a goal status transitions to "Achieved" (defined as: Current Saved Amount >= Target Amount), THE GoalRecommendationService SHALL generate a message: "Congratulations! You've achieved your [goal name] goal."

4. IF Remaining Amount (defined as: Target Amount - Current Saved Amount) is less than or equal to 0, THE GoalRecommendationService SHALL NOT generate any additional recommendations for that goal.

5. WHEN the goal progress meets BOTH conditions: (a) Percentage Complete is less than 50%, AND (b) Percentage Time Elapsed is less than 25%, THE GoalRecommendationService SHALL generate a message: "You are ahead of schedule. Consider increasing your target amount or reducing monthly savings."

6. WHEN multiple goal status conditions are true simultaneously, THE GoalRecommendationService SHALL apply recommendation priority in this order: (a) "Achieved" (highest priority), (b) "Behind Schedule", (c) "Ahead of Schedule" condition, (d) "On Track" (lowest priority).

---

### Requirement 5: Dashboard Integration

**User Story:** As a user, I want to see my savings goals on the main dashboard, so that I can monitor my progress alongside expenses and budgets.

#### Acceptance Criteria

1. WHEN the user navigates to the Dashboard, THE web interface SHALL display a Goal Planner section positioned immediately below Smart Spending Insights and immediately above Monthly Reports.

2. THE Goal Planner section SHALL display a Goal Summary Card containing:
   - Active Goals count as an integer value
   - Total Target Amount calculated as the sum of target amounts across all active goals
   - Total Saved calculated as the sum of current saved amounts across all active goals
   - Overall Goal Progress percentage calculated as (Total Saved / Total Target Amount) × 100, rounded to the nearest whole number

3. THE Goal Planner section SHALL display each active goal as a Goal Card containing:
   - Goal Icon displayed as a 24×24 pixel icon
   - Goal Name truncated to a maximum of 30 characters with ellipsis for longer names
   - Progress Bar displaying Completion Percentage calculated as (Current Saved / Target Amount) × 100, with a minimum width of 0% and maximum width of 100%
   - Current Saved Amount formatted in USD currency with 2 decimal places
   - Target Amount formatted in USD currency with 2 decimal places
   - Monthly Savings Required calculated as ((Target Amount - Current Saved) / Remaining Months) and formatted in USD currency with 2 decimal places; if Remaining Months is 0, display "N/A"
   - Remaining Days calculated as (Target Date - Current Date) as a positive integer; if Target Date is in the past, display 0
   - Goal Status Badge displaying one of: "On Track" (green), "Behind Schedule" (yellow), or "At Risk" (red)
   - "View Details" button as a clickable button element

4. WHEN the user clicks "View Details" on a goal card, THE system SHALL navigate to an expanded goal view within 2 seconds, displaying goal details including: Target Amount, Current Saved Amount, Target Date, Monthly Savings Required, and Available Edit Options: Edit Target Amount, Edit Target Date, and Delete Goal.

5. THE Goal Planner section SHALL display only goals with status "On Track" or "Behind Schedule"; goals with status "At Risk" or "Achieved" SHALL be excluded from the active goals display. Achieved goals SHALL be shown in a separate "Completed Goals" toggle section that is collapsed by default; clicking the toggle SHALL expand to display all achieved goals.

6. IF a user has no active goals, THE Goal Planner section SHALL display a placeholder message indicating "No active goals. Create a goal to start tracking your savings."

7. IF Total Target Amount equals 0, THE Goal Summary Card SHALL display Overall Goal Progress as 0%.

---

### Requirement 6: Goal Modification

**User Story:** As a user, I want to update my goal details, so that I can adjust my plans as circumstances change.

#### Acceptance Criteria

1. WHEN a user modifies a goal's target amount to a value greater than current savings, THE GoalCalculator SHALL recalculate Required Monthly Savings, Progress Percentage, Remaining Days, and Goal Status.

2. WHEN a user modifies a goal's current savings to a value greater than or equal to zero, THE GoalProgressTracker SHALL recalculate Progress Percentage and Goal Status.

3. WHEN a user modifies a goal's target date to a date AFTER today's date, THE GoalCalculator SHALL recalculate Remaining Days and Required Monthly Savings.

4. WHEN a user adds additional savings to a goal, THE GoalManager SHALL increase Current Savings by the added amount, recalculate Progress Percentage, and update Goal Status.

5. WHEN a user's modifications cause Progress Percentage to exactly equal 100%, THE GoalProgressTracker SHALL set Goal Status to Achieved.

6. IF a user attempts to modify a goal's target amount to a value less than or equal to current savings, THE System SHALL display an error message indicating the target must exceed current savings.

7. IF a user attempts to modify a goal's target date to a date on or before today's date, THE System SHALL display an error message indicating the target date must be in the future.

---

### Requirement 7: Goal Deletion

**User Story:** As a user, I want to delete goals that are no longer relevant, so that I can keep my goal list current.

#### Acceptance Criteria

1. WHEN a user requests to delete a goal, THE GoalManager SHALL remove the goal from the repository.

2. WHEN a goal is deleted, THE system SHALL persist the change to "goals.json" within 500 milliseconds.

3. IF an invalid goal ID is provided for deletion, THE GoalManager SHALL return HTTP 404 with message "Goal not found".

4. IF the goal has "Achieved" status, THE system SHALL NOT allow deletion; instead, the user SHALL mark them as completed or archive them.

5. IF an I/O error occurs while saving to "goals.json" after deletion, THE GoalManager SHALL throw a PersistenceException indicating the save operation failed.

6. WHEN deletion is successful, THE GoalManager SHALL return HTTP 204 with no response body.

---

### Requirement 8: Data Persistence

**User Story:** As a user, I want my savings goals to persist between sessions, so that I can track progress over time.

#### Acceptance Criteria

1. THE GoalRepository SHALL persist all goals to a JSON file named "goals.json" in the same data directory as the expense data file "expenses.json".

2. WHEN a goal is created, modified, or deleted, THE GoalRepository SHALL save all goals to "goals.json" within 5 seconds.

3. WHEN the application starts, THE GoalRepository SHALL load all persisted goals from "goals.json" into memory.

4. IF the file "goals.json" does not exist, THE GoalRepository SHALL return an empty list.

5. IF the file "goals.json" exists but is corrupted or malformed, THE GoalRepository SHALL return an empty list and log an error message indicating file corruption.

6. IF an I/O error occurs while saving to "goals.json", THE GoalRepository SHALL throw a PersistenceException indicating the save operation failed.

7. THE GoalRepository SHALL store goals in the same data directory as expenses, categories, and budgets.

---

### Requirement 9: Console Application Integration

**User Story:** As a user of the console interface, I want to manage my savings goals alongside expenses and budgets.

#### Acceptance Criteria

1. THE ConsoleApp SHALL include a "Goals" option in the main menu between "Budgets" and "Monthly Report".

2. WHEN the user selects "Goals", THE ConsoleApp SHALL display the Goal Submenu with options:
   - View all goals
   - Create new goal
   - Add savings to goal
   - Update goal details
   - Delete goal
   - Back

3. IF no goals exist, THE ConsoleApp SHALL display a "No goals found. Create your first goal!" message in the Goal Submenu.

4. WHEN viewing goals, THE ConsoleApp SHALL display each goal with: Name, Progress (percentage), Current/Target amounts, Monthly required, Days remaining, Status.

5. THE ConsoleApp SHALL display the Goal Summary when viewing all goals: Total target, Total saved, Overall progress.

6. WHEN creating a goal in console mode, THE ConsoleApp SHALL prompt for: Goal name, Target amount, Current savings (optional), Target date (yyyy-mm-dd) OR Duration in months.

7. WHEN entering goal name, THE ConsoleApp SHALL accept input up to 100 characters and reject empty input.

8. WHEN entering target amount, THE ConsoleApp SHALL accept values from 0.01 to 9,999,999.99 and reject zero or negative values.

9. WHEN entering current savings, THE ConsoleApp SHALL accept values from 0.00 to target amount and reject values exceeding target.

10. WHEN entering target date in yyyy-mm-dd format, THE ConsoleApp SHALL reject dates on or before the current date and validate format as 4 digits, hyphen, 2 digits, hyphen, 2 digits.

11. THE ConsoleApp SHALL reject goal creation if a goal with the same name already exists and display an error message indicating the duplicate name.

12. WHEN the user selects "Back" from the Goal Submenu, THE ConsoleApp SHALL return to the main menu.

---

### Requirement 10: REST API Endpoints

**User Story:** As a frontend developer, I want REST API endpoints to manage goals, so that the web interface can interact with goal data.

#### Acceptance Criteria

1. WHEN a client sends GET /api/goals, THE ApiHandler SHALL return HTTP 200 with a JSON array of all goals, where each goal includes: id (string), name (string, 1-100 characters), targetAmount (number, 0.01 to 999999.99), currentSavings (number, 0.00 to 999999.99), progressPercent (number, 0 to 100, calculated as currentSavings/targetAmount × 100), remainingAmount (number, 0.00 to 999999.99, calculated as targetAmount - currentSavings), targetDate (ISO 8601 date string or null), and targetDurationMonths (integer 1-120 or null).

2. WHEN a client sends GET /api/goals/{id}, THE ApiHandler SHALL return HTTP 200 with a JSON object containing the goal fields defined in criterion 1. IF the goal with the specified id does not exist, THE ApiHandler SHALL return HTTP 404 with an error message indicating "Goal not found".

3. WHEN a client sends POST /api/goals with a request body containing name (string, 1-100 characters, required), targetAmount (number, 0.01 to 999999.99, required), currentSavings (number, 0.00 to 999999.99, optional, defaults to 0.00), and either targetDate (ISO 8601 date string, required if targetDurationMonths absent) or targetDurationMonths (integer, 1 to 120, required if targetDate absent), THE ApiHandler SHALL create a new goal and return HTTP 201 with the created goal object. IF required fields are missing or invalid, THE ApiHandler SHALL return HTTP 400 with an error message indicating the validation failure.

4. WHEN a client sends PUT /api/goals/{id} with a request body containing any subset of: name (string, 1-100 characters), targetAmount (number, 0.01 to 999999.99), currentSavings (number, 0.00 to 999999.99), targetDate (ISO 8601 date string or null), targetDurationMonths (integer, 1-120 or null), THE ApiHandler SHALL update the goal fields specified in the request body and return HTTP 200 with the updated goal object. IF the goal with the specified id does not exist, THE ApiHandler SHALL return HTTP 404 with an error message indicating "Goal not found". IF required fields are invalid, THE ApiHandler SHALL return HTTP 400 with an error message indicating the validation failure.

5. WHEN a client sends DELETE /api/goals/{id}, THE ApiHandler SHALL remove the goal and return HTTP 204 with no response body. IF the goal with the specified id does not exist, THE ApiHandler SHALL return HTTP 404 with an error message indicating "Goal not found".

6. WHEN a client sends POST /api/goals/{id}/deposit with a request body containing amount (number, 0.01 to 999999.99, required), THE ApiHandler SHALL increase currentSavings by the specified amount and return HTTP 200 with the updated goal object. IF the goal with the specified id does not exist, THE ApiHandler SHALL return HTTP 404 with an error message indicating "Goal not found". IF amount is missing or invalid, THE ApiHandler SHALL return HTTP 400 with an error message indicating the validation failure.

7. IF a client sends a request with an invalid request body, THE ApiHandler SHALL return HTTP 400 with an error message indicating the validation failure. IF a client sends a request with an unsupported HTTP method, THE ApiHandler SHALL return HTTP 405 with an error message indicating "Method not allowed". IF an internal server error occurs, THE ApiHandler SHALL return HTTP 500 with an error message indicating "Internal server error".

---

### Requirement 11: Web Interface Styling

**User Story:** As a user, I want the Goal Planner section to match the premium fintech style of the existing application.

#### Acceptance Criteria

1. THE Goal Planner section SHALL use glassmorphism cards with CSS class "goal-card" applying background: rgba(255, 255, 255, 0.1), backdrop-filter: blur(10px), border: 1px solid rgba(255, 255, 255, 0.2), matching existing card styling.

2. Goal cards SHALL have rounded corners with CSS: border-radius: var(--radius, 16px), soft shadows with CSS: box-shadow: var(--shadow, 0 8px 32px), and consistent spacing with margin: 16px, matching the Monthly Report card.

3. THE Progress Bar SHALL use gradient colors: blue (#3B82F6) for On Track, green (#22C55E) for Achieved, orange (#F97316) for Behind Schedule.

4. THE Goal Status Badge SHALL use pill-shaped styling with CSS: border-radius: 999px, padding: 4px 12px, font-size: 12px, font-weight: 600, with appropriate background colors matching the status.

5. WHEN viewport width is less than 768px, THE Goal Planner section SHALL stack goal cards vertically in a single column layout; WHEN viewport width is 768px or greater, THE Goal Planner section SHALL display goal cards in a responsive grid layout with 2 columns.

6. WHEN the user hovers over a goal card, THE card SHALL display a smooth transition effect with CSS: transition: all 0.2s ease, and the card SHALL elevate with CSS: transform: translateY(-4px), box-shadow: 0 12px 40px.

---

### Requirement 12: Performance Requirements

**User Story:** As a user, I want the Goal Planner to be responsive, so that I have a smooth experience.

#### Acceptance Criteria

1. WHEN the user navigates to the Dashboard, THE GoalPlanner SHALL load and display all goal data within 200 milliseconds.

2. THE system SHALL handle up to 50 goals without performance degradation, where performance degradation is defined as response time increase exceeding 20% from baseline.

3. THE API endpoints SHALL respond within 100 milliseconds for all goal operations: GET /api/goals, GET /api/goals/{id}, POST /api/goals, PUT /api/goals/{id}, DELETE /api/goals/{id}, and POST /api/goals/{id}/deposit.

4. WHEN calculating goal metrics for N goals, THE GoalCalculator SHALL use algorithms with O(n) or better time complexity, with a maximum of N iterations for any calculation.

5. WHEN the user modifies a goal, THE UI SHALL update progress bars and status indicators within 100 milliseconds of the modification completing successfully.

---

### Property-Based Testing Requirements

The following correctness properties apply to goal calculations:

1. **Invariant - Completion Percentage Bounds**: FOR ALL Goal objects, Goal Completion Percentage SHALL satisfy 0 ≤ percentage ≤ 100 when currentSavings < targetAmount, and percentage = 100 when currentSavings ≥ targetAmount.

2. **Invariant - Remaining Amount Consistency**: FOR ALL Goal objects, Remaining Amount SHALL equal (Target Amount - Current Savings).

3. **Round Trip - Goal Serialization**: FOR ALL Goal objects, converting to JSON and parsing back SHALL produce an equivalent Goal with the same identifier, name, target amount, current savings, target date, and status.

4. **Round Trip - Goal CRUD Operations**: FOR ALL Goal objects, creating, reading, updating, and deleting goals SHALL maintain data integrity with no orphaned or duplicate entries.

5. **Monotonicity - Savings Progress**: WHEN Current Savings increases without exceeding Target Amount, Goal Completion Percentage SHALL increase monotonically.

6. **Idempotence - Status Calculation**: Calling GoalProgressTracker multiple times on the same goal data SHALL produce identical Goal Status results.

7. **Metamorphic - Required Monthly Savings**: WHEN Target Date is extended by N months (keeping all else constant), Required Monthly Savings SHALL decrease or stay the same.

8. **Metamorphic - Goal Deletion**: AFTER deleting a goal with ID X, findById(X) SHALL return empty, and findAll() SHALL not contain a goal with ID X.

---

### Common Program Correctness Properties

1. **Invariant - Goal Name Non-Empty**: FOR ALL created goals, Goal Name SHALL be a non-empty string after trimming whitespace.

2. **Invariant - Target Amount Positive**: FOR ALL created goals, Target Amount SHALL be greater than zero.

3. **Invariant - Target Date Future or Present**: FOR ALL created goals, Target Date SHALL be on or after the creation date.

4. **Error Handling - Invalid Input Rejection**: WHEN providing negative values for monetary fields, the system SHALL throw IllegalArgumentException with descriptive messages.

5. **Error Handling - Unknown Goal Access**: WHEN requesting a non-existent goal ID, the system SHALL return Optional.empty() at the repository level and 404 at the API level.

6. **Metamorphic - Bulk Operations**: Processing multiple goals SHALL produce the same individual results as processing each goal independently.

---

### Integration Test Requirements

The following scenarios require integration tests with representative examples:

1. Complete goal lifecycle: Create → Update savings → Verify status → Achieve → Archive

2. API round trip: POST goal → GET goal → PUT goal → DELETE goal

3. Console goal management: Create, add savings, view all, delete

4. Dashboard integration: Load dashboard and verify Goal Planner section renders with all cards

5. Data persistence: Create goals → Restart application → Verify goals load correctly

These integration tests use 1-3 representative examples each since they test infrastructure and external behavior rather than algorithmic logic.