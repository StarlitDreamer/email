package com.java.email.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.java.email.constant.MagicMathConstData;

import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final LogUtil logUtil = LogUtil.getLogger(JwtUtil.class);
    private static final String KEY = "RV";
	
	//接收业务数据,生成token并返回
    public static String genToken(Map<String, Object> claims) {
        try {
            return JWT.create()
                    .withClaim("claims", claims)
                    .withExpiresAt(new Date(System.currentTimeMillis() + MagicMathConstData.TOKEN_TIME))
                    .sign(Algorithm.HMAC256(KEY));
        }catch (Exception e){
            logUtil.error("jwt create token error: " + e);
            return null;
        }

    }

	//接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(KEY))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();
        }catch (Exception e){
            logUtil.error("jwt parse token error: " + e);
            return null;
        }

    }

}
