# Task List: Goal-Based Savings Planner

## Overview
Implement the Goal-Based Savings Planner feature following the design document and requirements. Tasks are organized in phases following the implementation roadmap.

## Task Graph

```
Phase 1: Core Infrastructure
├── Task 1.1: Create Goal model with all fields and enums
├── Task 1.2: Create GoalSummary DTO
├── Task 1.3: Create GoalRepository for JSON persistence
├── Task 1.4: Create GoalCalculator for all calculations
└── Task 1.5: Create GoalService as facade

Phase 2: Business Logic
├── Task 2.1: Implement GoalProgressTracker
├── Task 2.2: Implement GoalRecommendationService
└── Task 2.3: Integrate calculations and status tracking into Goal model

Phase 3: API Integration
├── Task 3.1: Add goal endpoints to ApiHandler
├── Task 3.2: Add GoalService to AppContext
└── Task 3.3: Test all REST endpoints

Phase 4: Console Integration
├── Task 4.1: Add Goals menu to ConsoleApp
├── Task 4.2: Implement create/view goals workflows
├── Task 4.3: Implement add savings workflow
├── Task 4.4: Implement update/delete goals workflows
└── Task 4.5: Test console user experience

Phase 5: Web UI
├── Task 5.1: Add CSS styles for goal cards
├── Task 5.2: Create goal-planner.js for API integration
├── Task 5.3: Update dashboard HTML
└── Task 5.4: Test responsive design

Phase 6: Testing & Polish
├── Task 6.1: Write unit tests for GoalCalculator
├── Task 6.2: Write unit tests for GoalProgressTracker
├── Task 6.3: Write property-based tests
├── Task 6.4: Write integration tests
├── Task 6.5: Performance testing and bug fixes
└── Task 6.6: Final review and polish
```

## Tasks

### Phase 1: Core Infrastructure

**Task 1.1: Create Goal model with all fields and enums**

Description: Create the Goal entity class with all required fields, constructors, getters/setters, and helper methods including icon generation based on goal name.

Requirements References: Requirement 1, Requirement 2

Sub-tasks:
- Create GoalStatus enum with ON_TRACK, BEHIND_SCHEDULE, ACHIEVED
- Create Goal class with core fields: id, name, targetAmount, currentSavings, targetDate, targetDurationMonths, icon, status, createdAt
- Add calculated field placeholders: progressPercent, remainingAmount, requiredMonthlySavings, dailySavingsTarget, daysRemaining, estimatedCompletionDate
- Add constructors: default and parameterized
- Add getter/setter methods for all fields
- Add getIcon() method with predefined mappings (laptop→💻, trip→✈️, etc.)
- Add validation methods for required fields

Expected Completion: Create src/main/java/com/expensetracker/model/Goal.java

---

**Task 1.2: Create GoalSummary DTO**

Description: Create a data transfer object for the dashboard goal summary card.

Requirements References: Requirement 5

Sub-tasks:
- Create GoalSummary class with fields: activeGoalsCount, totalTargetAmount, totalSavedAmount, overallProgressPercent
- Add constructor with all fields
- Add getter methods
- Add static factory method for creating from list of goals

Expected Completion: Create src/main/java/com/expensetracker/model/GoalSummary.java

---

**Task 1.3: Create GoalRepository for JSON persistence**

Description: Create the repository layer for persisting goals to goals.json using the existing JsonFileStorage infrastructure.

Requirements References: Requirement 8

Sub-tasks:
- Create GoalRepository class
- Initialize JsonFileStorage<Goal> for goals.json in data directory
- Implement findAll() method with sorting by created date descending
- Implement findById() method
- Implement save() method
- Implement update() method
- Implement delete() method
- Implement findByStatus() method for filtering
- Handle file not found (return empty list)
- Handle corrupted file (return empty list, log error)

Expected Completion: Create src/main/java/com/expensetracker/repository/GoalRepository.java

---

**Task 1.4: Create GoalCalculator for all calculations**

Description: Create a utility class for all goal-related calculations.

Requirements References: Requirement 2

Sub-tasks:
- Create GoalCalculator class
- Implement calculateProgressPercent(goal)
- Implement calculateRemainingAmount(goal)
- Implement calculateRequiredMonthlySavings(goal)
- Implement calculateDailySavingsTarget(goal)
- Implement calculateDaysRemaining(goal)
- Implement calculateEstimatedCompletionDate(goal)
- Implement calculateAll(goal) - bulk calculation method
- Handle edge cases: zero target amount, past target date, zero months remaining

