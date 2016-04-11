package org.carlspring.commons.http;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author mtodorov
 */
public class TestClient implements Closeable
{

    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

    private String protocol = "http";

    private String host = System.getProperty("http.commons.host") != null ?
                          System.getProperty("http.commons.host") : "localhost";

    private int port = System.getProperty("http.commons.port") != null ?
                       Integer.parseInt(System.getProperty("http.commons.port")) :
                       48080;

    private String contextBaseUrl;

    private String username = "maven";

    private String password = "password";

    private Client client;


    public TestClient()
    {
    }

    public static TestClient getTestInstance()
    {
        String host = System.getProperty("http.commons.host") != null ?
                      System.getProperty("http.commons.host") :
                      "localhost";

        int port = System.getProperty("http.commons.port") != null ?
                   Integer.parseInt(System.getProperty("http.commons.port")) :
                   48080;

        TestClient testClient = new TestClient();
        testClient.setUsername("maven");
        testClient.setPassword("password");
        testClient.setPort(port);
        testClient.setContextBaseUrl("http://" + host + ":" + testClient.getPort());

        return testClient;
    }

    public javax.ws.rs.client.Client getClientInstance()
    {
        if (client == null)
        {
            ClientConfig config = getClientConfig();
            client = ClientBuilder.newClient(config);

            return client;
        }
        else
        {
            return client;
        }
    }

    private ClientConfig getClientConfig()
    {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());

        return config;
    }

    @Override
    public void close()
    {
        if (client != null)
        {
            client.close();
        }
    }

    public void put(InputStream is, String url, String fileName, String mediaType)
    {
        String contentDisposition = "attachment; filename=\"" + fileName +"\"";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(mediaType)
                                    .header("Content-Disposition", contentDisposition)
                                    .put(Entity.entity(is, mediaType));

        handleFailures(response, "Failed to upload file!");
    }


    public InputStream getResource(String path)
            throws IOException
    {
        return getResource(path, 0);
    }

    public InputStream getResource(String path, long offset)
            throws IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Invocation.Builder request = resource.request();
        Response response;

        if (offset > 0)
        {
            logger.debug("Issuing a partial request... (Range: bytes=" + offset + "-)");

            response = request.header("Range", "bytes=" + offset + "-").get();
        }
        else
        {
            response = request.get();
        }

        return response.readEntity(InputStream.class);
    }

    public Response getResourceWithResponse(String path)
            throws IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).get();
    }

    public boolean pathExists(String path)
    {
        String url = getContextBaseUrl() + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path: " + url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).get();

        return response.getStatus() == 200;
    }

    private void handleFailures(Response response, String message)
    {
        int status = response.getStatus();
        if (status != 200)
        {
            Object entity = response.getEntity();

            if (entity != null && entity instanceof String)
            {
                logger.error((String) entity);
            }
        }
    }

    public void setupAuthentication(WebTarget target)
    {
        if (username != null && password != null)
        {
            target.register(HttpAuthenticationFeature.basic(username, password));
        }
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getContextBaseUrl()
    {
        if (contextBaseUrl == null)
        {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
