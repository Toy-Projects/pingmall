package com.kiseok.pingmall.common.domain.order;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import javax.validation.Validation;
import java.util.Collection;

@Component
public class OrdersValidator implements Validator {

    private final SpringValidatorAdapter validatorAdapter;

    public OrdersValidator() {
        this.validatorAdapter = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(target instanceof Collection)    {
            Collection collection = (Collection) target;
            for(Object object : collection) {
                validatorAdapter.validate(object, errors);
            }
        }
        else    {
            validatorAdapter.validate(target, errors);
        }
    }
}
