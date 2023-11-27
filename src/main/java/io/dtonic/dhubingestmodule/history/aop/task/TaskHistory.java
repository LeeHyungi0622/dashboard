package io.dtonic.dhubingestmodule.history.aop.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dtonic.dhubingestmodule.common.code.MonitoringCode;
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskHistory {
    MonitoringCode taskName();
}
