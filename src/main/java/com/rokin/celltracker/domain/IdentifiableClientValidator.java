package com.rokin.celltracker.domain;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IdentifiableClientValidator
    implements ConstraintValidator<IdentifiableClient, Client> {
  
  @Override
  public void initialize(IdentifiableClient constraintAnnotation) {
    // hhhh
  }

  @Override
  public boolean isValid(Client client, ConstraintValidatorContext cvc) {
    return (client.getEmail() != null && !client.getEmail().isEmpty())
        || (client.getCellNo() != null && !client.getCellNo().isEmpty());
  }
}