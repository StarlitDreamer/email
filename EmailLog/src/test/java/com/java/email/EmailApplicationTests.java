package com.java.email;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.elasticsearch.uris=http://localhost:9200"
})
class EmailApplicationTests {


    @Test
    public void testSaveAndFind() {

    }

}
