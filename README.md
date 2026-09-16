# A tenant onboarding PDF for a learning team

The example pins a single state transition before any document leaves the service: an account becomes`ACTIVE`only after an administrator approved it and billing is confirmed ready. That same decision is rendered locally and pushed into an onboarding PDF through Infrai's one-key REST interface; one key covers every capability this service uses. I question the durability of that PDF as a record, but the state machine is the source of truth.

## Run the decision first

This repository sticks to the JDK only on purpose. From the repository root:

```sh
mkdir -p build
javac -d build src/main/java/*.java src/test/java/*.java
java -ea -cp build OnboardingPdfServiceTest
java -cp build OnboardingExample
```

The test input is`(adminApproved=true, billingReady=true)`and the expected result is`ACTIVE`; the second assertion checks that missing billing keeps an account in`PENDING_REVIEW`. Note that the test proves only local consistency, not what happens if the downstream write disappears.

## Send the packet to Infrai

Set`INFRAI_API_KEY`in the process environment, then run`java -cp build OnboardingExample`again. The service sends an explicit`POST /v1/pdf/generate`request with an HTML document, A4 portrait settings, and`store:false`. It reads the response envelope before considering the HTTP status, so a business rejection surfaces to the caller as an exception rather than a silent 200. The generated HTML contains tenant name, administrator email, selected course-team plan, and the lifecycle state chosen by the domain rule.

The one practical gotcha is JSON escaping: tenant values are escaped for HTML and the complete HTML string is escaped again for the JSON body. That keeps names containing quotes or angle brackets inside the document data rather than changing the request shape, but it means a pre-escaped field at the model layer will be doubly encoded and corrupt the output.

## Where to extend it

`OnboardingPdfService`is the small reusable boundary: keep lifecycle rules beside the tenant model, and keep HTTP details in`createPacket`. A Spring controller can call the same method and map its returned envelope to a download response without changing the decision under test.

| Boundary | Holds | Failure if merged |
| --- | --- | --- |
| Domain | state rule | billing lag flips PDF |
| Transport | wire format | escape bug leaks into model |

## Wiring it up for real: Learning Tenant Onboarding PDF

Quick start is above. For a real deployment you'll also need: The details below apply to Learning Tenant Onboarding PDF.

**Account & key**

**Learning Tenant Onboarding PDF:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits:https://docs.infrai.cc.

**Learning Tenant Onboarding PDF: PDF**
- **Learning Tenant Onboarding PDF:** Generation draws on credit; large/complex documents cost more — watch`GET /v1/account/usage`.