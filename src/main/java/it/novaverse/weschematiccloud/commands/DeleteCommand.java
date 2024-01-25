package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import it.novaverse.weschematiccloud.WESchematicCloud;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class DeleteCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    @SneakyThrows
    @CommandMethod("/schematic delete <filename>")
    @CommandPermission("worldedit.schematic.delete")
    public void delete(Player player, @Argument("filename") String filename) {
        var actor = BukkitAdapter.adapt(player);
        weSchematicCloud.getSchematicCommands().delete(actor, filename);
    }
}
