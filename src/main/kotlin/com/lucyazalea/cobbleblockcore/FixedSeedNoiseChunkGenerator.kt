package com.lucyazalea.cobbleblockcore

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.core.BlockPos
import java.util.concurrent.CompletableFuture

class FixedSeedNoiseChunkGenerator(
    biomeSource: BiomeSource,
    settings: Holder<NoiseGeneratorSettings>,
    val fixedSeed: Long
) : NoiseBasedChunkGenerator(biomeSource, settings) {
    @Volatile
    private var fixedRandomState: RandomState? = null

    private fun fixedState(registryAccess: RegistryAccess): RandomState {
        fixedRandomState?.let { return it }
        return synchronized(this) {
            fixedRandomState ?: RandomState.create(
                generatorSettings().value(),
                registryAccess.lookupOrThrow(Registries.NOISE),
                fixedSeed
            ).also { fixedRandomState = it }
        }
    }

    private fun terrainState(randomState: RandomState): RandomState = fixedRandomState ?: randomState

    private fun terrainState(level: LevelHeightAccessor, randomState: RandomState): RandomState =
        fixedRandomState ?: (level as? LevelReader)?.let { fixedState(it.registryAccess()) } ?: randomState

    override fun codec(): MapCodec<out net.minecraft.world.level.chunk.ChunkGenerator> = CODEC

    override fun createBiomes(
        randomState: RandomState,
        blender: Blender,
        structureManager: StructureManager,
        chunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> =
        super.createBiomes(fixedState(structureManager.registryAccess()), blender, structureManager, chunk)

    override fun fillFromNoise(
        blender: Blender,
        randomState: RandomState,
        structureManager: StructureManager,
        chunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> =
        super.fillFromNoise(blender, fixedState(structureManager.registryAccess()), structureManager, chunk)

    override fun buildSurface(
        region: WorldGenRegion,
        structureManager: StructureManager,
        randomState: RandomState,
        chunk: ChunkAccess
    ) {
        super.buildSurface(region, structureManager, fixedState(structureManager.registryAccess()), chunk)
    }

    override fun applyCarvers(
        region: WorldGenRegion,
        seed: Long,
        randomState: RandomState,
        biomeManager: BiomeManager,
        structureManager: StructureManager,
        chunk: ChunkAccess,
        carvingStep: GenerationStep.Carving
    ) {
        super.applyCarvers(
            region,
            fixedSeed,
            fixedState(structureManager.registryAccess()),
            biomeManager,
            structureManager,
            chunk,
            carvingStep
        )
    }

    override fun getBaseHeight(
        x: Int,
        z: Int,
        heightmapType: Heightmap.Types,
        level: LevelHeightAccessor,
        randomState: RandomState
    ): Int = super.getBaseHeight(x, z, heightmapType, level, terrainState(level, randomState))

    override fun getBaseColumn(
        x: Int,
        z: Int,
        level: LevelHeightAccessor,
        randomState: RandomState
    ): NoiseColumn = super.getBaseColumn(x, z, level, terrainState(level, randomState))

    override fun addDebugScreenInfo(
        lines: MutableList<String>,
        randomState: RandomState,
        pos: BlockPos
    ) {
        super.addDebugScreenInfo(lines, terrainState(randomState), pos)
    }

    companion object {
        const val DEFAULT_FIXED_SEED = 1922393360843319768L

        val CODEC: MapCodec<FixedSeedNoiseChunkGenerator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BiomeSource.CODEC.fieldOf("biome_source")
                    .forGetter(FixedSeedNoiseChunkGenerator::getBiomeSource),
                NoiseGeneratorSettings.CODEC.fieldOf("settings")
                    .forGetter(FixedSeedNoiseChunkGenerator::generatorSettings),
                Codec.LONG.optionalFieldOf("seed", DEFAULT_FIXED_SEED)
                    .forGetter(FixedSeedNoiseChunkGenerator::fixedSeed)
            ).apply(instance, ::FixedSeedNoiseChunkGenerator)
        }
    }
}
