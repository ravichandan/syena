package apps.chans.com.syena;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import apps.chans.com.syena.web.response.GetWatchesResponse;

import static apps.chans.com.syena.web.response.GetWatchesResponse.*;
import static java.lang.System.out;
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
String content="{\"email\":\"sdfd\",\"watchMembers\":[{\"email\":\"neelima.salesha25@gmail.com\",\"name\":\"null\",\"enabled\":true,\"watchActive\":true}]}";

        ObjectMapper mapper = new ObjectMapper();
        GetWatchesResponse req1 =  mapper.readValue(content,GetWatchesResponse.class);
        out.println(req1.getEmail());
        /*GetWatchesResponse req =new GetWatchesResponse();
        req.setEmail("ddfdf@fd");
        List<GetWatchesResponse.Entry> mems= new ArrayList<>();
        req.addEntry("s.ravi@gmail.com","Chan",true);

                out.println(mapper.writeValueAsString(req));*/

    }
}