Expected Completion: Create src/main/java/com/expensetracker/service/GoalCalculator.java

---

**Task 1.5: Create GoalService as facade**

Description: Create the service layer that coordinates repository, calculator, and progress tracking.

Requirements References: Requirement 1, Requirement 6, Requirement 7

Sub-tasks:
- Create GoalService class
- Add GoalRepository and GoalCalculator dependencies
- Implement createGoal() with validation
- Implement getAllGoals() - returns goals with calculated fields
- Implement getGoalById()
- Implement addSavings() - deposits money to goal
- Implement updateGoal()
- Implement deleteGoal()
- Implement getGoalSummary() - aggregate statistics
- Implement getGoalsByStatus()
- Validate all inputs (amount > 0, name not empty, target > current, date in future)
- Throw IllegalArgumentException with descriptive messages

Expected Completion: Create src/main/java/com/expensetracker/service/GoalService.java

---

### Phase 2: Business Logic

**Task 2.1: Implement GoalProgressTracker**

Description: Create the status determination logic for goals.

Requirements References: Requirement 3

Sub-tasks:
- Create GoalProgressTracker class
- Implement determineStatus(goal) - returns ON_TRACK, BEHIND_SCHEDULE, or ACHIEVED
- Implement isOnTrack(goal) - progress >= expected pace
- Implement isBehindSchedule(goal) - progress < expected pace by 10%+
- Implement isAchieved(goal) - progress >= 100%
- Logic for ON_TRACK: Expected Monthly Savings >= Required Monthly Savings × 0.9
- Logic for BEHIND_SCHEDULE: Expected Monthly Savings < Required Monthly Savings × 0.9
- Logic for ACHIEVED: progress >= 100%
- Add progress rate calculation

Expected Completion: Create src/main/java/com/expensetracker/service/GoalProgressTracker.java

---

**Task 2.2: Implement GoalRecommendationService**

Description: Create the intelligent recommendation generation service.

Requirements References: Requirement 4

Sub-tasks:
- Create GoalRecommendationService class
- Implement getRecommendation(goal) - returns appropriate message
- Implement getOnTrackMessage(goal) - "Save ₹X/month to achieve your goal on time"
- Implement getBehindScheduleMessage(goal) - "Increase your monthly savings by ₹X to stay on track"
- Implement getAchievedMessage(goal) - "Congratulations! You've achieved your [name] goal"
- Implement getAheadOfScheduleMessage(goal) - "You are ahead of schedule..."
- Priority order: Achieved > Behind Schedule > Ahead of Schedule > On Track
- Only generate recommendations when remainingAmount > 0

Expected Completion: Create src/main/java/com/expensetracker/service/GoalRecommendationService.java

---

**Task 2.3: Integrate calculations and status tracking into Goal model**

Description: Update Goal model to automatically recalculate fields when modified.

Requirements References: Requirement 2, Requirement 3, Requirement 6

Sub-tasks:
- Add GoalCalculator and GoalProgressTracker as dependencies to GoalService
- After any goal modification, recalculate all derived fields:
  - progressPercent
  - remainingAmount
  - requiredMonthlySavings
  - dailySavingsTarget
  - daysRemaining
  - estimatedCompletionDate
  - status
- Create updateCalculatedFields(goal) method in GoalService
- Ensure status is automatically updated to ACHIEVED when currentSavings >= targetAmount

Expected Completion: Updates to GoalService and Goal.java

---

### Phase 3: API Integration

**Task 3.1: Add goal endpoints to ApiHandler**

Description: Extend ApiHandler to support all goal-related REST endpoints.

Requirements References: Requirement 10

Sub-tasks:
- Read existing ApiHandler.java
- Add handleGoals() method for /api/goals routes
- Implement GET /api/goals - return all goals with calculated fields
- Implement GET /api/goals/{id} - return single goal or 404
- Implement POST /api/goals - create new goal with validation
- Implement PUT /api/goals/{id} - update goal details
- Implement DELETE /api/goals/{id} - delete goal, return 204 or 404
- Implement POST /api/goals/{id}/deposit - add savings to goal
- Add GoalRequest inner class for request body parsing
- Use HttpUtil.sendJson() for responses
- Handle IllegalArgumentException with 400 status

