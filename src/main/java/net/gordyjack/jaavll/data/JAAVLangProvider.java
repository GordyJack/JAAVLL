package net.gordyjack.jaavll.data;

import net.fabricmc.fabric.api.datagen.v1.*;
import net.fabricmc.fabric.api.datagen.v1.provider.*;
import net.minecraft.core.*;
import org.apache.commons.lang3.text.*;

import java.util.concurrent.*;

public abstract class JAAVLangProvider extends FabricLanguageProvider {
    protected JAAVLangProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, languageCode, registryLookup);
    }

    protected static String displayName(String descriptionId) {
        return displayName(descriptionId, true);
    }
    @SuppressWarnings("deprecation")
    protected static String displayName(String descriptionId, boolean stripBlockSuffix) {
        // Get the namespace segment between the last two dots
        String namespaceWord = extractNamespaceWord(descriptionId);

        // Toss the leading "whatever.jaavaa." section
        String name = descriptionId.substring(descriptionId.lastIndexOf('.') + 1);

        // Optional special case for blocks
        if (stripBlockSuffix) {
            name = name.replaceAll("_block(?!s)", "");
        }

        // Normalize and space it out
        name = name.replace('-', '_').replace('_', ' ');

        // Capitalize words
        name = WordUtils.capitalizeFully(name);

        // Force namespace word into uppercase anywhere it appears
        // (?i) = case-insensitive
        name = name.replaceAll("(?i)" + namespaceWord, namespaceWord.toUpperCase());

        // Lowercase tiny words
        name = name.replace(" Of ", " of ")
                .replace(" The ", " the ");

        return name;
    }
    private static String extractNamespaceWord(String descriptionId) {
        int last = descriptionId.lastIndexOf('.');
        int prev = descriptionId.lastIndexOf('.', last - 1);
        return descriptionId.substring(prev + 1, last);
    }
}
