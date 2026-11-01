import io.gitlab.icestom.mod.export_worker.AnvilDisplayEntityExporter;
import net.hollowcube.polar.*;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

void main(String[] args) {
    boolean serverInitialized = false;
    try {
        Path anvilPath = Path.of(args[0]);
        Path polarPath = Path.of(args[1]);

        System.out.println("STATUS:Initializing Instance");
        MinecraftServer.init();
        serverInitialized = true;

        System.out.println("STATUS:Loading Anvil World");
        PolarWorld polarWorld = AnvilPolar.anvilToPolar(anvilPath);

        System.out.println("STATUS:Loading Display Entities");
        var displayEntities = AnvilDisplayEntityExporter.read(anvilPath);

        System.out.println("STATUS:Found " + displayEntities.size());

        InstanceContainer temp = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        temp.setChunkSupplier(LightingChunk::new);
        temp.setChunkLoader(new PolarLoader(polarWorld)
                .setWorldAccess(new DisplayEntityWorldAccess(displayEntities)));

        System.out.println("STATUS:Loading Chunks");
        List<CompletableFuture<Chunk>> futures = polarWorld.chunks().stream()
                .map(c -> temp.loadChunk(c.x(), c.z()))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("STATUS:Relighting Chunks");
        LightingChunk.relight(temp, temp.getChunks());

        System.out.println("STATUS:Freezing world data.");
        temp.getChunkLoader().saveChunks(temp.getChunks());

        System.out.println("STATUS:Writing World Data");
        byte[] bytes = PolarWriter.write(polarWorld);
        Files.write(polarPath, bytes);

        System.out.println("DONE");
    } catch (Throwable t) {
        t.printStackTrace();
        System.exit(1);
    } finally {
        if (serverInitialized) {
            try {
                MinecraftServer.stopCleanly();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    System.exit(0);
}

static class DisplayEntityWorldAccess implements PolarWorldAccess {

    private final Map<AnvilDisplayEntityExporter.ChunkPos, List<CompoundBinaryTag>> displayEntities;

    DisplayEntityWorldAccess(
            Map<AnvilDisplayEntityExporter.ChunkPos, List<CompoundBinaryTag>> displayEntities
    ) {
        this.displayEntities = displayEntities;
    }

    @Override
    public void saveChunkData(@NotNull Chunk chunk, @NotNull NetworkBuffer userData) {
        var key = new AnvilDisplayEntityExporter.ChunkPos(
                chunk.getChunkX(),
                chunk.getChunkZ()
        );

        List<CompoundBinaryTag> entities =
                displayEntities.getOrDefault(key, List.of());

        ListBinaryTag entityList = ListBinaryTag.builder()
                .add(entities)
                .build();

        CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put("entities", entityList)
                .build();

        userData.write(NetworkBuffer.NBT_COMPOUND, root);
    }
}
