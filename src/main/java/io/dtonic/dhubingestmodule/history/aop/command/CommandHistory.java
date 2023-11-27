package io.dtonic.dhubingestmodule.history.aop.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dtonic.dhubingestmodule.common.code.CommandStatusCode;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHistory {
    CommandStatusCode command();
}
