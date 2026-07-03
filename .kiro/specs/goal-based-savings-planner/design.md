# Technical Design Document

## Goal-Based Savings Planner

## 1. Architecture Overview

The Goal-Based Savings Planner feature follows the established Model-Repository-Service pattern in the Smart Expense Tracker codebase, extending it with specialized calculation and recommendation components.

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  ConsoleApp              │     ApiHandler                       │
│  (Console UI)            │     (REST API)                       │
└────────────┬────────────┴────────────┬──────────────────────────┘
             │                         │
             ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                               │
├──────────────────────┬────────────────┬─────────────────────────┤
│   GoalService        │ GoalCalculator │ GoalRecommendationSvc   │
│   (Business Logic)   │ (Calculations) │ (Recommendations)       │
└──────────┬───────────┴───────┬────────┴───────────┬────────────┘
           │                   │                    │
           ▼                   ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Repository Layer                            │
├─────────────────────────────────────────────────────────────────┤
│                    GoalRepository                                │
│              (Data Persistence & Retrieval)                      │
└─────────────────────────────┬───────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Storage Layer                               │
├─────────────────────────────────────────────────────────────────┤
│                  goals.json (JSON File Storage)                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Integration with Existing Architecture

The Goal feature integrates seamlessly with existing components:

- **AppContext**: Will be extended to provide GoalService instance
- **ApiHandler**: New route handlers for `/api/goals/*` endpoints
- **ConsoleApp**: New "Goals" menu option between "Budgets" and "Monthly Report"
- **Storage**: Uses existing `JsonFileStorage` infrastructure for `goals.json`
- **UI**: Glassmorphism cards integrated into existing dashboard

## 2. Data Model

### 2.1 Goal Entity

```java
package com.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;

public class Goal {
    // Core fields
    private String id;
    private String name;
    private double targetAmount;
    private double currentSavings;
    private LocalDate targetDate;
    private Integer targetDurationMonths;
    private String icon;
    private GoalStatus status;
    
    // Metadata
    private LocalDate createdAt;
    
    // Calculated fields (transient, not persisted)
    private double progressPercent;
    private double remainingAmount;
    private double requiredMonthlySavings;
    private double dailySavingsTarget;
    private int daysRemaining;
    private LocalDate estimatedCompletionDate;
    
    public enum GoalStatus {
        ON_TRACK("On Track"),
        BEHIND_SCHEDULE("Behind Schedule"),
        ACHIEVED("Achieved");
        
        private final String displayName;
        // ...
    }
    
    // Constructors
    public Goal() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDate.now();
        this.currentSavings = 0.0;
        this.status = GoalStatus.ON_TRACK;
    }
    
    public Goal(String name, double targetAmount, LocalDate targetDate) {
        this();
        this.name = name;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.icon = generateIcon(name);
    }
    
    // Getters and Setters
    // ...
    
    // Helper methods
    private String generateIcon(String name) {
        // Predefined icon mappings
        Map<String, String> iconMap = Map.ofEntries(
            Map.entry("laptop", "💻"),
            Map.entry("trip", "✈️"),
            Map.entry("travel", "✈️"),
            Map.entry("vacation", "🌴"),
            Map.entry("emergency fund", "🛡️"),
            Map.entry("emergency", "🛡️"),
            Map.entry("car", "🚗"),
            Map.entry("vehicle", "🚗"),
            Map.entry("house", "🏠"),
            Map.entry("home", "🏠"),
            Map.entry("wedding", "💒"),
            Map.entry("education", "📚"),
            Map.entry("school", "📚"),
            Map.entry("college", "🎓"),
            Map.entry("retirement", "🏖️"),
            Map.entry("phone", "📱"),
            Map.entry("computer", "🖥️"),
            Map.entry("bike", "🚲"),
            Map.entry("motorcycle", "🏍️")
        );
        
        String lowerName = name.toLowerCase();
        for (Map.Entry<String, String> entry : iconMap.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "💰"; // Default icon
    }
}
```

### 2.2 GoalSummary DTO

```java
package com.expensetracker.model;

public class GoalSummary {
    private int activeGoalsCount;
    private double totalTargetAmount;
    private double totalSavedAmount;
    private double overallProgressPercent;
    
    public GoalSummary(int activeGoalsCount, double totalTargetAmount, 
                      double totalSavedAmount, double overallProgressPercent) {
        this.activeGoalsCount = activeGoalsCount;
        this.totalTargetAmount = totalTargetAmount;
        this.totalSavedAmount = totalSavedAmount;
        this.overallProgressPercent = overallProgressPercent;
    }
    
    // Getters
}
```

