package io.servicecomb.swagger.extend.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParamColFmt {
  @AliasFor("name")
  String value() default "";

  @AliasFor("value")
  String name() default "";

  boolean required() default false;

  String collectionFormat() default "";
}
