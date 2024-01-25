package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import it.novaverse.weschematiccloud.WESchematicCloud;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class SaveCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    @SneakyThrows
    @CommandMethod("/schematic|schem save <filename> [format]")
    @CommandPermission("worldedit.schematic.save")
    public void save(
            Player player,
            @Argument("filename") String rawFileName,
            @Argument(value = "format", defaultValue = "sponge") String formatName,
            @Flag("f") boolean allowOverwrite
    ) {
        var sessionManager = WorldEdit.getInstance().getSessionManager();
        var actor = BukkitAdapter.adapt(player);
        var session = sessionManager.get(actor);
        weSchematicCloud.getSchematicCommands().save(actor, session, rawFileName, formatName, allowOverwrite);
    }
}