Expected Completion: Update src/main/java/com/expensetracker/web/ApiHandler.java

---

**Task 3.2: Add GoalService to AppContext**

Description: Make GoalService available to ApiHandler through AppContext.

Requirements References: Requirement 10

Sub-tasks:
- Read AppContext.java
- Add GoalService field
- Initialize GoalService in constructor after repositories are ready
- Add goalService() getter method
- Ensure proper initialization order (repository first, then service)

Expected Completion: Update src/main/java/com/expensetracker/AppContext.java

---

**Task 3.3: Test all REST endpoints**

Description: Verify all API endpoints work correctly using curl or Postman.

Requirements References: Requirement 10

Sub-tasks:
- Test POST /api/goals - create goal
- Test GET /api/goals - list all goals
- Test GET /api/goals/{id} - get single goal
- Test PUT /api/goals/{id} - update goal
- Test POST /api/goals/{id}/deposit - add savings
- Test DELETE /api/goals/{id} - delete goal
- Test error cases: invalid ID, missing fields, invalid amounts
- Verify goals.json persistence

Expected Completion: Verified working endpoints

---

### Phase 4: Console Integration

**Task 4.1: Add Goals menu to ConsoleApp**

Description: Add Goals option to main menu between Budgets and Monthly Report.

Requirements References: Requirement 9

Sub-tasks:
- Read ConsoleApp.java
- Add GoalService field and initialize in constructor
- Update printMainMenu() - renumber options (5. Goals, 6. Monthly Report, 0. Exit)
- Add handleGoalsMenu() method - displays submenu
- Add "No goals found" message when appropriate
- Add "Back" option to return to main menu

Expected Completion: Update src/main/java/com/expensetracker/ui/ConsoleApp.java

---

**Task 4.2: Implement create/view goals workflows**

Description: Implement goal creation and viewing in console.

Requirements References: Requirement 1, Requirement 9

Sub-tasks:
- Implement viewAllGoals() - display all goals with details
  - Show: name, progress%, current/target amounts, monthly required, days remaining, status
  - Display Goal Summary: total target, total saved, overall progress
- Implement createNewGoal() - prompt for goal details
  - Goal name (1-100 chars, not empty)
  - Target amount (0.01 - 9,999,999.99, > 0)
  - Current savings (optional, 0 - target amount)
  - Target date (yyyy-mm-dd format, future date) OR Duration in months (1-600)
  - Reject duplicate goal names
- Validate all inputs with IllegalArgumentException
- Display created goal summary after creation

Expected Completion: Update ConsoleApp.java with createGoal() and viewAllGoals() methods

---

**Task 4.3: Implement add savings workflow**

Description: Implement deposit functionality in console.

Requirements References: Requirement 6

Sub-tasks:
- Implement addSavingsToGoal() workflow
- Prompt for goal ID or name (partial match)
- Display matching goals with numbers
- User selects goal by number
- Prompt for deposit amount (0.01 - 9,999,999.99)
- Validate amount > 0
- Update goal and display new balance
- Show status change if applicable

Expected Completion: Update ConsoleApp.java with addSavingsToGoal() method

---

**Task 4.4: Implement update/delete goals workflows**

Description: Implement goal modification and deletion in console.

Requirements References: Requirement 6, Requirement 7

Sub-tasks:
- Implement updateGoalDetails() workflow
  - Prompt for goal ID or name
  - Display current goal details
  - Menu: Edit name, Edit target amount, Edit current savings, Edit target date, Cancel
  - Validate new values
  - Recalculate all fields after update
- Implement deleteGoal() workflow
  - Prompt for goal ID or name
  - Confirm deletion (y/n)
  - Prevent deletion of achieved goals
  - Return appropriate message

Expected Completion: Update ConsoleApp.java with updateGoalDetails() and deleteGoal() methods

---

**Task 4.5: Test console user experience**

Description: Verify all console workflows work correctly.

Requirements References: Requirement 9

Sub-tasks:
- Test full console workflow:
  1. View goals (empty state)
  2. Create goal
  3. View goals (with data)
  4. Add savings
  5. Update goal
  6. Delete goal
