package com.sashkomusic.aop;

import java.lang.annotation.*;

/**
 * Mark methods that should append Anthropic web_search tool JSON to the request.
 * The Aspect will ensure the JSON snippet is attached right before making the model call.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseWebSearchTool {
    /**
     * Maximum number of web_search tool invocations the model may perform.
     */
    int maxUses() default 5;
}
