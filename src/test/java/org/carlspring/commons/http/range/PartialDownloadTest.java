package org.carlspring.commons.http.range;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.http.TestClient;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.commons.util.MessageDigestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
public class PartialDownloadTest
{

    private TestClient client = new TestClient();


    @Before
    public void setUp() throws Exception
    {
        File dir = new File("target/storages/org/carlspring/strongbox/partial/partial-foo/3.1");
        if (!dir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        File file = new File(dir, "partial-foo-3.1.jar");

        String contents = "This is a test file containing some meaningless long text for the sake of testing.";
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(new ByteArrayOutputStream());
        mdos.write(contents.getBytes());
        mdos.flush();
        mdos.close();

        String md5 = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm()) + "\n";
        String sha1 = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm()) + "\n";

        Files.write(file.toPath(), contents.getBytes());
        Files.write(Paths.get(file.getAbsolutePath() + ".md5"), md5.getBytes());
        Files.write(Paths.get(file.getAbsolutePath() + ".sha1"), sha1.getBytes());
    }

    @Test
    public void testPartialFetch()
            throws Exception
    {
        String artifactPath = "/storages/storage0/releases/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        assertTrue("Artifact does not exist!", client.pathExists(artifactPath));

        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".md5"));
        String sha1Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".sha1"));

        InputStream is = client.getResource(artifactPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);

        int size = 8;
        byte[] bytes = new byte[size];
        int total = 0;
        int len = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            total += len;
            if (total == size)
            {
                break;
            }
        }

        mdos.flush();

        bytes = new byte[size];
        is.close();

        System.out.println("Read " + total + " bytes.");

        is = client.getResource(artifactPath, total);

        System.out.println("Skipped " + total + " bytes.");

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        System.out.println("Wrote " + total + " bytes.");
        System.out.println("Partial read, terminated after writing " + partialRead + " bytes.");
        System.out.println("Partial read, continued and wrote " + len2 + " bytes.");
        System.out.println("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        mdos.close();

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        System.out.println("MD5   [Remote]: " + md5Remote);
        System.out.println("MD5   [Local ]: " + md5Local);

        System.out.println("SHA-1 [Remote]: " + sha1Remote);
        System.out.println("SHA-1 [Local ]: " + sha1Local);

        System.out.println("Contents:");
        System.out.println(baos.toString());

        FileOutputStream output = new FileOutputStream(new File("target/partial-foo-3.1.jar"));
        output.write(baos.toByteArray());

        assertEquals("Glued partial fetches did not match MD5 checksum!", md5Remote, md5Local);
        assertEquals("Glued partial fetches did not match SHA-1 checksum!", sha1Remote, sha1Local);
    }

}
