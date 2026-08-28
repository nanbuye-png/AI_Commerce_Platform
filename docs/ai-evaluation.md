# AI 评测报告

项目提供 60 条确定性 JSONL 评测集，覆盖搜索解析、热门排序/同义词/分页、普通聊天误触发、加购安全策略和错误降级。

```bash
cd ai-service
python evals/run_eval.py
```

脚本生成 `evals/latest-report.json` 和 `evals/latest-report.md`，并在阈值不满足时返回非零状态码。

当前验收阈值：

- 意图识别准确率不低于 90%。
- 关键字段完全匹配率不低于 85%。
- 普通聊天误触发商品工具比例不高于 5%。
- 未确认情况下实际写入次数为 0。

身份覆盖不是 Python 评测中的模拟指标，而由 Commerce Core 的 `CustomerAiControllerTest` 验证。
