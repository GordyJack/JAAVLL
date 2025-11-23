package net.gordyjack.jaavll.util;

import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.*;

public final class IDUtil {
    private IDUtil() {}

    public static ResourceLocation idFromItem(ItemLike itemConvertible) {
        return BuiltInRegistries.ITEM.getKey(itemConvertible.asItem());
    }
    public static String nameFromId(ItemLike itemConvertible) {
        return nameFromId(idFromItem(itemConvertible));
    }
    public static String nameFromId(ResourceLocation id) {
        return id.getPath();
    }
}
