package com.rokin.celltracker.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IdentifiableClientValidator.class })
public @interface IdentifiableClient {

  /**
   * get message.
   * @return
   */
  String message() default "client identify";

  /**
   * 
   * @return
   */
  Class<?>[] groups() default {};

  /**
   * 
   * @return
   */
  Class<? extends Payload>[] payload() default {};

}