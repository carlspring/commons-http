package org.carlspring.commons.io;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author carlspring
 */
public class ByteRangeInputStream extends AbstractByteRangeInputStream
{

    protected long length;


    public ByteRangeInputStream(ReloadableInputStreamHandler handler, ByteRange byteRange)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler, byteRange);
    }

    public ByteRangeInputStream(ReloadableInputStreamHandler handler, List<ByteRange> byteRanges)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler, byteRanges);
    }

    public ByteRangeInputStream(InputStream is) throws NoSuchAlgorithmException
    {
        super(is);
    }

    @Override
    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    @Override
    public void reposition(long position) throws IOException
    {
    }

}
