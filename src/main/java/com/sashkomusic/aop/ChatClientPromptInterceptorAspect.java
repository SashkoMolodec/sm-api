package com.sashkomusic.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intercepts ChatClient.prompt(..) and, when enabled by @UseWebSearchTool (via WebSearchToolAspect),
 * injects a top-level "tools" list into the underlying request spec using reflection. This avoids
 * polluting the system message and ensures Anthropic receives the tools at the root of the request.
 */
@Aspect
@Component
public class ChatClientPromptInterceptorAspect {

    @Around("execution(* org.springframework.ai.chat.client.ChatClient.prompt(..))")
    public Object aroundPrompt(ProceedingJoinPoint pjp) throws Throwable {
        Object ret = pjp.proceed();
        if (!(ret instanceof ChatClient.ChatClientRequestSpec spec)) {
            return ret;
        }
        if (!WebSearchToolAspect.isEnabled()) {
            return spec;
        }
        // Tools payload as List<Map<String,Object>>
        List<Map<String, Object>> tools = WebSearchToolAspect.getToolsAsList();
        // Try to call a permissive API on the spec to set arbitrary request option: "option(String,Object)".
        try {
            Method option = spec.getClass().getMethod("option", String.class, Object.class);
            option.invoke(spec, "tools", tools);
            return spec;
        } catch (NoSuchMethodException e) {
            // try an `options(Map)` method if available
            try {
                Method options = spec.getClass().getMethod("options", Map.class);
                Map<String, Object> m = new HashMap<>();
                m.put("tools", tools);
                options.invoke(spec, m);
                return spec;
            } catch (NoSuchMethodException e2) {
                // Fallback: do nothing; better than corrupting the system message.
                return spec;
            }
        }
    }
}
