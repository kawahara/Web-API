package valandur.webapi.master;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedisHandler extends AbstractHandler {

    private RedissonClient redis;
    private Logger logger = LoggerFactory.getLogger(WebAPI.class);

    public RedisHandler() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redis = Redisson.create(config);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        logger.info("Request: " + target);
        String path = target;

        String serverId = request.getHeader("X-WEBAPI-SERVER");
        if (serverId == null) {
            List<String> pathParts = getPathParts(target);
            if (pathParts.size() == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Server not found");
                return;
            }

            serverId = pathParts.get(0);
            path = String.join("/", pathParts.subList(1, pathParts.size()));
        }

        redis.getQueue("requests_" + serverId).add(path);
    }

    public static List<String> getPathParts(String path) {
        if (path == null) return new ArrayList<>();
        return Arrays.stream(path.replaceFirst("^/", "")
                .split("/")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
