package dev.dragonride

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import org.slf4j.LoggerFactory

/**
 * `/dragonride [scale] [fly]` summons an ender dragon at the player, shrinks it via the scale
 * attribute, and seats the player on it. The client mixins in [dev.dragonride.mixin] are what make
 * the attribute visually shrink the dragon, because vanilla's dragon renderer ignores it.
 */
object DragonRideMod : ModInitializer {

    private val LOG = LoggerFactory.getLogger("dragonride")

    /** Default size when no scale is given (1.0 = full size). */
    private const val DEFAULT_SCALE = 0.25f

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(
                literal("dragonride")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)) // op level 2
                    .executes { spawnAndRide(it, DEFAULT_SCALE, false) }
                    .then(
                        argument("scale", FloatArgumentType.floatArg(0.05f, 16.0f))
                            .executes { spawnAndRide(it, FloatArgumentType.getFloat(it, "scale"), false) }
                            .then(
                                argument("fly", BoolArgumentType.bool())
                                    .executes {
                                        spawnAndRide(
                                            it,
                                            FloatArgumentType.getFloat(it, "scale"),
                                            BoolArgumentType.getBool(it, "fly"),
                                        )
                                    },
                            ),
                    ),
            )
        })
        LOG.info("Dragon Ride ready: /dragonride [scale] [fly].")
    }

    private fun spawnAndRide(ctx: CommandContext<CommandSourceStack>, scale: Float, fly: Boolean): Int {
        val source = ctx.source
        val player = source.playerOrException
        val level = source.level

        val dragon = EntityTypes.ENDER_DRAGON.spawn(level, player.blockPosition(), EntitySpawnReason.COMMAND)
        if (dragon == null) {
            source.sendFailure(Component.literal("[DragonRide] Couldn't spawn the dragon."))
            return 0
        }

        // fly=false gives NoAI, so it does not wander, attack or run End-fight phases.
        // fly=true leaves AI on; it flies its own patterns and is still not steerable.
        dragon.isNoAi = !fly
        // Vanilla applies this to the hitbox and riding math; the client mixin applies it to the model.
        val attr = dragon.getAttribute(Attributes.SCALE)
        if (attr != null) {
            attr.baseValue = scale.toDouble()
            LOG.info(
                "[diag] after set: getBaseValue={}, getValue={}, entity.getScale={}",
                attr.baseValue, attr.value, dragon.scale,
            )
        } else {
            LOG.info("[diag] SCALE attribute is NULL on the dragon")
        }

        // force = true so the player can mount a mob that normally isn't rideable.
        player.startRiding(dragon, true, true)

        source.sendSuccess(
            { Component.literal("[DragonRide] Dragon summoned: scale $scale, fly=$fly. Hold on!") },
            false,
        )
        LOG.info("{} mounted a scale-{} dragon (fly={}).", player.name.string, scale, fly)
        return 1
    }
}
