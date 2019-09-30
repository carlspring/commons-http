package org.carlspring.commons.http.range.validation;


import org.carlspring.commons.http.range.ByteRange;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Pablo Tirado
 */
public class ByteRangeCheckValidatorTest
{

    private static Validator validator;

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @BeforeAll
    static void setUp()
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void byteRangeLimitToNullIsValid()
    {
        // Given
        // Example: 1000- ; Read all bytes after 1000
        ByteRange byteRange = new ByteRange(1000L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void byteRangeNegativeLimitIsValid()
    {
        // Given
        // Example: -2000 ; Read the last 2000 bytes.
        ByteRange byteRange = new ByteRange(0L, -2000L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void byteRangeLimitGreaterThanOffsetIsValid()
    {
        // Given
        // Example: 1000-2000 ; Read bytes 1000-2000 (incl.)
        ByteRange byteRange = new ByteRange(1000L, 2000L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void byteRangeOffsetGreaterThanLimitIsNotValid()
    {
        // Given
        ByteRange byteRange = new ByteRange(50L, 0L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        violations.forEach(v -> logger.debug("Violation: {}", v.getMessage()));

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void byteRangeOffsetLowerThanZeroIsNotValid()
    {
        // Given
        ByteRange byteRange = new ByteRange(-500L, 100L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        violations.forEach(v -> logger.debug("Violation: {}", v.getMessage()));

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void byteRangeTotalLengthLowerThanZeroIsNotValid()
    {
        // Given
        ByteRange byteRange = new ByteRange(500L, 1000L);
        byteRange.setTotalLength(-1L);

        // When
        Set<ConstraintViolation<ByteRange>> violations = validator.validate(byteRange);

        violations.forEach(v -> logger.debug("Violation: {}", v.getMessage()));

        // Then
        assertFalse(violations.isEmpty());
    }
}
