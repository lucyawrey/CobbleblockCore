package com.cobblemon.example

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.RegisterCommandsEvent

@Mod("cobblemon_neoforge_mdk")
class ExampleNeoForgeMod {
    @SubscribeEvent
    fun onCommandRegistration(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("test").executes { context ->
                val species = PokemonSpecies.getByIdentifier(ResourceLocation.parse("cobblemon:bidoof"))
                context.getSource().sendSystemMessage(
                    Component.literal("Got species: ")
                        .withStyle(Style.EMPTY.withColor(0x03e3fc))
                        .append(species!!.translatedName)
                )
                return@executes 0
            }
        )
    }
}