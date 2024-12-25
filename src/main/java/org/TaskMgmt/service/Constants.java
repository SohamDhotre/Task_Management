package org.TaskMgmt.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class Constants {

    public static final long TIMEOUT_DURATION = 15;
    public static final int MAX_FAILED_ATTEMPTS = 3;
    // Define public paths as a private static final list
    public final List<String> PUBLIC_PATHS = List.of(
            "/",
            "/metrics-summary",
            "/actuator/**",
            "/user/register",
            "/user/test",
            "/user/login",
            "/public/**"
    );

}
