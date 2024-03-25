package gg.norisk.subwaysurfers.server.command

import com.mojang.brigadier.context.CommandContext
import gg.norisk.subwaysurfers.common.collectible.Powerup
import gg.norisk.subwaysurfers.common.collectible.collectibles
import gg.norisk.subwaysurfers.network.s2c.*
import gg.norisk.subwaysurfers.server.ServerConfig
import gg.norisk.subwaysurfers.server.mechanics.PatternManager
import gg.norisk.subwaysurfers.server.mechanics.SpeedManager
import gg.norisk.subwaysurfers.subwaysurfers.*
import net.minecraft.block.Blocks
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText

object StartCommand {
    fun init() {
        command("subwaysurfers") {
            literal("debug") {
                runs {
                    val player = this.source.playerOrThrow
                    player.debugMode = !player.debugMode
                    player.sendMessage(literalText("Debug Mode: ") {
                        text(player.debugMode.toString())
                    })
                }
            }
            literal("stop") {
                runs { extracted(false) }
            }
            literal("start") {
                argument<Float>("yaw") { yawArg ->
                    runs { extracted(yawArg = yawArg()) }
                    argument<Float>("pitch") { pitchArg ->
                        runs { extracted(yawArg = yawArg(), pitchArg = pitchArg()) }
                        argument<Double>("desiredCameraDistance") { cameraDistanceArg ->
                            runs { extracted(true, cameraDistanceArg(), yawArg(), pitchArg()) }
                        }
                    }
                }
                runs { extracted() }
            }
        }
    }

    fun handleGameStop(player: ServerPlayerEntity) {
        gameOverScreenS2C.send(GameOverDto(player.coins, player.age), player)
        PatternManager.playerPatterns.remove(player.uuid)
        player.isSubwaySurfers = false
        player.coins = 0
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = SpeedManager.vanillaSpeed
        player.rail = 1
        player.world.playSoundFromEntity(
            null,
            player,
            SoundEvents.BLOCK_GLASS_BREAK,
            SoundCategory.PLAYERS,
            0.4f,
            0.8f
        )
    }

    fun handleStartGame(
        player: ServerPlayerEntity,
        isEnabled: Boolean = true,
        cameraDistanceArg: Double? = null,
        yawArg: Float? = null,
        pitchArg: Float? = null
    ) {
        val startStopPacket = StartStopPacket()
        val cameraSettings = CameraSettings()
        isEnabled.apply { startStopPacket.isEnabled = this }
        cameraDistanceArg?.apply { cameraSettings.desiredCameraDistance = this }
        yawArg?.apply { cameraSettings.yaw = this }
        pitchArg?.apply { cameraSettings.pitch = this }

        player.isSubwaySurfers = false

        if (isEnabled) {
            player.serverWorld.setBlockState(
                ServerConfig.config.startPos.toBlockPos().down(),
                Blocks.BEDROCK.defaultState
            )
            player.teleport(
                player.serverWorld,
                ServerConfig.config.startPos.x,
                ServerConfig.config.startPos.y,
                ServerConfig.config.startPos.z,
                0f,
                0f
            )

            val startTime = 3L
            val railPattern =
                PatternManager.playerPatterns.computeIfAbsent(player.uuid) { PatternManager.getRailPattern() }
            val patternPacket = PatternPacket(
                PatternManager.getEnvironmentPattern(),
                railPattern.map { it.railName },
                PatternManager.getEnvironmentPattern()
            )
            preStartS2C.send(
                PreStartS2C(
                    ServerConfig.config.startPos,
                    patternPacket,
                    startTime,
                    cameraSettings
                ), player
            )

            startTimer(startTime, player, startStopPacket)
        } else {
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = SpeedManager.vanillaSpeed
            player.isSubwaySurfers = false
            startStopPacketS2C.send(startStopPacket, player)
        }
    }

    private fun startTimer(howLong: Long, player: ServerPlayerEntity, settings: StartStopPacket) {
        mcCoroutineTask(howOften = howLong, period = 20.ticks) { task ->
            if (task.round == howLong) {
                mcCoroutineTask(delay = 20.ticks) {
                    start(player, settings)
                }
            }
        }
    }

    private fun start(player: ServerPlayerEntity, settings: StartStopPacket) {
        player.isSubwaySurfers = true
        player.coins = 0
        player.punishTicks = 0
        // reset powerups
        collectibles.filterIsInstance<Powerup>().forEach { player.dataTracker.set(it.endTimestampTracker, 0L) }
        player.rail = 1
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue =
            ServerConfig.config.surferBaseSpeed
        startStopPacketS2C.send(settings, player)
    }

    private fun CommandContext<ServerCommandSource>.extracted(
        isEnabled: Boolean = true, cameraDistanceArg: Double? = null, yawArg: Float? = null, pitchArg: Float? = null
    ) {
        handleStartGame(this.source.playerOrThrow, isEnabled, cameraDistanceArg, yawArg, pitchArg)
    }
}
