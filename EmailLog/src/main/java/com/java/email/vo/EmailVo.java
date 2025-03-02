package com.java.email.vo;

import co.elastic.clients.elasticsearch.ilm.PutLifecycleRequest;
import com.java.email.pojo.UndeliveredEmail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author EvoltoStar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5022L;

    private List<UndeliveredEmail> emailList;
    private long total;
}
