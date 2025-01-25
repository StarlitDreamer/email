package org.easyarch.email;


import org.easyarch.email.result.ResultCodeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
