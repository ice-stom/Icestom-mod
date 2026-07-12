import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.world.DimensionType;

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

        InstanceContainer temp = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        temp.setChunkSupplier(LightingChunk::new);
        temp.setChunkLoader(new PolarLoader(polarWorld));

        System.out.println("STATUS:Loading Chunks");
        List<CompletableFuture<Chunk>> futures = polarWorld.chunks().stream()
                .map(c -> temp.loadChunk(c.x(), c.z()))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("STATUS:Relighting Chunks");
        LightingChunk.relight(temp, temp.getChunks());

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
