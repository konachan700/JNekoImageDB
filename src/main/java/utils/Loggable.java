package utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Loggable {
    Logger logger = LoggerFactory.getLogger(Loggable.class);

    default void E(String text) {
        logger.error(text);
    }

    default void W(String text) {
        logger.warn(text);
    }

    default void L(String text) {
        logger.info(text);
    }
}
