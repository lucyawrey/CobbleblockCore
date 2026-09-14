package com.lucyazalea.cobbleblockcore.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;

public final class ShrinePiece extends TemplateStructurePiece {
    public static final StructurePieceType.StructureTemplateType TYPE =
            ShrinePiece::new;
    private static final ResourceLocation TEMPLATE =
            ResourceLocation.fromNamespaceAndPath("cobbleblockcore", "shrine");

    public ShrinePiece(StructureTemplateManager templates, BlockPos position) {
        super(
                TYPE,
                0,
                templates,
                TEMPLATE,
                TEMPLATE.toString(),
                new StructurePlaceSettings().setKnownShape(true),
                position
        );
    }

    public ShrinePiece(StructureTemplateManager templates, net.minecraft.nbt.CompoundTag tag) {
        super(
                TYPE,
                tag,
                templates,
                ignored -> new StructurePlaceSettings().setKnownShape(true)
        );
    }

    @Override
    protected void handleDataMarker(
            @NotNull String marker,
            @NotNull BlockPos position,
            @NotNull ServerLevelAccessor level,
            @NotNull RandomSource random,
            @NotNull BoundingBox boundingBox
    ) {
    }
}
