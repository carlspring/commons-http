package org.carlspring.commons.http.range.validation;

import org.carlspring.commons.http.range.ByteRange;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Pablo Tirado
 */
public class ByteRangeCheckValidator
        implements ConstraintValidator<ByteRangeCheck, ByteRange>
{

    @Override
    public void initialize(ByteRangeCheck byteRangeCheck)
    {
        // Nothing here
    }

    @Override
    public boolean isValid(ByteRange byteRange,
                           ConstraintValidatorContext constraintValidatorContext)
    {
        Long start = byteRange.getOffset();
        Long end = byteRange.getLimit();

        if (end == null)
        {
            return true;
        }

        if (end <= 0)
        {
            return start >= end;
        }

        return end >= start;

    }
}
