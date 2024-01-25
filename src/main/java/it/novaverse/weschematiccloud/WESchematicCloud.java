package it.novaverse.weschematiccloud;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.SchematicCommands;
import it.novaverse.weschematiccloud.commands.*;
import it.novaverse.weschematiccloud.schematic.SchematicUploader;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class WESchematicCloud extends JavaPlugin {

    private SchematicUploader schematicUploader;
    private SchematicCommands schematicCommands;

    @SneakyThrows
    @Override
    public void onEnable() {
        schematicUploader = new SchematicUploader(this);

        var paperCommandManager = new PaperCommandManager<>(
                this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            paperCommandManager.registerBrigadier();
            getSLF4JLogger().info("Brigadier support enabled");
        }
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            paperCommandManager.registerAsynchronousCompletions();
            getSLF4JLogger().info("Asynchronous completions enabled");
        }
        paperCommandManager.setSetting(CommandManager.ManagerSettings.OVERRIDE_EXISTING_COMMANDS, false);

        var annotationParser = new AnnotationParser<>(paperCommandManager, CommandSender.class,
                parameters -> SimpleCommandMeta.builder().with(CommandMeta.DESCRIPTION, "No description").build()
        );

        schematicCommands = new SchematicCommands(WorldEdit.getInstance());

        saveDefaultConfig();

        annotationParser.parse(new DownloadCommand(this));
        annotationParser.parse(new LoadCommand(this));
        annotationParser.parse(new SaveCommand(this));
        annotationParser.parse(new FormatsCommand(this));
        annotationParser.parse(new ListCommand(this));
        annotationParser.parse(new DeleteCommand(this));
    }

    @Nonnull
    public SchematicUploader getSchematicUploader() {
        Objects.requireNonNull(schematicUploader);
        return schematicUploader;
    }

    @Nonnull
    public SchematicCommands getSchematicCommands() {
        Objects.requireNonNull(schematicCommands);
        return schematicCommands;
    }
}
