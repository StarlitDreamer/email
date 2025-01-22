package com.java;

import com.java.atuhcode.Auth;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class Test {
    @org.junit.jupiter.api.Test
    public void test(){
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println(Arrays.toString(Auth.values()));
    }

}
