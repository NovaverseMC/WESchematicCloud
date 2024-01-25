package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import it.novaverse.weschematiccloud.WESchematicCloud;
import it.novaverse.weschematiccloud.schematic.SchematicHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class DownloadCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    @SneakyThrows
    @CommandMethod("/download [format]")
    @CommandPermission("worldedit.clipboard.download")
    public void download(Player player, @Argument(value = "format", defaultValue = "sponge") @Greedy String formatName) {
        var actor = BukkitAdapter.adapt(player);
        var sessionManager = WorldEdit.getInstance().getSessionManager();
        var session = sessionManager.get(actor);

        ClipboardHolder clipboard;
        try {
            clipboard = session.getClipboard();
        } catch (EmptyClipboardException e) {
            actor.print(TextComponent.of("Your clipboard is empty!").color(TextColor.RED));
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByAlias(formatName);
        if (format == null) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
            return;
        }

        actor.print(TextComponent.of("Generating download url with format '" + format + "'"));
        SchematicHolder holder = new SchematicHolder(clipboard, format);
        weSchematicCloud.getSchematicUploader().upload(holder)
                .thenApplySync(url -> {
                    String urlString = url.toString();
                    actor.print(TextComponent.of("Download link: " + urlString)
                            .color(TextColor.GREEN)
                            .clickEvent(ClickEvent.openUrl(urlString)));
                    return null;
                })
                .exceptionallySync(throwable -> {
                    actor.print(TextComponent.of("Failed to generate download link: " + throwable.getMessage()).color(TextColor.RED));
                    weSchematicCloud.getSLF4JLogger().error("Unable to upload schematic!", throwable);
                    return null;
                });
    }
}
