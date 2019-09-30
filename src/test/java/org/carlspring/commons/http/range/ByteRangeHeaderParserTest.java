package org.carlspring.commons.http.range;

import org.carlspring.commons.http.range.validation.ByteRangeValidationException;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.carlspring.commons.http.range.ByteRangeHeaderParser.BYTE_RANGE_NOT_VALID_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public class ByteRangeHeaderParserTest
{

    @Nested
    @DisplayName("Tests for single byte ranges")
    class SingleByteRangeHeaderParserTest
    {

        @Test
        void testParsingWithNegativeOffsetShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=-5-50";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertEquals(BYTE_RANGE_NOT_VALID_MESSAGE, exception.getMessage());
        }

        @Test
        void testParsingWithNegativeLengthShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=5-50/-1000";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertThat(exception.getMessage(), containsString("Range length must be greater than or equal to zero"));
        }

        @Test
        void testParsingWithOffsetGreaterThaLimitShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=50-0";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertThat(exception.getMessage(), containsString("Range limit must be greater than or equal to offset"));
        }

        @Test
        void testParsingWithOffsetOnly()
        {
            // Given
            String headerContents = "bytes=500-";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            assertFalse(ranges.isEmpty());
            assertEquals(1, ranges.size(), "Parsed incorrect number of ranges!");

            ByteRange range = ranges.get(0);

            assertEquals(500, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertNull(range.getLimit(), "Parsed an incorrect end value!");

            assertEquals("bytes=500-", range.toString());
        }

        @Test
        void testParsingWithEndOnly()
        {
            // Given
            String headerContents = "bytes=-500";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            assertFalse(ranges.isEmpty());
            assertEquals(1, ranges.size(), "Parsed incorrect number of ranges!");

            long totalLength = 1001L;
            ByteRange range = ranges.get(0);
            range.setTotalLength(totalLength);

            assertEquals(0, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(-500, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=500-1000/1001", range.toString());
        }

        @Test
        void testParsingWithOffsetAndEnd()
        {
            // Given
            String headerContents = "bytes=500-1000";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            assertFalse(ranges.isEmpty());
            assertEquals(1, ranges.size(), "Parsed incorrect number of ranges!");

            long totalLength = 1001L;
            ByteRange range = ranges.get(0);
            range.setTotalLength(totalLength);

            assertEquals(500, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(1000, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=500-1000/1001", range.toString());
        }

        @Test
        void testToStringWithWildcardLength1()
        {
            // Given
            String headerContents = "bytes=500-1000/*";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            ByteRange range = ranges.get(0);

            assertEquals(500, range.getOffset().longValue(), "Failed to parse offset!");
            assertEquals(1000, range.getLimit().longValue(), "Failed to parse end!");
            assertEquals(0, range.getTotalLength(), "Failed to parse length!");
        }

        @Test
        void testToStringWithWildcardLength2()
        {
            // Given
            String headerContents = "bytes=500/*";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            ByteRange range = ranges.get(0);

            assertEquals(500, range.getOffset().longValue(), "Failed to parse offset!");
            assertEquals(0, range.getTotalLength(), "Failed to parse length!");
        }

        @Test
        void testToStringWithWildcardLength3()
        {
            // Given
            String headerContents = "bytes=-500/*";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            ByteRange range = ranges.get(0);

            assertEquals(0, range.getOffset().longValue(), "Failed to parse offset!");
            assertEquals(-500, range.getLimit().longValue(), "Failed to parse limit!");
            assertEquals(0, range.getTotalLength(), "Failed to parse length!");
        }

        @Test
        void testToStringWithFixedLength()
        {
            // Given
            String headerContents = "bytes=100-500/1024";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            ByteRange range = ranges.get(0);

            assertEquals(100, range.getOffset().longValue(), "Failed to parse offset!");
            assertEquals(500, range.getLimit().longValue(), "Failed to parse limit!");
            assertEquals(1024, range.getTotalLength(), "Failed to parse length!");
        }
    }

    @Nested
    @DisplayName("Tests for multiple byte ranges")
    class MultipleByteRangeHeaderParserTest
    {

        @Test
        void testParsingWithNegativeOffsetShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=5-50,-60-80";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertEquals(BYTE_RANGE_NOT_VALID_MESSAGE, exception.getMessage());
        }

        @Test
        void testParsingWithNegativeLengthShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=5-50,60-80/-1000";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertThat(exception.getMessage(), containsString("Range length must be greater than or equal to zero"));
        }

        @Test
        void testParsingWithOffsetGreaterThaLimitShouldThrowByteRangeValidationException()
        {
            // Given
            String headerContents = "bytes=5-60,200-100";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            ByteRangeValidationException exception = assertThrows(ByteRangeValidationException.class,
                                                                  parser::getRanges);

            // Then
            assertThat(exception.getMessage(), containsString("Range limit must be greater than or equal to offset"));
        }

        @Test
        void testParsingWithOffsetAndEnd()
        {
            // Given
            String headerContents = "bytes=100-200,500-1000";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            assertFalse(ranges.isEmpty());
            assertEquals(2, ranges.size(), "Parsed incorrect number of ranges!");

            long totalLength = 1001L;
            ByteRange range = ranges.get(0);
            range.setTotalLength(totalLength);

            assertEquals(100, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(200, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=100-200/1001", range.toString());

            range = ranges.get(1);
            range.setTotalLength(totalLength);

            assertEquals(500, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(1000, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=500-1000/1001", range.toString());
        }

        @Test
        void testParsingWithFirstByteOffsetAndLastByteEnd()
        {
            // Given
            String headerContents = "bytes=0-0,-1";
            ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);

            // When
            List<ByteRange> ranges = parser.getRanges();

            // Then
            assertFalse(ranges.isEmpty());
            assertEquals(2, ranges.size(), "Parsed incorrect number of ranges!");

            ByteRange range = ranges.get(0);

            assertEquals(0, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(0, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=0-0", range.toString());

            range = ranges.get(1);

            assertEquals(0, range.getOffset().longValue(), "Parsed an incorrect offset value!");
            assertEquals(-1, range.getLimit().longValue(), "Parsed an incorrect end value!");

            assertEquals("bytes=-1", range.toString());
        }

    }
}