- Test validation errors
- Test edge cases
- Verify goal persistence across sessions

Expected Completion: Verified working console functionality

---

### Phase 5: Web UI

**Task 5.1: Add CSS styles for goal cards**

Description: Add CSS styles for goal planner section matching fintech design.

Requirements References: Requirement 11

Sub-tasks:
- Read style.css
- Add .goal-card styles (glassmorphism, rounded corners, shadows)
- Add .goal-summary-card styles
- Add .goal-progress-bar and .goal-progress-fill styles
- Add .goal-status-badge styles (On Track, Behind Schedule, Achieved)
- Add responsive breakpoints (mobile: stacked, desktop: grid)
- Add hover effects and transitions
- Match existing color palette and spacing

Expected Completion: Update src/main/resources/web/css/style.css

---

**Task 5.2: Create goal-planner.js for API integration**

Description: Create JavaScript module for goal-related UI functionality.

Requirements References: Requirement 5, Requirement 10

Sub-tasks:
- Create goal-planner.js
- Implement fetchGoals() - GET /api/goals
- Implement fetchGoal(id) - GET /api/goals/{id}
- Implement createGoal(data) - POST /api/goals
- Implement updateGoal(id, data) - PUT /api/goals/{id}
- Implement deleteGoal(id) - DELETE /api/goals/{id}
- Implement addSavings(id, amount) - POST /api/goals/{id}/deposit
- Implement renderGoalCards(goals) - generates HTML for goal cards
- Implement renderGoalSummary(summary) - generates HTML for summary card
- Add event handlers for View Details buttons
- Add loading states and error handling

Expected Completion: Create src/main/resources/web/js/goal-planner.js

---

**Task 5.3: Update dashboard HTML**

Description: Integrate Goal Planner section into main dashboard.

Requirements References: Requirement 5

Sub-tasks:
- Read index.html
- Add goal-planner.js script import
- Add Goal Planner section HTML between Smart Spending Insights and Monthly Reports
  - Goal Summary Card
  - Goal Cards Grid Container
  - Empty state placeholder
- Add modal HTML for View Details
- Verify proper section ordering

Expected Completion: Update src/main/resources/web/index.html

---

**Task 5.4: Test responsive design**

Description: Verify UI works on different screen sizes.

Requirements References: Requirement 5, Requirement 11

Sub-tasks:
- Test on mobile viewport (< 768px)
- Test on desktop viewport (>= 768px)
- Verify responsive grid layout
- Verify touch-friendly interactions
- Test on various browsers
- Verify all interactive elements work
- Check color contrast and readability

Expected Completion: Verified responsive design

---

### Phase 6: Testing & Polish

**Task 6.1: Write unit tests for GoalCalculator**

Description: Create comprehensive unit tests for all calculation methods.

Requirements References: Requirement 2

Sub-tasks:
- Create GoalCalculatorTest
- Test calculateProgressPercent:
  - 0% progress
  - 50% progress
  - 100% progress
  - Edge cases (zero target)
- Test calculateRemainingAmount
- Test calculateRequiredMonthlySavings:
  - Normal case
  - Edge case: zero months remaining
- Test calculateDailySavingsTarget
- Test calculateDaysRemaining
- Test calculateEstimatedCompletionDate
- Use JUnit 5 and assertions

Expected Completion: Create test for GoalCalculator

---

**Task 6.2: Write unit tests for GoalProgressTracker**

Description: Create unit tests for status determination logic.

Requirements References: Requirement 3

Sub-tasks:
- Create GoalProgressTrackerTest
- Test isAchieved():
  - 100% complete
  - Current == Target
  - > 100% (clamped to 100)
- Test isOnTrack():
  - At expected pace
  - Slightly ahead (within 10%)
- Test isBehindSchedule():
  - Significantly behind (>10% variance)
- Test determineStatus():
  - All three status outcomes
  - Priority: Achieved > Behind > On Track
- Test edge cases

Expected Completion: Create test for GoalProgressTracker

---

**Task 6.3: Write property-based tests**

Description: Use jqwik or similar framework for property-based testing.

Requirements References: Property-Based Testing Requirements

