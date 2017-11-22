package valandur.webapi.master;

import org.eclipse.jetty.util.log.Logger;
import org.slf4j.LoggerFactory;

public class JettyLogger implements Logger {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(WebAPI.class);
    private boolean enableDebug = false;

    @Override public String getName() { return "jetty"; }
    @Override public void warn(String msg, Object... args) {
        logger.warn(msg, args);
    }
    @Override public void warn(Throwable thrown) {
        logger.warn("THROWN: " + thrown);
    }
    @Override public void warn(String msg, Throwable thrown) {
        logger.warn(msg, thrown);
    }
    @Override public void info(String msg, Object... args) {
        logger.debug(msg, args);
    }
    @Override public void info(Throwable thrown) {
        logger.debug("THROWN: " + thrown);
    }
    @Override public void info(String msg, Throwable thrown) {
        logger.debug(msg, thrown);
    }
    @Override public boolean isDebugEnabled() { return enableDebug; }
    @Override public void setDebugEnabled(boolean enabled) {
        this.enableDebug = enabled;
    }
    @Override public void debug(String msg, Object... args) {
        logger.debug(msg, args);
    }
    @Override public void debug(String msg, long value) {
        logger.debug(msg, value);
    }
    @Override public void debug(Throwable thrown) {
        logger.debug("THROWN: " + thrown);
    }
    @Override public void debug(String msg, Throwable thrown) {
        logger.debug(msg, thrown);
    }
    @Override public Logger getLogger(String name) { return this; }
    @Override public void ignore(Throwable ignored) { }
}
