package com.lucyazalea.cobbleblockcore

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

data class LegendaryShrineDefinition(
    val id: String,
    val displayName: String,
    val level: Int,
    val summonItem: String,
    val lootItem: String?,
    val requirements: Map<String, Int>,
    val takesCoins: Boolean = false,
    val dimension: ResourceLocation = ResourceLocation.withDefaultNamespace("overworld"),
    val summonData: String = "min_perfect_ivs=3",
    val shape: VoxelShape = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0),
)

object LegendaryContent {
    const val MOD_ID = "cobbleblockcore"

    val SHRINES: List<LegendaryShrineDefinition> = listOf(
        LegendaryShrineDefinition(
            "articuno", "Articuno", 60, "arctic_wing", "articuno_orb",
            mapOf(
                "minecraft:polished_diorite" to 20, "minecraft:ice" to 20,
                "minecraft:packed_ice" to 10, "minecraft:blue_ice" to 10,
                "minecraft:diamond_block" to 1
            )
        ),
        LegendaryShrineDefinition(
            "zapdos", "Zapdos", 60, "charged_wing", "zapdos_orb",
            mapOf(
                "minecraft:yellow_concrete" to 30, "minecraft:redstone_block" to 10,
                "minecraft:redstone_torch" to 6, "minecraft:lightning_rod" to 2,
                "minecraft:gold_block" to 2
            )
        ),
        LegendaryShrineDefinition(
            "moltres", "Moltres", 60, "molten_wing", "moltres_orb",
            mapOf(
                "minecraft:polished_blackstone" to 30, "minecraft:fire" to 10,
                "minecraft:netherrack" to 10, "minecraft:nether_bricks" to 10,
                "minecraft:iron_block" to 3
            )
        ),
        LegendaryShrineDefinition(
            "lugia", "Lugia", 65, "fragile_ocarina", null,
            mapOf(
                "mega_showdown:pedestal" to 3, "cobbleblockcore:articuno_orb" to 1,
                "cobbleblockcore:zapdos_orb" to 1, "cobbleblockcore:moltres_orb" to 1,
                "minecraft:water" to 10, "minecraft:prismarine" to 20,
                "minecraft:lapis_block" to 8, "minecraft:smooth_quartz" to 10
            )
        ),
        LegendaryShrineDefinition(
            "mew", "Mew", 70, "ancient_fossil", null,
            mapOf(
                "minecraft:diamond_block" to 4, "minecraft:amethyst_block" to 5,
                "minecraft:mossy_stone_bricks" to 10, "minecraft:mossy_cobblestone" to 10,
                "minecraft:cherry_planks" to 10
            )
        ),
        LegendaryShrineDefinition(
            "hooh", "Ho-Oh", 65, "rainbow_feather", null, mapOf(
                "minecraft:bell" to 1, "minecraft:gold_block" to 10,
                "minecraft:bamboo_planks" to 15, "minecraft:crimson_planks" to 10,
                "minecraft:deepslate_bricks" to 10, "minecraft:deepslate_brick_stairs" to 10
            ), takesCoins = true, shape = Block.box(0.0, 0.0, 2.0, 16.0, 13.0, 14.0)
        ),
        LegendaryShrineDefinition(
            "celebi", "Celebi", 70, "time_flute", null,
            mapOf("cobblemon:leaf_stone_block" to 1, "minecraft:mossy_cobblestone" to 10),
            takesCoins = true,
            dimension = ResourceLocation.fromNamespaceAndPath(MOD_ID, "ancient_forest"),
            shape = Block.box(0.0, 0.0, 2.0, 16.0, 13.0, 14.0)
        )
    )

    val EXTRA_ITEMS: Map<String, String> = mapOf(
        "blue_feather" to "A feather dropped by Suicune. One of three needed to craft the Rainbow Feather.",
        "red_feather" to "A feather dropped by Entei. One of three needed to craft the Rainbow Feather.",
        "yellow_feather" to "A feather dropped by Raikou. One of three needed to craft the Rainbow Feather.",
        "ancient_fossil_piece" to "Use four pieces to craft an Ancient Fossil, or try crushing one.",
        "ancient_fossil_dust" to "A certain Pokémon's DNA could be extracted from this.",
        "ancient_dna" to "This DNA should be able to clone the Ancient Pokémon Mew."
    )

    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(
        net.minecraft.core.registries.Registries.BLOCK, MOD_ID
    )
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(
        net.minecraft.core.registries.Registries.ITEM, MOD_ID
    )

    private val blockEntries = SHRINES.associate { shrine ->
        shrine.id to BLOCKS.register("${shrine.id}_shrine", Supplier {
            ShrineBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE)
                    .strength(10.0f, 100.0f)
                    .requiresCorrectToolForDrops(),
                shrine.shape
            )
        })
    }

    val shrineBlocks = blockEntries

    init {
        SHRINES.forEach { shrine ->
            ITEMS.register("${shrine.id}_shrine", Supplier {
                net.minecraft.world.item.BlockItem(
                    blockEntries.getValue(shrine.id).get(),
                    Item.Properties()
                )
            })
        }
        SHRINES.forEach { shrine ->
            ITEMS.register(shrine.summonItem, Supplier { Item(Item.Properties()) })
            shrine.lootItem?.let { ITEMS.register(it, Supplier { Item(Item.Properties()) }) }
        }
        EXTRA_ITEMS.forEach { (id, _) -> ITEMS.register(id, Supplier { Item(Item.Properties()) }) }
        ITEMS.register("torn_page", Supplier { Item(Item.Properties()) })
    }

    fun itemId(id: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, id.substringAfter(':'))

    fun blockId(id: String): ResourceLocation =
        ResourceLocation.parse(id)

    fun registeredBlock(id: String): Block =
        BuiltInRegistries.BLOCK.get(blockId(id))
}