## 3. Class Diagram

### 3.1 Core Components

```
┌───────────────────────────────────────────────────────────────────┐
│                         Goal (Model)                               │
├───────────────────────────────────────────────────────────────────┤
│ - id: String                                                       │
│ - name: String                                                     │
│ - targetAmount: double                                             │
│ - currentSavings: double                                           │
│ - targetDate: LocalDate                                            │
│ - targetDurationMonths: Integer                                    │
│ - icon: String                                                     │
│ - status: GoalStatus                                               │
│ - createdAt: LocalDate                                             │
├───────────────────────────────────────────────────────────────────┤
│ + Goal()                                                           │
│ + Goal(name: String, targetAmount: double, targetDate: LocalDate)  │
│ + getProgressPercent(): double                                     │
│ + getRemainingAmount(): double                                     │
│ + getDaysRemaining(): int                                          │
│ + isAchieved(): boolean                                            │
│ + isOnTrack(): boolean                                             │
└───────────────────────────────────────────────────────────────────┘
                                    ▲
                                    │
┌───────────────────────────────────────────────────────────────────┐
│                      GoalRepository                                │
├───────────────────────────────────────────────────────────────────┤
│ - storage: JsonFileStorage<Goal>                                   │
│ - goals: List<Goal>                                                │
├───────────────────────────────────────────────────────────────────┤
│ + GoalRepository(dataDir: Path)                                    │
│ + findAll(): List<Goal>                                            │
│ + findById(id: String): Optional<Goal>                             │
│ + save(goal: Goal): Goal                                           │
│ + delete(id: String): boolean                                      │
│ + update(goal: Goal): Goal                                         │
│ + findByStatus(status: GoalStatus): List<Goal>                     │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                        GoalService                                 │
├───────────────────────────────────────────────────────────────────┤
│ - goalRepository: GoalRepository                                   │
│ - goalCalculator: GoalCalculator                                   │
│ - goalProgressTracker: GoalProgressTracker                         │
├───────────────────────────────────────────────────────────────────┤
│ + GoalService(repository: GoalRepository,                          │
│               calculator: GoalCalculator,                          │
│               progressTracker: GoalProgressTracker)                │
│ + createGoal(name: String, targetAmount: double,                   │
│              currentSavings: double, targetDate: LocalDate,        │
│              targetDurationMonths: Integer): Goal                  │
│ + getAllGoals(): List<Goal>                                        │
│ + getGoalById(id: String): Optional<Goal>                          │
│ + addSavings(goalId: String, amount: double): Goal                 │
│ + updateGoal(goal: Goal): Goal                                      │
│ + deleteGoal(id: String): boolean                                   │
│ + getGoalSummary(): GoalSummary                                     │
│ + getGoalsByStatus(status: GoalStatus): List<Goal>                 │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                      GoalCalculator                                │
├───────────────────────────────────────────────────────────────────┤
│ + calculateProgressPercent(goal: Goal): double                     │
│ + calculateRemainingAmount(goal: Goal): double                      │
│ + calculateRequiredMonthlySavings(goal: Goal): double              │
│ + calculateDailySavingsTarget(goal: Goal): double                  │
│ + calculateDaysRemaining(goal: Goal): int                          │
│ + calculateEstimatedCompletionDate(goal: Goal): LocalDate          │
│ + calculateAll(goal: Goal): void                                   │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                   GoalProgressTracker                              │
├───────────────────────────────────────────────────────────────────┤
│ + determineStatus(goal: Goal): GoalStatus                          │
│ + updateGoalStatus(goal: Goal): void                               │
│ + isOnTrack(goal: Goal): boolean                                   │
│ + isBehindSchedule(goal: Goal): boolean                            │
│ + isAchieved(goal: Goal): boolean                                  │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│                 GoalRecommendationService                          │
├───────────────────────────────────────────────────────────────���───┤
│ + getRecommendation(goal: Goal): String                            │
│ + getOnTrackMessage(goal: Goal): String                            │
│ + getBehindScheduleMessage(goal: Goal): String                     │
│ + getAchievedMessage(goal: Goal): String                           │
│ + getAheadOfScheduleMessage(goal: Goal): String                    │
└───────────────────────────────────────────────────────────────────┘
```

