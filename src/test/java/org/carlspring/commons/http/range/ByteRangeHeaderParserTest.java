package org.carlspring.commons.http.range;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author mtodorov
 */
public class ByteRangeHeaderParserTest
{


    @Test
    public void testParsingWithOffsetOnly()
    {
        String headerContents = "bytes=500-";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        ByteRange range = ranges.get(0);

        assertEquals("Parsed an incorrect offset value!", 500, range.getOffset().longValue());
        assertNull("Parsed an incorrect end value!", range.getLimit());

        assertEquals("bytes=500-", range.toString());
    }

    @Test
    public void testParsingWithEndOnly()
    {
        String headerContents = "bytes=-500";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        long totalLength = 1001L;
        ByteRange range = ranges.get(0);
        range.setTotalLength(totalLength);

        assertEquals("Parsed an incorrect offset value!", 0, range.getOffset().longValue());
        assertEquals("Parsed an incorrect end value!", -500, range.getLimit().longValue());

        assertEquals("bytes=500-1000/1001", range.toString());
    }

    @Test
    public void testParsingWithOffsetAndEnd()
    {
        String headerContents = "bytes=500-1000";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        assertFalse(ranges.isEmpty());
        assertEquals("Parsed incorrect number of ranges!", 1, ranges.size());

        long totalLength = 1001L;
        ByteRange range = ranges.get(0);
        range.setTotalLength(totalLength);

        assertEquals("Parsed an incorrect offset value!", 500, range.getOffset().longValue());
        assertEquals("Parsed an incorrect end value!", 1000, range.getLimit().longValue());

        assertEquals("bytes=500-1000/1001", range.toString());
    }

    @Test
    public void testToStringWithWildcardLength1()
    {
        String headerContents = "bytes=500-1000/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 500, range.getOffset().longValue());
        assertEquals("Failed to parse end!", 1000, range.getLimit().longValue());
        assertEquals("Failed to parse length!", 0, range.getTotalLength().longValue());
    }

    @Test
    public void testToStringWithWildcardLength2()
    {
        String headerContents = "bytes=500/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 500, range.getOffset().longValue());
        assertEquals("Failed to parse length!", 0, range.getTotalLength().longValue());
    }

    @Test
    public void testToStringWithWildcardLength3()
    {
        String headerContents = "bytes=-500/*";

        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headerContents);
        List<ByteRange> ranges = parser.getRanges();

        ByteRange range = ranges.get(0);

        assertEquals("Failed to parse offset!", 0, range.getOffset().longValue());
        assertEquals("Failed to parse limit!", -500, range.getLimit().longValue());
        assertEquals("Failed to parse length!", 0, range.getTotalLength().longValue());
    }

}
