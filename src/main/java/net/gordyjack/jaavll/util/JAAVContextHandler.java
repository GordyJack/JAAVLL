package net.gordyjack.jaavll.util;

import net.minecraft.resources.*;
import org.slf4j.*;

public class JAAVContextHandler {
    public final String MOD_ID;
    public final Logger LOGGER;

    public JAAVContextHandler(String modID) {
        this.MOD_ID = modID;
        this.LOGGER = LoggerFactory.getLogger(modID);
    }
    public void log(String message, char level) {
        switch (level) {
            case 'd' -> LOGGER.debug(message);
            case 'w' -> LOGGER.warn(message);
            case 'e' -> LOGGER.error(message);
            default -> LOGGER.info(message);
        }
    }
    public ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(this.MOD_ID, path);
    }
}