## 4. API Design

### 4.1 REST Endpoints

All endpoints follow the existing `ApiHandler` patterns and return JSON responses.

#### GET /api/goals

**Description**: Retrieve all goals with calculated fields

**Response** (HTTP 200):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Buy Laptop",
    "targetAmount": 100000,
    "currentSavings": 25000,
    "targetDate": "2025-06-30",
    "targetDurationMonths": 6,
    "icon": "💻",
    "status": "On Track",
    "progressPercent": 25.0,
    "remainingAmount": 75000,
    "requiredMonthlySavings": 12500,
    "daysRemaining": 90,
    "estimatedCompletionDate": "2025-06-30"
  }
]
```

#### GET /api/goals/{id}

**Description**: Retrieve a specific goal by ID

**Response** (HTTP 200): Single goal object as above

**Error** (HTTP 404): `{"error": "Goal not found"}`

#### POST /api/goals

**Description**: Create a new savings goal

**Request Body**:
```json
{
  "name": "Europe Trip",
  "targetAmount": 200000,
  "currentSavings": 50000,
  "targetDate": "2025-12-31",
  "targetDurationMonths": null
}
```
*Note: Either `targetDate` or `targetDurationMonths` is required, not both.*

**Response** (HTTP 201): Created goal object

**Error** (HTTP 400): `{"error": "All fields are required: name, target amount, and either target date or duration"}`

#### PUT /api/goals/{id}

**Description**: Update goal details

**Request Body** (any subset of fields):
```json
{
  "name": "Updated Goal Name",
  "targetAmount": 150000,
  "currentSavings": 30000,
  "targetDate": "2025-08-31"
}
```

**Response** (HTTP 200): Updated goal object

**Error** (HTTP 404): `{"error": "Goal not found"}`

#### DELETE /api/goals/{id}

**Description**: Delete a goal

**Response** (HTTP 204): No content

**Error** (HTTP 404): `{"error": "Goal not found"}`

#### POST /api/goals/{id}/deposit

**Description**: Add savings to a goal

**Request Body**:
```json
{
  "amount": 5000
}
```

**Response** (HTTP 200): Updated goal object

**Error** (HTTP 400): `{"error": "Amount must be greater than zero"}`

**Error** (HTTP 404): `{"error": "Goal not found"}`

### 4.2 Error Handling

All error responses follow existing patterns:
```json
{
  "error": "Descriptive error message"
}
```

HTTP Status Codes:
- 200: Success
- 201: Created
- 204: No Content
- 400: Bad Request (validation errors)
- 404: Not Found
- 405: Method Not Allowed
- 500: Internal Server Error

## 5. Console Menu Structure

### 5.1 Main Menu Integration

```
--- Main Menu ---
1. Expenses
2. Categories
3. Budgets
4. Goals          ← NEW
5. Monthly Report
0. Exit
```

### 5.2 Goals Submenu

```
--- Goals ---
1. View all goals
2. Create new goal
3. Add savings to goal
4. Update goal details
5. Delete goal
0. Back
```

### 5.3 Console Workflow Examples

#### Creating a Goal
```
--- Create New Goal ---
Goal name: Europe Trip
Target amount: 200000
Current savings (optional, press Enter for 0): 50000

Choose deadline type:
1. Target date (yyyy-MM-dd)
2. Duration in months
Enter choice (1 or 2): 1
Target date (yyyy-MM-dd): 2025-12-31

Goal created successfully! 💰
Progress: 25.0%
Monthly savings required: ₹12,500/month
Days remaining: 180
Status: On Track
```

#### Adding Savings
```
--- Add Savings to Goal ---
Enter goal ID or name: Europe
[1] Europe Trip - ₹200,000 target - 25% complete
Select goal number: 1
Enter amount to add: 10000

₹10,000 added to Europe Trip!
New balance: ₹60,000 / ₹200,000 (30%)
```

## 6. UI/UX Design

### 6.1 Dashboard Integration

The Goal Planner section appears between "Smart Spending Insights" and "Monthly Reports" on the main dashboard.

### 6.2 Goal Summary Card

```
┌─────────────────────────────────────────────────────────────────┐
│  💰 Goal Summary                                  [View All ▸]  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│    Active Goals        Total Target        Total Saved          │
│         3              ₹500,000            ₹125,000             │
│                                                                 │
│    ─────────────────────────────────────────────────────────   │
│                                                                 │
│    Overall Progress                                            │
│    ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  25%          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Goal Card Design

