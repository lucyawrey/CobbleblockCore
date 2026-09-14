package com.lucyazalea.cobbleblockcore.structure;

import com.lucyazalea.cobbleblockcore.FixedSeedNoiseChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AncientForestShrineStructure extends Structure {
    public static final MapCodec<AncientForestShrineStructure> CODEC =
            Structure.simpleCodec(AncientForestShrineStructure::new);
    public static final StructureType<AncientForestShrineStructure> TYPE = () -> CODEC;

    private static final int TARGET_CHUNK_X = -1;
    private static final int TARGET_CHUNK_Z = -3;
    private static final BlockPos ORIGIN = new BlockPos(-15, 69, -33);

    public AncientForestShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        if (!(context.chunkGenerator() instanceof FixedSeedNoiseChunkGenerator)
                || chunk.x != TARGET_CHUNK_X
                || chunk.z != TARGET_CHUNK_Z) {
            return Optional.empty();
        }

        StructureTemplateManager templates = context.structureTemplateManager();
        return Optional.of(new GenerationStub(ORIGIN, pieces ->
                pieces.addPiece(new ShrinePiece(templates, ORIGIN))));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return TYPE;
    }
}
