package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import it.novaverse.weschematiccloud.WESchematicCloud;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class FormatsCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    @CommandMethod("/schematic|schem formats")
    @CommandPermission("worldedit.schematic.formats")
    public void formats(Player player) {
        var actor = BukkitAdapter.adapt(player);
        weSchematicCloud.getSchematicCommands().formats(actor);
    }
}