Sub-tasks:
- Add jqwik dependency to pom.xml
- Create GoalPropertiesTest
- Test Invariant - Completion Percentage Bounds
- Test Invariant - Remaining Amount Consistency
- Test Metamorphic - Required Monthly Savings monotonicity
- Test Metamorphic - Savings Progress monotonicity
- Test Round-Trip - Serialization/deserialization
- Generate 100+ random inputs per property
- Fix any failing properties

Expected Completion: Create property-based test class

---

**Task 6.4: Write integration tests**

Description: Create integration tests for complete workflows.

Requirements References: Integration Test Requirements

Sub-tasks:
- Create GoalIntegrationTest
- Test complete goal lifecycle:
  1. Create goal
  2. Add savings multiple times
  3. Verify status updates correctly
  4. Achieve goal
  5. Verify achieved status
- Test API round-trip (CRUD operations)
- Test console goal management flow
- Test dashboard integration (manual verification)
- Test data persistence (restart app, verify goals load)

Expected Completion: Create integration test class

---

**Task 6.5: Performance testing and bug fixes**

Description: Optimize performance and fix any discovered bugs.

Requirements References: Requirement 12

Sub-tasks:
- Test with 50 goals - verify response time < 100ms
- Test API endpoint performance
- Test calculation performance for N goals
- Fix any bugs found during testing
- Optimize any O(n²) operations to O(n)
- Add caching where appropriate
- Verify all acceptance criteria are met

Expected Completion: Performance benchmarks and bug fixes

---

**Task 6.6: Final review and polish**

Description: Final code review and polish before completion.

Requirements References: All requirements

Sub-tasks:
- Code review for all new files
- Verify naming conventions
- Verify documentation/comments
- Check for code smells
- Verify error handling
- Verify logging
- Final build and test run
- Update README if needed
- Create sample goals.json for demo

Expected Completion: Feature complete and reviewed

## Dependencies Between Tasks

### Task Prerequisites

1. **Task 1.1** (Goal model) → Prerequisite for all other tasks
2. **Task 1.2** (GoalSummary) → After Task 1.1
3. **Task 1.3** (GoalRepository) → After Task 1.1
4. **Task 1.4** (GoalCalculator) → After Task 1.1
5. **Task 1.5** (GoalService) → After Tasks 1.1, 1.3, 1.4
6. **Task 2.1** (GoalProgressTracker) → After Task 1.1
7. **Task 2.2** (GoalRecommendationService) → After Task 1.1
8. **Task 2.3** (Integrate calculations) → After Tasks 1.5, 2.1, 2.2
9. **Task 3.1** (API endpoints) → After Task 1.5
10. **Task 3.2** (AppContext) → After Task 1.5
11. **Task 3.3** (API testing) → After Task 3.1
12. **Task 4.1** (Goals menu) → After Task 1.5
13. **Task 4.2** (Create/view goals) → After Task 4.1
14. **Task 4.3** (Add savings) → After Task 4.2
15. **Task 4.4** (Update/delete) → After Task 4.3
16. **Task 4.5** (Console testing) → After Tasks 4.2, 4.3, 4.4
17. **Task 5.1** (CSS styles) → Can start anytime
18. **Task 5.2** (goal-planner.js) → After Task 3.1
19. **Task 5.3** (Dashboard HTML) → After Tasks 5.1, 5.2
20. **Task 5.4** (UI testing) → After Task 5.3
21. **Task 6.1** (Calculator tests) → After Task 1.4
22. **Task 6.2** (Tracker tests) → After Task 2.1
23. **Task 6.3** (Property tests) → After Tasks 1.4, 2.1
24. **Task 6.4** (Integration tests) → After Tasks 3.3, 4.5, 5.4
25. **Task 6.5** (Performance) → After Task 6.4
26. **Task 6.6** (Final polish) → After all other tasks

## Execution Order

Tasks should be executed in this order for optimal efficiency:

1. Task 1.1
2. Task 1.2
3. Task 1.3
4. Task 1.4
5. Task 1.5
6. Task 2.1
7. Task 2.2
8. Task 2.3
9. Task 3.2
10. Task 3.1
11. Task 3.3
12. Task 4.1
13. Task 4.2
14. Task 4.3
15. Task 4.4
16. Task 4.5
17. Task 5.1
18. Task 5.2
19. Task 5.3
20. Task 5.4
21. Task 6.1
22. Task 6.2
23. Task 6.3
24. Task 6.4
25. Task 6.5
26. Task 6.6