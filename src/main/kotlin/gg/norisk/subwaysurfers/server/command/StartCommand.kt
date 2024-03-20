package gg.norisk.subwaysurfers.server.command

import com.mojang.brigadier.context.CommandContext
import gg.norisk.subwaysurfers.entity.UUIDMarker
import gg.norisk.subwaysurfers.network.s2c.*
import gg.norisk.subwaysurfers.server.ServerConfig
import gg.norisk.subwaysurfers.server.mechanics.PatternManager
import gg.norisk.subwaysurfers.server.mechanics.SpeedManager
import gg.norisk.subwaysurfers.subwaysurfers.*
import gg.norisk.subwaysurfers.worldgen.RailWorldManager
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.world.Heightmap
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import kotlin.math.roundToInt

object StartCommand {
    fun init() {
        command("subwaysurfers") {
            literal("pattern") {
                runs {
                }
            }
            literal("magnet") {
                runs {
                    this.source.playerOrThrow.isMagnetic = !this.source.playerOrThrow.isMagnetic
                    this.source.playerOrThrow.sendMessage("Is Active: ${this.source.playerOrThrow.isMagnetic}".literal)
                }
            }
            literal("jetpack") {
                runs {
                    this.source.playerOrThrow.hasJetpack = !this.source.playerOrThrow.hasJetpack
                    this.source.playerOrThrow.sendMessage("Is Active: ${this.source.playerOrThrow.hasJetpack}".literal)
                }
            }
            literal("flydebug") {
                runs {
                    this.source.playerOrThrow.isSubwaySurfers = !this.source.playerOrThrow.isSubwaySurfers
                    this.source.playerOrThrow.sendMessage("Is Active: ${this.source.playerOrThrow.isSubwaySurfers}".literal)
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
        val settings = VisualClientSettings()
        isEnabled.apply { settings.isEnabled = this }
        cameraDistanceArg?.apply { settings.desiredCameraDistance = this }
        yawArg?.apply { settings.yaw = this }
        pitchArg?.apply { settings.pitch = this }

        if (isEnabled) {
            settings.patternPacket = PatternPacket(
                PatternManager.currentPattern.left,
                PatternManager.currentPattern.middle,
                PatternManager.currentPattern.right
            )

            player.teleport(
                player.serverWorld,
                ServerConfig.config.startPos.x,
                ServerConfig.config.startPos.y,
                ServerConfig.config.startPos.z,
                0f,
                0f
            )
            player.isSubwaySurfers = true
            player.coins = 0
            player.punishTicks = 0
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue =
                SpeedManager.SURFER_BASE_SPEED
        } else {
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = SpeedManager.vanillaSpeed
            player.isSubwaySurfers = false
        }

        visualClientSettingsS2C.send(settings, player)

        player.rail = 1
    }

    private fun CommandContext<ServerCommandSource>.extracted(
        isEnabled: Boolean = true, cameraDistanceArg: Double? = null, yawArg: Float? = null, pitchArg: Float? = null
    ) {
        handleStartGame(this.source.playerOrThrow, isEnabled, cameraDistanceArg, yawArg, pitchArg)
    }
}
