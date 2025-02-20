package com.java.email.vo;

import co.elastic.clients.elasticsearch.ilm.PutLifecycleRequest;
import com.java.email.pojo.UndeliveredEmail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author EvoltoStar
 */
@Data
public class EmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5022L;

    private List<UndeliveredEmail> emailList;
    private long total;
}
