package com.github.gaborfeher.grantmaster.core;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.bval.jsr303.ApacheValidationProvider;

public enum MyValidatorFactory {

    SINGLE_INSTANCE {

        ValidatorFactory avf =
            Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory();

        @Override
        public Validator getValidator() {
            return avf.getValidator();
        }

    };

    public abstract Validator getValidator(); 
}
