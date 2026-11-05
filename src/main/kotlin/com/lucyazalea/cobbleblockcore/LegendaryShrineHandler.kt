package com.lucyazalea.cobbleblockcore

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import java.util.ArrayDeque
import kotlin.random.Random

object LegendaryShrineHandler {
    private val pendingSummons = ArrayDeque<PendingSummon>()

    @SubscribeEvent
    fun onShrineInteraction(event: PlayerInteractEvent.RightClickBlock) {
        if (event.level.isClientSide || event.hand != InteractionHand.MAIN_HAND) return
        val level = event.level as? ServerLevel ?: return
        val shrine = LegendaryContent.SHRINES.firstOrNull {
            event.level.getBlockState(event.pos).`is`(
                LegendaryContent.shrineBlocks.getValue(it.id).get()
            )
        } ?: return

        event.isCanceled = true
        val player = event.entity as? net.minecraft.server.level.ServerPlayer ?: return
        val held = player.mainHandItem

        if (shrine.takesCoins && held.`is`(resourceItem("cobblemon:relic_coin"))) {
            held.shrink(1)
            player.displayClientMessage(Component.literal("§6You toss a coin in."), false)
            grantAdvancement(level, player, "shrine_donation")
            return
        }

        val counts = countRequirements(level, event.pos, shrine.requirements.keys)
        val missing = shrine.requirements.mapNotNull { (id, required) ->
            val amount = counts[id] ?: 0
            if (amount >= required) null else "${required - amount}× ${displayName(id)}"
        }

        if (missing.isNotEmpty()) {
            player.displayClientMessage(
                Component.literal("The shrine to ${shrine.displayName} is incomplete. Missing ${missing.joinToString(", ")}."),
                false
            )
            return
        }

        val summonItem = resourceItem("${LegendaryContent.MOD_ID}:${shrine.summonItem}")
        when {
            held.`is`(summonItem) -> summon(level, event.pos, player, shrine)
            held.isEmpty -> {
                if (shrine.id == "celebi" && level.dimension().location() == shrine.dimension) {
                    player.server.commands.performPrefixedCommand(
                        player.createCommandSourceStack(), "skyblock home"
                    )
                } else {
                    player.displayClientMessage(
                        Component.literal("The shrine to ${shrine.displayName} is ready."),
                        false
                    )
                }
            }
            else -> player.displayClientMessage(
                Component.literal("That offering is not right. Bring the ${displayName(BuiltInRegistries.ITEM.getKey(summonItem).toString())} to this shrine."),
                false
            )
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        while ((pendingSummons.firstOrNull()?.readyAt ?: Int.MAX_VALUE) <= event.server.tickCount) {
            val pending = pendingSummons.removeFirst()
            val level = event.server.getLevel(pending.dimension) ?: continue
            val x = pending.pos.x + 1.0 + Random.nextDouble() * 4.0
            val z = pending.pos.z + 1.0 + Random.nextDouble() * 4.0
            val shiny = if (Random.nextInt(4096) == 0) "shiny " else ""
            val command = "execute in ${pending.dimension.location()} run spawnpokemonat $x ${pending.pos.y} $z ${pending.id} $shiny${pending.data} level=${pending.level}"
            event.server.commands.performPrefixedCommand(
                event.server.createCommandSourceStack().withLevel(level), command
            )
        }
    }

    private fun summon(level: ServerLevel, pos: BlockPos, player: net.minecraft.server.level.ServerPlayer, shrine: LegendaryShrineDefinition) {
        if (shrine.id == "celebi" && level.dimension().location() != shrine.dimension) {
            player.server.commands.performPrefixedCommand(
                player.createCommandSourceStack(), "execute in ${shrine.dimension} run tp ${player.name.string} 0 70 0"
            )
            return
        }

        player.displayClientMessage(Component.literal("§b§l${shrine.displayName} is coming."), false)
        level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f)
        grantAdvancement(level, player, "legendary_sutargetmmon")
        player.mainHandItem.shrink(1)

        shrine.lootItem?.let {
            level.addFreshEntity(net.minecraft.world.entity.item.ItemEntity(
                level, pos.x + 0.5, pos.y + 2.0, pos.z + 0.5,
                ItemStack(resourceItem("${LegendaryContent.MOD_ID}:$it"))
            ))
        }
        if (Random.nextDouble() <= 0.25) {
            val page = ItemStack(resourceItem("${LegendaryContent.MOD_ID}:torn_page"))
            player.drop(page, false)
        }
        pendingSummons.add(
            PendingSummon(
                level.dimension(), pos, shrine.id, shrine.level, shrine.summonData,
                level.server.tickCount + 100
            )
        )
    }

    private fun countRequirements(level: ServerLevel, center: BlockPos, requiredIds: Set<String>): Map<String, Int> {
        val counts = HashMap<String, Int>()
        val relevantBlocks = requiredIds.mapTo(HashSet()) { LegendaryContent.blockId(it) }
        val pedestal = ResourceLocation.parse("mega_showdown:pedestal")
        for (x in center.x - 8..center.x + 8) {
            for (y in center.y - 8..center.y + 8) {
                for (z in center.z - 8..center.z + 8) {
                    val pos = BlockPos(x, y, z)
                    val id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).block)
                    if (id in relevantBlocks || id == pedestal) {
                        increment(counts, id.toString())
                        if (id == pedestal) {
                            pedestalItem(level.getBlockEntity(pos))?.let { increment(counts, it) }
                        }
                    }
                }
            }
        }
        return counts
    }

    private fun pedestalItem(entity: BlockEntity?): String? {
        val tag = entity?.saveWithoutMetadata(entity.level!!.registryAccess()) ?: return null
        val item = tag.getCompound("Item")
        return item.getString("id").takeIf { it.isNotEmpty() }
    }

    private fun increment(counts: MutableMap<String, Int>, id: String) {
        counts[id] = (counts[id] ?: 0) + 1
    }

    private fun displayName(id: String): String =
        Component.translatable("block.$id").string.let { if (it == "block.$id") id else it }

    private fun resourceItem(id: String) = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))

    private fun grantAdvancement(level: ServerLevel, player: net.minecraft.server.level.ServerPlayer, id: String) {
        val advancement = level.server.advancements.get(ResourceLocation.fromNamespaceAndPath("cobbleblock", id))
            ?: level.server.advancements.get(ResourceLocation.fromNamespaceAndPath("cobbleblockcore", id))
            ?: return
        player.advancements.award(advancement, advancement.value().criteria.keys.firstOrNull() ?: return)
    }

    private data class PendingSummon(
        val dimension: net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,
        val pos: BlockPos,
        val id: String,
        val level: Int,
        val data: String,
        val readyAt: Int,
    )
}
