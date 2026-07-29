package com.commerce.platform.warehouse.domain.exception;

import com.commerce.platform.common.exception.BusinessException;

/**
 * 任务状态变更异常
 * <p>
 * 当拣货/打包任务进行非法状态迁移时抛出。
 * </p>
 */
public class InvalidTaskStatusException extends BusinessException {

    public InvalidTaskStatusException(Long taskId, String taskType, String currentStatus, String targetStatus) {
        super(taskType + "状态非法迁移: taskId=" + taskId
                + ", currentStatus=" + currentStatus
                + ", targetStatus=" + targetStatus);
    }
}