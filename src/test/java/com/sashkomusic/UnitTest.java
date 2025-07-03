package com.sashkomusic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

@ExtendWith(SpringExtension.class)
public class UnitTest {

    @Test
    void test() {
        Set<String> items = Set.of("tag1", "tag2", "tag3");
        String s = items.toString();
        System.out.println(s);
    }
}
