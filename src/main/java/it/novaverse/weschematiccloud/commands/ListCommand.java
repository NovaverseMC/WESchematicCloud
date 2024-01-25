package it.novaverse.weschematiccloud.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.injection.RawArgs;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import it.novaverse.weschematiccloud.WESchematicCloud;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ListCommand {

    @Nonnull
    private final WESchematicCloud weSchematicCloud;

    @CommandMethod("/schematic|schem list")
    @CommandPermission("worldedit.schematic.list")
    @RawArgs
    public void list(
            Player player,
            @Flag("p") Integer page,
            @Flag("d") boolean oldestDate,
            @Flag("n") boolean newestDate
    ) {
        var actor = BukkitAdapter.adapt(player);
        weSchematicCloud.getSchematicCommands().list(
                actor,
                page == null ? 1 : page,
                oldestDate,
                newestDate
        );
    }
}
