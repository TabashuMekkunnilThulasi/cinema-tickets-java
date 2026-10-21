# Cinema Tickets - Java

## Overview

This project implements a cinema ticket purchasing service in Java as part of the DWP Software Engineer coding exercise.

The service validates ticket purchase requests, calculates the total payment amount and determines the number of seats to reserve.

## Business Rules

The service supports three ticket types:

| Ticket Type | Price | Seat Required |
|---|---|---|
| Adult | £25 | Yes |
| Child | £15 | Yes |
| Infant | £0 | No |

- A maximum of 25 tickets can be purchased in a single transaction, including infants.
- At least one adult ticket is required for a valid purchase.
- Child and infant tickets cannot be purchased without an adult ticket.
- Infants sit on an adult's lap and do not require a separate seat.
- Invalid purchases are rejected before payment or seat reservation is requested.

## Implementation

`TicketServiceImpl` implements the provided `TicketService` interface.

The service uses constructor injection for `TicketPaymentService` and `SeatReservationService`.

For each purchase, it:

1. Validates the account ID and ticket requests.
2. Calculates the total ticket quantity and checks the 25-ticket limit.
3. Checks that the purchase includes an adult ticket.
4. Calculates the payment amount based on ticket prices.
5. Calculates the required seats, excluding infants.
6. Requests payment and seat reservation using the provided service interfaces.

`TicketTypeRequest` is immutable, with final fields and no setters.

The provided `TicketService` interface and `thirdparty` packages have not been modified.

## Technologies

- Java 21
- Maven
- JUnit 5
- Mockito

## Running the Tests

Ensure Java 21 and Maven are installed.

From the `cinema-tickets-java` directory, run:

```bash
mvn clean verify
```

This compiles the project, runs the automated tests and builds the JAR.

## Testing

The project includes 22 automated tests covering:

- Invalid account IDs
- Null and empty ticket requests
- Invalid ticket quantities and ticket types
- Purchases without an adult
- Purchases exceeding 25 tickets
- Purchases containing exactly 25 tickets
- Ticket price calculations
- Seat reservation calculations
- Infants being excluded from seat allocation
- Infants being included in the total ticket limit
- Prevention of external service calls for invalid purchases
- Constructor dependency validation

## Assumptions

- Account IDs greater than zero are valid and have sufficient funds.
- The external payment and seat reservation services operate successfully, as specified in the exercise.
- Ticket quantities must be positive.
- The presence of at least one adult ticket satisfies the adult accompaniment requirement; the specification does not explicitly define a maximum number of infants per adult.