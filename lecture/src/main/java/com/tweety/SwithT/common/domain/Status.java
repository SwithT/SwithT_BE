package com.tweety.SwithT.common.domain;

import lombok.Getter;

@Getter
public enum Status {
    STANDBY,
    REJECT,
    ADMIT,
    TERMINATE,
    CANCEL
}
