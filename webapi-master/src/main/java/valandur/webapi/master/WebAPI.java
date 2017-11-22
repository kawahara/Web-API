package valandur.webapi.master;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.UUID;

public class WebAPI {

    public static final String VERSION = "@version@";

    private static Logger logger = LoggerFactory.getLogger(WebAPI.class);

    private static String serverId;
    private static String serverHost = "127.0.0.1";
    private static Integer serverPortHttp = 8080;
    private static Integer serverPortHttps = 8081;
    private static String keyStoreLocation = "/keystore.jks";

    private static Server server;

    public static void main(String[] args) {
        logger.info("------------------------");
        logger.info("  Web-API v" + VERSION);
        logger.info("------------------------\n");

        serverId = UUID.randomUUID().toString();

        startWebServer();
    }

    private static void startWebServer() {
        // Start web server
        logger.info("Starting Web Server...");

        try {
            server = new Server();

            // HTTP config
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setOutputBufferSize(32768);

            String baseUri = null;

            // HTTP
            if (serverPortHttp >= 0) {
                if (serverPortHttp < 1024) {
                    logger.warn("You are using an HTTP port < 1024 which is not recommended! \n" +
                            "This might cause errors when not running the server as root/admin. \n" +
                            "Running the server as root/admin is not recommended. " +
                            "Please use a port above 1024 for HTTP."
                    );
                }
                ServerConnector httpConn = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
                httpConn.setHost(serverHost);
                httpConn.setPort(serverPortHttp);
                httpConn.setIdleTimeout(30000);
                server.addConnector(httpConn);

                baseUri = "http://" + serverHost + ":" + serverPortHttp;
            }

            // HTTPS
            if (serverPortHttps >= 0) {
                if (serverPortHttps < 1024) {
                    logger.warn("You are using an HTTPS port < 1024 which is not recommended! \n" +
                            "This might cause errors when not running the server as root/admin. \n" +
                            "Running the server as root/admin is not recommended. " +
                            "Please use a port above 1024 for HTTPS."
                    );
                }

                // Update http config
                httpConfig.setSecureScheme("https");
                httpConfig.setSecurePort(serverPortHttps);

                // SSL Factory
                SslContextFactory sslFactory = new SslContextFactory();
                sslFactory.setKeyStorePath(WebAPI.class.getResource(keyStoreLocation).getPath());
                sslFactory.setKeyStorePassword("mX4z%&uJ2E6VN#5f");
                sslFactory.setKeyManagerPassword("mX4z%&uJ2E6VN#5f");

                // HTTPS config
                HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
                SecureRequestCustomizer src = new SecureRequestCustomizer();
                src.setStsMaxAge(2000);
                src.setStsIncludeSubDomains(true);
                httpsConfig.addCustomizer(src);


                ServerConnector httpsConn = new ServerConnector(server,
                        new SslConnectionFactory(sslFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfig)
                );
                httpsConn.setHost(serverHost);
                httpsConn.setPort(serverPortHttps);
                httpsConn.setIdleTimeout(30000);
                server.addConnector(httpsConn);

                baseUri = "https://" + serverHost + ":" + serverPortHttps;
            }

            if (baseUri == null) {
                logger.warn("You have disabled both HTTP and HTTPS - The WebAPI will be unreachable!");
            }

            server.setHandler(new RedisHandler());

            logger.info("Setting up jetty logger...");
            Log.setLog(new JettyLogger());

            server.start();

            logger.info("Running: " + baseUri);
        } catch (SocketException e) {
            logger.error("Web-API webserver could not start, probably because one of the ports needed for HTTP " +
                    "and/or HTTPS are in use or not accessible (ports below 1024 are protected)");
        } catch (MultiException e) {
            e.getThrowables().forEach(t -> {
                if (t instanceof SocketException) {
                    logger.error("Web-API webserver could not start, probably because one of the ports needed for HTTP " +
                            "and/or HTTPS are in use or not accessible (ports below 1024 are protected)");
                } else {
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stopWebServer() {
        if (server != null) {
            try {
                server.stop();
                server = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
