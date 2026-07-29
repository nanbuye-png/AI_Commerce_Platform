package com.commerce.platform.returns.application.command;

public class ApproveReturnCommand {
    private final Long returnId;
    public ApproveReturnCommand(Long returnId) { this.returnId = returnId; }
    public Long getReturnId() { return returnId; }
}