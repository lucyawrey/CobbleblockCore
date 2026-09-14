package com.lucyazalea.cobbleblockcore

import com.lucyazalea.cobbleblockcore.structure.AncientForestShrineStructure
import com.lucyazalea.cobbleblockcore.structure.ShrinePiece
import net.neoforged.fml.common.Mod
import net.neoforged.fml.ModLoadingContext
import net.neoforged.neoforge.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import com.mojang.serialization.MapCodec
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import java.util.function.Supplier

@Mod("cobbleblockcore")
class CobbleblockCore {
    init {
        CHUNK_GENERATORS.register(requireNotNull(ModLoadingContext.get().activeContainer.eventBus))
        STRUCTURE_TYPES.register(requireNotNull(ModLoadingContext.get().activeContainer.eventBus))
        STRUCTURE_PIECES.register(requireNotNull(ModLoadingContext.get().activeContainer.eventBus))
        // NeoForge.EVENT_BUS.register(this) uncomment if adding events
    }

    private companion object {
        val CHUNK_GENERATORS: DeferredRegister<MapCodec<out ChunkGenerator>> =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, "cobbleblockcore").also {
                it.register("fixed_seed_noise", Supplier { FixedSeedNoiseChunkGenerator.CODEC })
            }

        val STRUCTURE_TYPES: DeferredRegister<StructureType<*>> =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, "cobbleblockcore").also {
                it.register(
                    "ancient_forest_shrine",
                    Supplier { AncientForestShrineStructure.TYPE }
                )
            }

        val STRUCTURE_PIECES: DeferredRegister<StructurePieceType> =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, "cobbleblockcore").also {
                it.register(
                    "shrine",
                    Supplier { ShrinePiece.TYPE }
                )
            }
    }
}