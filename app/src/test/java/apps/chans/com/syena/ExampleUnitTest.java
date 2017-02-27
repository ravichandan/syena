package apps.chans.com.syena;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import apps.chans.com.syena.web.response.GetWatchesResponse;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        //req.setRequester("chan@g.com");
//		req.setInstallationId("sfd");
String content="{\n" +
        "            \"email\": null,\n" +
        "                \"watchMembers\": [\n" +
        "            {\n" +
        "                \"email\": \"s.ravichandan@gmail.com\",\n" +
        "                    \"name\": null,\n" +
        "                    \"enabled\": false\n" +
        "            }\n" +
        "            ],\n" +
        "        }";

        ObjectMapper mapper = new ObjectMapper();
        GetWatchesResponse req =  mapper.readValue(content,GetWatchesResponse.class);
        System.out.println(req.getEmail());

    }
}