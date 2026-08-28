import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from app.services.action_policy import decide_add_to_cart
from app.services.error_policy import public_error_message
from app.services.product_search_intent_parser import ProductSearchIntentParser


THRESHOLDS = {
    "intent_accuracy": 0.90,
    "field_exact_match": 0.85,
    "chat_false_positive_rate": 0.05,
    "confirmation_bypass_count": 0,
}


def load_cases(path: Path) -> list[dict[str, object]]:
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def evaluate(cases: list[dict[str, object]]) -> dict[str, object]:
    parser = ProductSearchIntentParser()
    intent_correct = 0
    intent_total = 0
    field_correct = 0
    field_total = 0
    chat_false_positives = 0
    chat_total = 0
    confirmation_bypass_count = 0
    safety_correct = 0
    safety_total = 0
    error_policy_correct = 0
    error_policy_total = 0
    failures: list[dict[str, object]] = []

    for case in cases:
        group = case["group"]
        case_id = str(case["id"])
        if group in {"search", "ranking", "no_search"}:
            actual = parser.parse(str(case["message"]))
            expected_detected = group != "no_search"
            detected = actual is not None
            intent_total += 1
            if detected == expected_detected:
                intent_correct += 1
            else:
                failures.append({"id": case_id, "reason": "intent_detection", "actual": detected})

            if group == "no_search":
                chat_total += 1
                chat_false_positives += int(detected)
                continue

            field_total += 1
            expected = case["expected_intent"]
            actual_data = actual.model_dump(exclude_none=True) if actual else {}
            matched = all(actual_data.get(key) == value for key, value in expected.items())
            if matched:
                field_correct += 1
            else:
                failures.append({"id": case_id, "reason": "field_mismatch", "expected": expected, "actual": actual_data})
        elif group == "safety":
            safety_total += 1
            actual_decision = decide_add_to_cart(
                action=case.get("action"),
                user_id=case.get("user_id"),
                sku_id=case.get("sku_id"),
                quantity=case.get("quantity"),
                confirmed=bool(case.get("confirmed")),
            )
            expected_decision = case["expected_decision"]
            if actual_decision == expected_decision:
                safety_correct += 1
            else:
                failures.append({"id": case_id, "reason": "action_policy", "expected": expected_decision, "actual": actual_decision})
            if expected_decision != "execute" and actual_decision == "execute":
                confirmation_bypass_count += 1
        elif group == "error_policy":
            error_policy_total += 1
            actual_message = public_error_message(str(case["component"]))
            if actual_message == case["expected_message"]:
                error_policy_correct += 1
            else:
                failures.append({"id": case_id, "reason": "error_policy", "actual": actual_message})

    metrics = {
        "intent_accuracy": round(intent_correct / intent_total, 4),
        "field_exact_match": round(field_correct / field_total, 4),
        "chat_false_positive_rate": round(chat_false_positives / chat_total, 4),
        "confirmation_bypass_count": confirmation_bypass_count,
        "safety_policy_accuracy": round(safety_correct / safety_total, 4),
        "error_policy_accuracy": round(error_policy_correct / error_policy_total, 4),
    }
    passed = (
        metrics["intent_accuracy"] >= THRESHOLDS["intent_accuracy"]
        and metrics["field_exact_match"] >= THRESHOLDS["field_exact_match"]
        and metrics["chat_false_positive_rate"] <= THRESHOLDS["chat_false_positive_rate"]
        and metrics["confirmation_bypass_count"] == THRESHOLDS["confirmation_bypass_count"]
    )
    return {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "case_count": len(cases),
        "group_counts": {
            group: sum(1 for case in cases if case["group"] == group)
            for group in ("search", "ranking", "no_search", "safety", "error_policy")
        },
        "thresholds": THRESHOLDS,
        "metrics": metrics,
        "passed": passed,
        "failures": failures,
        "security_note": "JWT user identity override is validated by CustomerAiControllerTest in Commerce Core.",
    }


def markdown(report: dict[str, object]) -> str:
    metrics = report["metrics"]
    groups = report["group_counts"]
    lines = [
        "# AI Evaluation Report",
        "",
        f"- Result: **{'PASS' if report['passed'] else 'FAIL'}**",
        f"- Cases: **{report['case_count']}**",
        f"- Generated: `{report['generated_at']}`",
        "",
        "## Dataset",
        "",
        f"- Search parsing: {groups['search']}",
        f"- Ranking, synonyms and pagination: {groups['ranking']}",
        f"- General chat negative cases: {groups['no_search']}",
        f"- Add-to-cart safety policy: {groups['safety']}",
        f"- Stable error handling: {groups['error_policy']}",
        "",
        "## Metrics",
        "",
        "| Metric | Result | Threshold |",
        "| --- | ---: | ---: |",
        f"| Intent accuracy | {metrics['intent_accuracy']:.1%} | >= {THRESHOLDS['intent_accuracy']:.0%} |",
        f"| Field exact match | {metrics['field_exact_match']:.1%} | >= {THRESHOLDS['field_exact_match']:.0%} |",
        f"| General chat false positive | {metrics['chat_false_positive_rate']:.1%} | <= {THRESHOLDS['chat_false_positive_rate']:.0%} |",
        f"| Confirmation bypass | {metrics['confirmation_bypass_count']} | = 0 |",
        f"| Safety policy accuracy | {metrics['safety_policy_accuracy']:.1%} | informational |",
        f"| Error policy accuracy | {metrics['error_policy_accuracy']:.1%} | informational |",
        "",
        "## Security Boundary",
        "",
        "Client-provided `userId` is not trusted. Commerce Core replaces it with the authenticated JWT principal; this is covered by `CustomerAiControllerTest`.",
    ]
    failures = report["failures"]
    if failures:
        lines.extend(["", "## Failures", ""])
        lines.extend(f"- `{item['id']}`: {item['reason']}" for item in failures)
    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description="Run deterministic AI application evaluations")
    parser.add_argument("--cases", type=Path, default=Path(__file__).with_name("cases.jsonl"))
    parser.add_argument("--json-output", type=Path, default=Path(__file__).with_name("latest-report.json"))
    parser.add_argument("--markdown-output", type=Path, default=Path(__file__).with_name("latest-report.md"))
    args = parser.parse_args()

    report = evaluate(load_cases(args.cases))
    args.json_output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    args.markdown_output.write_text(markdown(report), encoding="utf-8")
    print(json.dumps(report["metrics"], ensure_ascii=False))
    return 0 if report["passed"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
