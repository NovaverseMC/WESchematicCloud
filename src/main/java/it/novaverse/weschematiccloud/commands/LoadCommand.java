package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import it.novaverse.weschematiccloud.WESchematicCloud;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class LoadCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    public LoadCommand(@Nonnull WESchematicCloud weSchematicCloud) {
        Objects.requireNonNull(weSchematicCloud);
        this.weSchematicCloud = weSchematicCloud;
    }

    @SneakyThrows
    @CommandMethod("/schematic|schem load <filename> [format]")
    @CommandPermission("worldedit.schematic.load")
    public void load(
            Player player,
            @Argument("filename") String filename,
            @Argument(value = "format", defaultValue = "sponge") @Greedy String formatName
    ) {

        var actor = BukkitAdapter.adapt(player);
        var sessionManager = WorldEdit.getInstance().getSessionManager();
        var session = sessionManager.get(actor);

        if (formatName.startsWith("url:")) { // Dirty hack to allow easy copy and paste from cloud
            var tmp = formatName;
            formatName = filename;
            filename = tmp;
        }

        if (!filename.startsWith("url:")) {
            weSchematicCloud.getSchematicCommands().load(actor, session, filename, formatName);
            return;
        }

        if (!actor.hasPermission("worldedit.schematic.load.web")) {
            actor.print(TextComponent.of("You have no permission: worldedit.schematic.load.web").color(TextColor.RED));
            return;
        }

        var format = ClipboardFormats.findByAlias(formatName);
        if (format == null) {
            actor.print(TextComponent.of("Unknown format: " + formatName).color(TextColor.RED));
            return;
        }

        var uuid = UUID.fromString(filename.substring(4));

        final String finalFilename = filename;
        weSchematicCloud.getSchematicUploader().download(uuid, format)
                .thenApplySync(clipboard -> {
                    session.setClipboard(null);
                    var holder = new ClipboardHolder(clipboard);
                    session.setClipboard(holder);

                    actor.print(TextComponent.of("Loaded schematic: " + finalFilename).color(TextColor.GREEN));
                    return null;
                })
                .exceptionallySync(throwable -> {
                    actor.print(TextComponent.of("Unable to load schematic: " + throwable.getMessage()).color(TextColor.RED));
                    weSchematicCloud.getSLF4JLogger().error("Failed to load a saved clipboard", throwable);
                    return null;
                });
    }
}
