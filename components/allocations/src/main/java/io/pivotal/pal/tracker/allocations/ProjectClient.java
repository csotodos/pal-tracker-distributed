package io.pivotal.pal.tracker.allocations;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;
    private Map<Long, ProjectInfo> cacheProjects;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
        this.cacheProjects = new ConcurrentHashMap<Long, ProjectInfo>();
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
        logger.info("Getting project with id {} from REST call", projectId);

        this.cacheProjects.put(projectId, projectInfo);

        return projectInfo;
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause){
        logger.info("Getting project with id {} from cache", projectId);
        return this.cacheProjects.get(projectId);
    }
}
