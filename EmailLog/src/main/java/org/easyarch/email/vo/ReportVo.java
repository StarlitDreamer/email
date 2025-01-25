package org.easyarch.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class ReportVo implements Serializable {

    private static final long serialVersionUID = 5042L;
    @Data
    public static class Delivery implements Serializable{
        private static final long serialVersionUID = 5043L;
        private double rate;
        private double deliveryAmount;
        private long total;
    }
    @Data
    public static class Open implements Serializable{
        private static final long serialVersionUID = 5044L;
        private double rate;
        private double openAmount;
        private long total;
    }
    @Data
    public static class Bounce implements Serializable{
        private static final long serialVersionUID = 5045L;
        private double rate;
        private double bounceAmount;
        private long total;
    }
    @Data
    public static class Unsubscribe implements Serializable{
        private static final long serialVersionUID = 5046L;
        private double rate;
        private double unsubscribeAmount;
        private long total;
    }
    private Delivery delivery;
    private Open open;
    private Bounce bounce;
    private Unsubscribe unsubscribe;
}
