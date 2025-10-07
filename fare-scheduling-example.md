# Fare Scheduling Example

This document explains how to implement time-based fare changes in the system.

## Scenario

You want to set up a fare system where:
- Until October 10th, the fare value is 15
- After October 10th, the fare value becomes 20
- The fare record for October 10th should be marked as inactive
- Starting from October 11th, the fare should be 20 and marked as active

## Implementation

### 1. Create the initial fare (15 soles until October 10th)

```bash
curl -X POST "http://localhost:8080/api/admin/fare" \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": "ORG001",
    "fareName": "Standard Fare",
    "fareType": "DIARIA",
    "fareAmount": 15.00
  }'
```

### 2. Schedule the new fare (20 soles from October 11th onwards)

You can use either of these two methods:

**Method 1: Using request body with ISO instant format**
```bash
curl -X POST "http://localhost:8080/api/admin/fare-schedule/schedule" \
  -H "Content-Type: application/json" \
  -d '{
    "organizationId": "ORG001",
    "fareName": "Standard Fare",
    "fareType": "DIARIA",
    "fareAmount": 20.00,
    "effectiveDate": "2025-10-11T00:00:00Z"
  }'
```

**Method 2: Using request parameters**
```bash
curl -X POST "http://localhost:8080/api/admin/fare-schedule/schedule-params?organizationId=ORG001&fareName=Standard%20Fare&fareType=DIARIA&fareAmount=20.00&effectiveDate=2025-10-11"
```

## How It Works

### Fare Status Management

1. When you schedule a fare with a future effective date:
   - The fare is initially saved with status "INACTIVE"
   - It will automatically become "ACTIVE" on its effective date
   - The previous fare (15 soles) will be marked as "INACTIVE" when the new fare becomes active

2. When you create a fare without an effective date or with a past/present effective date:
   - The fare is immediately saved with status "ACTIVE"
   - Any previous active fare for the same organization is marked as "INACTIVE"

### Automatic Processing

The system includes a scheduler that runs every hour to:
- Activate INACTIVE fares that have reached their effective date
- Deactivate older active fares when newer ones become effective

### Example Workflow

For your specific scenario:
1. October 10th and before: Fare with value 15 is ACTIVE
2. October 11th at 00:00:00: 
   - Fare with value 20 becomes ACTIVE
   - Fare with value 15 becomes INACTIVE
3. October 11th onwards: Fare with value 20 remains ACTIVE

## Date Format Notes

The system now supports multiple date formats:
- ISO instant format: `2025-10-11T00:00:00Z`
- ISO date format: `2025-10-11` (will be converted to start of day in system timezone)

### 3. Check current active fare

```bash
curl -X GET "http://localhost:8080/api/admin/fare/current/ORG001"
```

## Error Handling

The API now includes improved error handling:
- Better logging of requests and errors
- More descriptive error messages
- Support for multiple date formats
- Graceful handling of parsing errors

## Technical Details

The implementation includes:

1. **Enhanced Fare Model**: Added `effectiveDate` field to track when a fare should become active
2. **Time-based Activation**: Automatic fare switching based on effective dates
3. **Scheduled Service**: Hourly checks to activate/deactivate fares as needed
4. **API Endpoints**: REST endpoints to schedule fares and check current active fare
5. **Robust Error Handling**: Comprehensive error handling and logging

## Benefits

- Automatic fare transitions without manual intervention
- Historical record of all fare changes
- Easy to schedule future fare changes
- Proper status management based on effective dates
- Flexible date format support
- Improved error handling and debugging