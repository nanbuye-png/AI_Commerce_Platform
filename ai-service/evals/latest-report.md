# AI Evaluation Report

- Result: **PASS**
- Cases: **60**
- Generated: `2026-08-27T14:21:21.090518+00:00`

## Dataset

- Search parsing: 25
- Ranking, synonyms and pagination: 10
- General chat negative cases: 10
- Add-to-cart safety policy: 10
- Stable error handling: 5

## Metrics

| Metric | Result | Threshold |
| --- | ---: | ---: |
| Intent accuracy | 100.0% | >= 90% |
| Field exact match | 85.7% | >= 85% |
| General chat false positive | 0.0% | <= 5% |
| Confirmation bypass | 0 | = 0 |
| Safety policy accuracy | 100.0% | informational |
| Error policy accuracy | 100.0% | informational |

## Security Boundary

Client-provided `userId` is not trusted. Commerce Core replaces it with the authenticated JWT principal; this is covered by `CustomerAiControllerTest`.

## Failures

- `s08`: field_mismatch
- `s09`: field_mismatch
- `s10`: field_mismatch
- `s16`: field_mismatch
- `r09`: field_mismatch
