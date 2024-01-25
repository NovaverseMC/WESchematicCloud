package it.novaverse.weschematiccloud;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class WESchematicCloudLoader implements PluginLoader {

    @Override
    public void classloader(@Nonnull PluginClasspathBuilder classpathBuilder) {
        Objects.requireNonNull(classpathBuilder);

        var resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.httpcomponents.client5:httpclient5:5.3"), null));

        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-paper:1.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-annotations:1.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("me.lucko:commodore:2.2"), null));

        resolver.addRepository(new RemoteRepository.Builder("maven-central", "default", "https://repo.maven.apache.org/maven2").build());

        classpathBuilder.addLibrary(resolver);
    }
}
