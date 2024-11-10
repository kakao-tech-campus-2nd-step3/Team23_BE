package kappzzang.jeongsan.global.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorTypeExample {

    ErrorType[] value();
}
