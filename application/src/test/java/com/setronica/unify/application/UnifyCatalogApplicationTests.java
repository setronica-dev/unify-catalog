package com.setronica.unify.application;

import com.setronica.unify.application.dto.JobStatusDto;
import com.setronica.unify.application.dto.ValidationResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnifyCatalogApplicationTests {
    private static final String TEST_SCHEMA = "{" +
            "    \"$id\": \"https://example.com/product.schema.json\"," +
            "    \"$schema\": \"https://json-schema.org/draft/2020-12/schema\"," +
            "    \"title\": \"ProductContent\"," +
            "    \"type\": \"object\"," +
            "    \"properties\": {" +
            "        \"title\": {" +
            "            \"type\": \"string\"," +
            "            \"description\": \"Product title\"" +
            "        }," +
            "        \"price\": {" +
            "            \"description\": \"Product price\"," +
            "            \"type\": \"number\"," +
            "            \"minimum\": 0" +
            "        }" +
            "    }," +
            "    \"required\": [\"title\", \"price\"]" +
            "}";
    private static final String TEST_CATEGORY = "general";

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
    }

    @Test
    void impex_critical_path() throws Exception {
        RestTemplate rest = new RestTemplate();
        String server = "http://localhost:" + port + "/catalog/";

        //set the schema for a product category
        ValidationResultDto dto = rest.postForObject(server + "schema/{id}", TEST_SCHEMA, ValidationResultDto.class, TEST_CATEGORY);
        assertTrue(dto.isSuccess());

        //upload import file
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ClassPathResource("test_data.csv"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        String token = rest.postForObject(server + "files/upload", requestEntity, String.class);
        assertNotNull(token);
        assertSame(JobStatusDto.COMPLETE, awaitJob(rest, server, token));

        //initiate export
        token = rest.postForObject(server + "files/download", null, String.class);
        assertNotNull(token);
        assertSame(JobStatusDto.COMPLETE, awaitJob(rest, server, token));

        //download the result
        String csv = rest.getForObject(server + "files/result/{t}", String.class, token);
        String expected = new ClassPathResource("expected_data.csv").getContentAsString(StandardCharsets.UTF_8);
        assertEquals(expected, csv);
    }

    private static JobStatusDto awaitJob(RestTemplate rest, String server, String token) throws InterruptedException {
        JobStatusDto status;
        long timer = System.currentTimeMillis();
        do {
            Thread.sleep(10L);
            status = rest.getForObject(server + "files/status/{t}", JobStatusDto.class, token);
        } while (status == JobStatusDto.RUNNING && System.currentTimeMillis() - timer < 10000);
        return status;
    }
}