```
┌─────────────────────────────────────────────────────────────────┐
│  💻 Buy Laptop                                  [View Details]  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│    ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  25%  │
│                                                                 │
│    ₹25,000 / ₹100,000                                           │
│                                                                 │
│    Required: ₹12,500/month    Days left: 90    Status: On Track │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 CSS Specifications

#### Glassmorphism Card
```css
.goal-card {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius, 16px);
    box-shadow: var(--shadow, 0 8px 32px);
    margin: 16px;
    padding: 20px;
    transition: all 0.2s ease;
}

.goal-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}
```

#### Progress Bar
```css
.goal-progress-bar {
    height: 8px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    overflow: hidden;
    margin: 12px 0;
}

.goal-progress-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 0.3s ease;
}

.goal-progress-fill.achieved {
    background: linear-gradient(90deg, #22C55E, #4ADE80);
}

.goal-progress-fill.on-track {
    background: linear-gradient(90deg, #3B82F6, #60A5FA);
}

.goal-progress-fill.behind-schedule {
    background: linear-gradient(90deg, #F97316, #FB923C);
}
```

#### Status Badge
```css
.goal-status-badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
}

.goal-status-badge.achieved {
    background: rgba(34, 197, 94, 0.2);
    color: #22C55E;
}

.goal-status-badge.on-track {
    background: rgba(59, 130, 246, 0.2);
    color: #3B82F6;
}

.goal-status-badge.behind-schedule {
    background: rgba(249, 115, 22, 0.2);
    color: #F97316;
}
```

#### Responsive Grid
```css
.goal-planner-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

@media (min-width: 768px) {
    .goal-planner-section {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
    }
    
    .goal-summary-card {
        grid-column: 1 / -1;
    }
}
```

### 6.5 Interactive Elements

#### View Details Modal
```javascript
function showGoalDetails(goalId) {
    // Fetch goal details via API
    // Display modal with:
    // - Goal name and icon
    // - Progress bar with percentage
    // - Current vs Target amounts
    // - Monthly savings required
    // - Days remaining
    // - Estimated completion date
    // - Recommendation message
    // - Edit button
    // - Delete button
}
```

## 7. File Structure

### 7.1 Java Source Files

```
src/main/java/com/expensetracker/
├── model/
│   └── Goal.java                          [NEW]
│   └── GoalStatus.java                    [NEW]
│   └── GoalSummary.java                   [NEW]
├── repository/
│   └── GoalRepository.java                [NEW]
├── service/
│   └── GoalService.java                   [NEW]
│   └── GoalCalculator.java                [NEW]
│   └── GoalProgressTracker.java           [NEW]
│   └── GoalRecommendationService.java     [NEW]
├── ui/
│   └── ConsoleGoalHandler.java            [NEW - optional, integrated into ConsoleApp]
└── web/
    └── GoalApiHandler.java                [NEW - integrated into ApiHandler]
```

### 7.2 Resource Files

```
src/main/resources/
├── web/
│   ├── js/
│   │   └── goal-planner.js                [NEW]
│   └── css/
│       └── style.css                      [MODIFIED - add goal styles]
```

### 7.3 Spec Files

```
.kiro/specs/goal-based-savings-planner/
├── .config.kiro
├── requirements.md                        [EXISTING]
├── design.md                              [THIS DOCUMENT]
└── tasks.md                               [TO BE CREATED]
```

## 8. Correctness Properties

### 8.1 Invariants

**Invariant 1**: Completion Percentage Bounds
```
FOR ALL Goal g:
  IF g.currentSavings < g.targetAmount THEN
    0 <= g.progressPercent < 100
  ELSE
    g.progressPercent == 100
```

**Invariant 2**: Remaining Amount Consistency
```
FOR ALL Goal g:
  g.remainingAmount == g.targetAmount - g.currentSavings
```

**Invariant 3**: Goal Status Validity
```
FOR ALL Goal g:
  g.status ∈ {ON_TRACK, BEHIND_SCHEDULE, ACHIEVED}
```

### 8.2 Metamorphic Properties

**Metamorphic 1**: Required Monthly Savings Decrease
```
FOR ALL Goal g, FOR ALL Date d > g.targetDate:
  LET g2 = g with targetDate = d
  LET months1 = monthsBetween(g.createdAt, g.targetDate)
  LET months2 = monthsBetween(g.createdAt, d)
  LET required1 = g.requiredMonthlySavings
  LET required2 = g2.requiredMonthlySavings
  
  IF months2 > months1 THEN
    required2 <= required1
```

**Metamorphic 2**: Savings Progress Monotonicity
```
FOR ALL Goal g, FOR ALL amounts a1, a2 WHERE 0 <= a1 < a2 <= g.targetAmount:
  LET g1 = g with currentSavings = a1
  LET g2 = g with currentSavings = a2
  
  g1.progressPercent < g2.progressPercent
```

### 8.3 Round-Trip Properties

**Round-Trip 1**: Serialization/Deserialization
```
FOR ALL Goal g:
  LET json = toJson(g)
  LET g2 = fromJson(json)
  
  g.id == g2.id AND
  g.name == g2.name AND
  g.targetAmount == g2.targetAmount AND
  g.currentSavings == g2.currentSavings AND
  g.targetDate == g2.targetDate AND
  g.status == g2.status
```

## 9. Testing Strategy

### 9.1 Unit Testing (Specific Examples)

Test specific scenarios with representative examples:

1. **Goal Creation**
   - Create goal with target date
   - Create goal with duration
   - Create goal with initial savings
   - Validation: empty name, zero target, past date

2. **Calculations**
   - 0% progress
   - 50% progress
   - 100% progress (achieved)
   - Behind schedule scenario
   - Ahead of schedule scenario

3. **Status Determination**
   - On track with exact pace
   - Behind schedule (>10% variance)
   - On track (<10% variance)
   - Achieved (100% complete)

### 9.2 Property-Based Testing (Universal Properties)

Test correctness properties across 100+ random inputs:

1. Completion percentage always in [0, 100]
2. Remaining amount = target - current
3. Required monthly savings decreases when deadline extends
4. Progress increases monotonically with savings
5. Serialization round-trip preserves data

### 9.3 Integration Testing

1. Complete goal lifecycle: Create → Add savings → Verify status → Achieve
2. API round-trip: POST → GET → PUT → DELETE
3. Console goal management workflow
4. Dashboard integration rendering
5. Data persistence across application restart

## 10. Implementation Roadmap

### Phase 1: Core Infrastructure
- [ ] Create Goal model with all fields
- [ ] Create GoalRepository for JSON persistence
- [ ] Create GoalCalculator for all calculations
- [ ] Create GoalService as facade

### Phase 2: Business Logic
- [ ] Implement GoalProgressTracker
- [ ] Implement GoalRecommendationService
- [ ] Integrate calculations into Goal model

### Phase 3: API Integration
- [ ] Add goal endpoints to ApiHandler
- [ ] Add GoalService to AppContext
- [ ] Test all REST endpoints

### Phase 4: Console Integration
- [ ] Add Goals menu to ConsoleApp
- [ ] Implement all console workflows
- [ ] Test console user experience

### Phase 5: Web UI
- [ ] Add CSS styles for goal cards
- [ ] Create goal-planner.js for API integration
- [ ] Update dashboard HTML
- [ ] Test responsive design

### Phase 6: Testing & Polish
- [ ] Write unit tests
- [ ] Write property-based tests
- [ ] Write integration tests
- [ ] Performance testing
- [ ] Bug fixes and refinements

## 11. Dependencies

### 11.1 External Dependencies

- **Gson** (already in pom.xml): JSON serialization/deserialization
- **Java 17+**: LocalDate, UUID, streams, etc.

### 11.2 Internal Dependencies

- **JsonFileStorage**: Reused for goals.json persistence
- **AppContext**: To provide GoalService to handlers
- **HttpUtil**: For API response formatting

## 12. Security Considerations

### 12.1 Input Validation

All user inputs must be validated:
- Goal name: 1-100 characters, non-empty after trim
- Target amount: 0.01 - 9,999,999.99
- Current savings: 0.00 - target amount
- Target date: Must be future date (or allow past if goal already achieved)
- Deposit amount: 0.01 - 9,999,999.99

### 12.2 Error Handling

- Return HTTP 400 for validation errors with descriptive messages
- Return HTTP 404 for non-existent goals
- Never expose internal error details to clients
- Log errors server-side for debugging

### 12.3 Data Integrity

- Use UUID for goal IDs (no sequential IDs to prevent enumeration)
- Validate goal ownership? (Single-user app, no ownership concept)
- Prevent negative balances
- Enforce target >= current savings