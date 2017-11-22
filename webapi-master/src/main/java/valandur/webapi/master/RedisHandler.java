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
        String serverId = request.getHeader("X-WEBAPI-SERVER");
        redis.getQueue("requests_" + serverId).add(target);
    }
}
