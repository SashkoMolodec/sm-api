package com.sashkomusic.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class WebSearchToolAspect {

    private static final ThreadLocal<Context> CTX = ThreadLocal.withInitial(Context::new);

    public static boolean isEnabled() {
        return CTX.get().enabled;
    }

    public static String getToolsJson() {
        Context c = CTX.get();
        int max = c.maxUses > 0 ? c.maxUses : 5;
        return "\"tools\": [{\n" +
                "            \"type\": \"web_search_20250305\",\n" +
                "            \"name\": \"web_search\",\n" +
                "            \"max_uses\": " + max + "\n" +
                "        }]";
    }

    public static List<Map<String, Object>> getToolsAsList() {
        int max = CTX.get().maxUses > 0 ? CTX.get().maxUses : 5;
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "web_search_20250305");
        tool.put("name", "web_search");
        tool.put("max_uses", max);
        return List.of(tool);
    }

    @Around("@annotation(useWebSearchTool)")
    public Object aroundAnnotated(ProceedingJoinPoint pjp, UseWebSearchTool useWebSearchTool) throws Throwable {
        Context prev = CTX.get();
        boolean prevEnabled = prev.enabled;
        int prevMax = prev.maxUses;
        CTX.set(new Context(true, useWebSearchTool.maxUses()));
        try {
            return pjp.proceed();
        } finally {
            CTX.set(new Context(prevEnabled, prevMax));
        }
    }

    private record Context(boolean enabled, int maxUses) {
        public Context() { this(false, 5); }
    }
}
