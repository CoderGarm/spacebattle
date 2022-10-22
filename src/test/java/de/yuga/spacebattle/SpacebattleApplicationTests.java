package de.yuga.spacebattle;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

@SpringBootTestProfile
@ActiveProfiles("dev")
@Disabled("only for generation of swagger spec needed")
public class SpacebattleApplicationTests {

    @Autowired
    WebApplicationContext context;

    private final static String tmpdir = System.getProperty("java.io.tmpdir");
    private final static String separator = System.getProperty("file.separator");

    /**
     * Run this test to generate the open api definition of the rest api.
     */
    @Test
    public void generateOpenApi3() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result) -> {
                    FileUtils.writeStringToFile(new File(tmpdir + separator + "open-api3.json"), result.getResponse().getContentAsString());
                });

    }
}
