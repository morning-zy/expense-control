package com.maning.expensecontrol.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 审批/驳回的请求体。
 */
@Getter
@Setter
public class ApprovalDecisionDTO {

    @Size(max = 500, message = "审批意见不能超过 500 字")
    private String comment;
}
