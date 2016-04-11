package org.carlspring.commons.http.range;

import org.carlspring.commons.io.ByteRangeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author Martin Todorov
 */
public class ByteRangeRequestHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ByteRangeRequestHandler.class);


    public static Response.ResponseBuilder handlePartialDownload(ByteRangeInputStream bris,
                                                                 HttpHeaders headers)
            throws IOException
    {
        ByteRangeHeaderParser parser = new ByteRangeHeaderParser(headers.getRequestHeaders().getFirst("Range"));
        List<ByteRange> ranges = parser.getRanges();
        if (ranges.size() == 1)
        {
            logger.debug("Received request for a partial download with a single range.");

            return handlePartialDownloadWithSingleRange(bris, ranges.get(0));
        }
        else
        {
            logger.debug("Received request for a partial download with multiple ranges.");

            return handlePartialDownloadWithMultipleRanges(bris, ranges);
        }
    }

    public static Response.ResponseBuilder handlePartialDownloadWithSingleRange(ByteRangeInputStream bris,
                                                                                ByteRange byteRange)
            throws IOException
    {
        if (byteRange.getOffset() < bris.getLength())
        {
            // If OK: Return: 206 Partial Content
            //     Set headers:
            //         Accept-Ranges: bytes
            //         Content-Length: 64656927
            //         Content-Range: bytes 100-64656926/64656927
            //         Content-Type: application/jar
            //         Pragma: no-cache

            bris.setCurrentByteRange(byteRange);
            //noinspection ResultOfMethodCallIgnored
            bris.skip(byteRange.getOffset());

            Response.ResponseBuilder responseBuilder = prepareResponseBuilderForPartialRequest(bris);
            responseBuilder.header("Content-Length", calculatePartialRangeLength(byteRange, bris.getLength()));
            responseBuilder.status(Response.Status.PARTIAL_CONTENT);

            return responseBuilder;
        }
        else
        {
            // Else: If the byte-range-set is unsatisfiable, the server SHOULD return
            //       a response with a status of 416 (Requested range not satisfiable).

            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    public static Response.ResponseBuilder handlePartialDownloadWithMultipleRanges(ByteRangeInputStream bris,
                                                                                   List<ByteRange> byteRanges)
            throws IOException
    {
        // TODO: To be handled as part of SB-367.

        // TODO: This is not the right check
        if (bris.getCurrentByteRange().getOffset() >= bris.getLength())
        {
            // TODO: If OK: Return: 206 Partial Content
            // TODO:     For each range:
            // TODO:         Set headers:
            // TODO:             Content-Type: application/jar
            // TODO:             Content-Length: 64656927
            // TODO:             Accept-Ranges: bytes
            // TODO:             Content-Range: bytes 100-64656926/64656927
            // TODO:             Pragma: no-cache

            Response.ResponseBuilder responseBuilder = prepareResponseBuilderForPartialRequest(bris);

            // TODO: Add multipart content here

            return responseBuilder;
        }
        else
        {
            // TODO: Else: If the byte-range-set is unsatisfiable, the server SHOULD return
            // TODO:       a response with a status of 416 (Requested range not satisfiable).

            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    public static long calculatePartialRangeLength(ByteRange byteRange, long length)
    {
        if (byteRange.getLimit() > 0 && byteRange.getOffset() > 0)
        {
            logger.debug("Partial content byteRange.getOffset: " + byteRange.getOffset());
            logger.debug("Partial content byteRange.getLimit: " + byteRange.getLimit());
            logger.debug("Partial content length: " + (byteRange.getLimit() - byteRange.getOffset()));

            return byteRange.getLimit() - byteRange.getOffset();
        }
        else if (length > 0 && byteRange.getOffset() > 0 && byteRange.getLimit() == 0)
        {
            logger.debug("Partial content length: " + (length - byteRange.getOffset()));

            return length - byteRange.getOffset();
        }
        else
        {
            return -1;
        }
    }

    public static Response.ResponseBuilder prepareResponseBuilderForPartialRequest(ByteRangeInputStream bris)
    {
        Response.ResponseBuilder responseBuilder = Response.ok(bris).status(Response.Status.PARTIAL_CONTENT);
        responseBuilder.header("Accept-Ranges", "bytes");
        // TODO:
        // responseBuilder.header("Content-Length", ...);
        responseBuilder.header("Content-Range", "bytes " + bris.getCurrentByteRange().getOffset() + "-" +
                                                (bris.getLength() - 1) + "/" + bris.getLength());
        responseBuilder.header("Pragma", "no-cache");

        return responseBuilder;
    }

    public static boolean isRangedRequest(HttpHeaders headers)
    {
        if (headers == null)
        {
            return false;
        }

        String contentRange = headers.getRequestHeaders() != null &&
                              headers.getRequestHeaders().getFirst("Range") != null ?
                              headers.getRequestHeaders().getFirst("Range") : null;

        return contentRange != null &&
               !contentRange.equals("0/*") && !contentRange.equals("0-") && !contentRange.equals("0");
    }

}

