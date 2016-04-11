package org.carlspring.commons.http.rest;

import org.carlspring.commons.http.range.ByteRangeRequestHandler;
import org.carlspring.commons.io.ByteRangeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.carlspring.commons.http.range.ByteRangeRequestHandler.handlePartialDownload;
import static org.carlspring.commons.http.range.ByteRangeRequestHandler.isRangedRequest;

/**
 * @author Martin Todorov
 */
@Path("/storages")
public class ArtifactRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ByteRangeRequestHandler.class);

    private static final String STORAGES_BASEDIR = "target/storages";

    @GET
    @Path("{storageId}/{repositoryId}/{path:.*}")
    public Response download(@PathParam("storageId") String storageId,
                             @PathParam("repositoryId") String repositoryId,
                             @PathParam("path") String path,
                             @Context HttpServletRequest request,
                             @Context HttpHeaders headers)
            throws IOException,
                   InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException,
                   NoSuchAlgorithmException
    {
        logger.debug(" repository = " + repositoryId + ", path = " + path);

        Response.ResponseBuilder responseBuilder;

        File file = new File(STORAGES_BASEDIR, path);
        if (isRangedRequest(headers))
        {
            ByteRangeInputStream bris = new ByteRangeInputStream(new FileInputStream(file));
            bris.setLength(file.length());
            
            responseBuilder = handlePartialDownload(bris, headers);
        }
        else
        {
            InputStream is = new FileInputStream(file);
            responseBuilder = Response.ok(is);
        }

        // TODO: Restore this properly
        setMediaTypeHeader(path, responseBuilder);

        responseBuilder.header("Accept-Ranges", "bytes");

        return responseBuilder.build();
    }

    private void setMediaTypeHeader(String path, Response.ResponseBuilder responseBuilder)
    {
        // TODO: This is far from optimal and will need to have a content type approach at some point:
        if (path.endsWith(".md5") || path.endsWith(".sha1"))
        {
            responseBuilder.type(MediaType.TEXT_PLAIN);
        }
        else if (path.endsWith("maven-metadata.xml"))
        {
            responseBuilder.type(MediaType.APPLICATION_XML);
        }
        else
        {
            responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);
        }
    }

}

