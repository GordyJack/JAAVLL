package net.gordyjack.jaavll.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public final class RegistryUtils {
    private RegistryUtils() {}

    // -----------------------------------------------------------
    //  FILTERED ENTRIES (main workhorse)
    // -----------------------------------------------------------
    public static <T> LinkedHashMap<T, ResourceLocation> entriesForMod(Registry<T> registry, String modId) {
        return registry.entrySet().stream()
                .map(entry -> {
                    T value = entry.getValue();
                    ResourceLocation id = registry.getKey(value);
                    return (id != null && id.getNamespace().equals(modId))
                            ? Map.entry(value, id) : null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(e -> e.getValue().toString()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // -----------------------------------------------------------
    //  ID LOOKUPS
    // -----------------------------------------------------------
    public static ResourceLocation getId(Block block) {
        return getId(BuiltInRegistries.BLOCK, block);
    }
    public static ResourceLocation getId(Item item) {
        return getId(BuiltInRegistries.ITEM, item);
    }
    public static <T> ResourceLocation getId(Registry<T> registry, T value) {
        return registry.getKey(value);
    }

    // -----------------------------------------------------------
    // Name Lookups
    // -----------------------------------------------------------
    public static String getName(Block block) {
        return getName(getId(block));
    }
    public static String getName(Item item) {
        return getName(getId(item));
    }
    public static String getName(ResourceLocation id) {
        return id.getPath();
    }


    // -----------------------------------------------------------
    //  BLOCK ↔ ITEM MAP
    // -----------------------------------------------------------
    public static LinkedHashMap<Block, Item> blockItemsForMod(String modId) {
        LinkedHashMap<Block, Item> result = new LinkedHashMap<>();

        entriesForMod(BuiltInRegistries.ITEM, modId)
                .forEach((item, id) -> {
                    if (item instanceof BlockItem blockItem) {
                        result.put(blockItem.getBlock(), item);
                    }
                });

        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Block Registration Methods
    //------------------------------------------------------------------------------------------------------------------
    public static Block registerBlock(ResourceLocation blockID, BlockBehaviour.Properties properties) {
        return registerBlock(blockID, Block::new, properties, BlockItem::new, new Item.Properties());
    }
    public static Block registerBlock(ResourceLocation blockID, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties blockProperties) {
        return registerBlock(blockID, blockFactory, blockProperties, BlockItem::new, new Item.Properties());
    }
    public static Block registerBlock(ResourceLocation blockID, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties blockProperties,
                                      Item.Properties itemProperties) {
        return registerBlock(blockID, blockFactory, blockProperties, null, itemProperties);
    }
    public static Block registerBlock(ResourceLocation blockID, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties blockProperties,
                                      BiFunction<Block, Item.Properties, Item> blockItemFactory) {
        return registerBlock(blockID, blockFactory, blockProperties, blockItemFactory, null);
    }
    public static Block registerBlockWithoutItem(ResourceLocation blockID, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties blockProperties) {
        return registerBlock(blockID, blockFactory, blockProperties, null, null);
    }
    public static Block registerBlock(ResourceLocation blockID, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties blockProperties,
                                      @Nullable BiFunction<Block, Item.Properties, Item> blockItemFactory, @Nullable Item.Properties itemProperties) {
        final Block BLOCK = Blocks.register(ResourceKey.create(Registries.BLOCK, blockID), blockFactory, blockProperties);
        if (!(blockItemFactory == null && itemProperties == null)) {
            blockItemFactory = blockItemFactory == null ? BlockItem::new : blockItemFactory;
            itemProperties = itemProperties == null ? new Item.Properties() : itemProperties;
            Items.registerBlock(BLOCK, blockItemFactory, itemProperties);
        }
        return BLOCK;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Item Registration Methods
    //------------------------------------------------------------------------------------------------------------------
    private static Item registerItem(ResourceLocation itemID) {
        return registerItem(itemID, Item::new);
    }
    private static Item registerItem(ResourceLocation itemID, Function<Item.Properties, Item> factory) {
        return registerItem(itemID, factory, new Item.Properties());
    }
    private static Item registerItem(ResourceLocation itemID, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return Items.registerItem(ResourceKey.create(Registries.ITEM, itemID), factory, settings);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Creative Mode Tab Registration Methods
    //------------------------------------------------------------------------------------------------------------------
    public static ResourceKey<CreativeModeTab> registerCreativeModeTab(ResourceLocation tabID, ItemLike icon, int column, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
        ResourceKey<CreativeModeTab> returnKey = ResourceKey.create(Registries.CREATIVE_MODE_TAB, tabID);
        CreativeModeTab.Row row = Math.floor((double) 5 / column) % 2 != 0 ? CreativeModeTab.Row.TOP : net.minecraft.world.item.CreativeModeTab.Row.BOTTOM;
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, returnKey,
                CreativeModeTab.builder(row, 6 + column)
                        .title(Component.translatable("creativeTab." + tabID.getNamespace() + "." + tabID.getPath()))
                        .icon(() -> new ItemStack(icon))
                        .displayItems(displayItemsGenerator)
                        .build());
        return returnKey;
    }
}