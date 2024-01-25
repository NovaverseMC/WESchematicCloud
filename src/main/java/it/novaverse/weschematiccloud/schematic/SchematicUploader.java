package it.novaverse.weschematiccloud.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import it.novaverse.weschematiccloud.WESchematicCloud;
import it.novaverse.weschematiccloud.utils.NanoPromise;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Objects;
import java.util.UUID;

public class SchematicUploader {
    @Nonnull
    private final String backendUrl;

    public SchematicUploader(@Nonnull WESchematicCloud weSchematicCloud) {
        Objects.requireNonNull(weSchematicCloud);
        backendUrl = Objects.requireNonNull(weSchematicCloud.getConfig().getString("backendUrl"), "Backend Url not found");
    }

    @SneakyThrows
    @Nonnull
    public NanoPromise<URL> upload(@Nonnull SchematicHolder holder) {
        Objects.requireNonNull(holder);

        var extension = holder.getFormat().getPrimaryFileExtension();
        return NanoPromise.completed(holder)
                .thenApplySync(this::serializeSchematic)
                .thenApplyAsync(bytes -> uploadBytes(bytes, extension));
    }

    @Nonnull
    @SneakyThrows
    private URL uploadBytes(byte[] bytes, @Nonnull String extension) {
        Objects.requireNonNull(extension);

        var uuid = UUID.randomUUID().toString();
        var filename = uuid + "." + extension;

        @Cleanup
        var httpClient = HttpClients.createDefault();
        var postRequest = new HttpPost(backendUrl + "upload.php?" + uuid);

        @Cleanup
        var httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("schematicFile", bytes, ContentType.APPLICATION_OCTET_STREAM, filename)
                .build();
        postRequest.setEntity(httpEntity);

        @Cleanup
        var httpResponse = httpClient.execute(postRequest);
        var responseCode = httpResponse.getCode();
        if (responseCode != 200) {
            throw new IllegalArgumentException("Unexpected response code from backend! " + responseCode);
        }

        var downloadUrl = backendUrl + "?key=" + uuid + "&type=" + extension;
        return new URL(downloadUrl);
    }

    @SneakyThrows
    private byte[] serializeSchematic(@Nonnull SchematicHolder holder) {
        Objects.requireNonNull(holder);

        var cb = holder.getClipboard().getClipboard();

        @Cleanup
        var byteOut = new ByteArrayOutputStream();
        @Cleanup
        var bufferOut = new BufferedOutputStream(byteOut);

        // Can't be combined, we need to close it before invoking .toByteArray()
        try (var writer = holder.getFormat().getWriter(bufferOut)) {
            writer.write(cb);
        }

        return byteOut.toByteArray();
    }

    @Nonnull
    public NanoPromise<Clipboard> download(@Nonnull UUID uuid, @Nonnull ClipboardFormat format) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(format);

        var extension = format.getPrimaryFileExtension();
        return NanoPromise.start()
                .thenApplyAsync(n -> downloadBytes(uuid, extension))
                .thenApplySync(bytes -> deserializeSchematic(bytes, format));
    }

    @SneakyThrows
    private byte[] downloadBytes(@Nonnull UUID uuid, @Nonnull String extension) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(extension);

        var webUrl = new URL(backendUrl + "uploads/" + uuid + "." + extension);

        @Cleanup
        var byteChannel = Channels.newChannel(webUrl.openStream());
        @Cleanup
        var in = Channels.newInputStream(byteChannel);

        return in.readAllBytes();
    }

    @Nonnull
    @SneakyThrows
    private Clipboard deserializeSchematic(byte[] bytes, @Nonnull ClipboardFormat format) {
        Objects.requireNonNull(format);

        @Cleanup
        var inputStream = new ByteArrayInputStream(bytes);
        @Cleanup
        var bufferInput = new BufferedInputStream(inputStream);
        @Cleanup
        var reader = format.getReader(bufferInput);

        return reader.read();
    }
}
