package io.gitlab.icestom.mod.export_worker;

import net.kyori.adventure.nbt.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class AnvilDisplayEntityExporter {

    private static final Set<String> DISPLAY_TYPES = Set.of(
            "minecraft:block_display",
            "minecraft:item_display",
            "minecraft:text_display"
    );

    private AnvilDisplayEntityExporter() {}

    public static Map<ChunkPos, List<CompoundBinaryTag>> read(Path worldPath)
            throws IOException {

        Path entitiesPath = worldPath.resolve("entities");

        if (!Files.isDirectory(entitiesPath)) {
            return Map.of();
        }

        Map<ChunkPos, List<CompoundBinaryTag>> result =
                new HashMap<>();

        try (DirectoryStream<Path> regions =
                     Files.newDirectoryStream(entitiesPath, "r.*.*.mca")) {

            for (Path regionFile : regions) {
                readRegion(regionFile, result);
            }
        }

        return result;
    }

    private static void readRegion(
            Path path,
            Map<ChunkPos, List<CompoundBinaryTag>> result
    ) throws IOException {

        String filename = path.getFileName().toString();

        String[] parts = filename
                .substring(2, filename.length() - 4)
                .split("\\.");

        int regionX = Integer.parseInt(parts[0]);
        int regionZ = Integer.parseInt(parts[1]);

        try (RandomAccessFile file = new RandomAccessFile(
                path.toFile(), "r"
        )) {
            long fileLength = file.length();

            // Region files can be empty placeholders, truncated, or still
            // being written by a live server concurrently with export.
            // Anything smaller than one full header is not readable yet.
            if (fileLength < 8192) {
                return;
            }

            byte[] header = new byte[8192];
            file.readFully(header);

            ByteBuffer offsets = ByteBuffer
                    .wrap(header)
                    .order(ByteOrder.BIG_ENDIAN);

            for (int localZ = 0; localZ < 32; localZ++) {
                for (int localX = 0; localX < 32; localX++) {

                    int offset = offsets.getInt(
                            (localX + localZ * 32) * 4
                    );

                    if (offset == 0) {
                        continue;
                    }

                    int sectorOffset = offset >>> 8;
                    int sectorCount = offset & 0xFF;

                    // sectorOffset < 2 would overlap the 8KiB header itself
                    // and is never valid; sectorCount == 0 is meaningless.
                    if (sectorOffset < 2 || sectorCount == 0) {
                        continue;
                    }

                    long fileOffset =
                            (long) sectorOffset * 4096L;

                    // Not enough room left in the file even for the 4-byte
                    // length prefix -> corrupt/truncated entry, skip it.
                    if (fileOffset + 4L > fileLength) {
                        continue;
                    }

                    file.seek(fileOffset);

                    int length = file.readInt();

                    if (length <= 1) {
                        continue;
                    }

                    // Payload would run past the end of the file -> skip.
                    if (fileOffset + 4L + length > fileLength) {
                        continue;
                    }

                    int compressionType = file.readUnsignedByte();

                    byte[] compressed = new byte[length - 1];
                    file.readFully(compressed);

                    byte[] nbtData = decompress(
                            compressed,
                            compressionType
                    );

                    CompoundBinaryTag chunk =
                            readCompound(nbtData);

                    int chunkX =
                            regionX * 32 + localX;

                    int chunkZ =
                            regionZ * 32 + localZ;

                    readEntities(
                            chunk,
                            chunkX,
                            chunkZ,
                            result
                    );
                }
            }
        }
    }

    private static void readEntities(
            CompoundBinaryTag chunk,
            int chunkX,
            int chunkZ,
            Map<ChunkPos, List<CompoundBinaryTag>> result
    ) {
        BinaryTag entitiesTag =
                chunk.get("Entities");

        if (!(entitiesTag instanceof ListBinaryTag entities)) {
            return;
        }

        ChunkPos chunkPos =
                new ChunkPos(chunkX, chunkZ);

        for (BinaryTag tag : entities) {

            if (!(tag instanceof CompoundBinaryTag entity)) {
                continue;
            }

            String id = entity.getString("id");

            if (!DISPLAY_TYPES.contains(id)) {
                continue;
            }

            result.computeIfAbsent(
                    chunkPos,
                    ignored -> new ArrayList<>()
            ).add(entity);
        }
    }

    private static byte[] decompress(
            byte[] data,
            int compressionType
    ) throws IOException {
        return switch (compressionType) {
            case 1 -> {
                try (ByteArrayInputStream input =
                             new ByteArrayInputStream(data);
                     GZIPInputStream gzip =
                             new GZIPInputStream(input);
                     ByteArrayOutputStream output =
                             new ByteArrayOutputStream()) {

                    gzip.transferTo(output);
                    yield output.toByteArray();
                }
            }

            case 2 -> {
                try (ByteArrayInputStream input =
                             new ByteArrayInputStream(data);
                     InflaterInputStream inflater =
                             new InflaterInputStream(input);
                     ByteArrayOutputStream output =
                             new ByteArrayOutputStream()) {

                    inflater.transferTo(output);
                    yield output.toByteArray();
                }
            }
            case 3 -> data;

            default ->
                    throw new IOException(
                            "Unknown Anvil compression type: "
                                    + compressionType
                    );
        };
    }

    private static CompoundBinaryTag readCompound(
            byte[] data
    ) throws IOException {
        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(data)) {

            return BinaryTagIO.unlimitedReader()
                    .read(input);
        }
    }

    public record ChunkPos(int x, int z) {}
}