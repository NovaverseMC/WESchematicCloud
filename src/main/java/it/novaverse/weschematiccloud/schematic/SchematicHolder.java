package it.novaverse.weschematiccloud.schematic;

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

@AllArgsConstructor
@Getter
@Setter
public class SchematicHolder {
    @Nonnull
    private ClipboardHolder clipboard;
    @Nonnull
    private ClipboardFormat format;
}
