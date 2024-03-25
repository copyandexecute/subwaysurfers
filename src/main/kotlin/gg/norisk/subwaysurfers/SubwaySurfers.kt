package gg.norisk.subwaysurfers

import gg.norisk.subwaysurfers.client.ClientSettings
import gg.norisk.subwaysurfers.client.hud.GameOverScreen
import gg.norisk.subwaysurfers.client.hud.InGameHud
import gg.norisk.subwaysurfers.client.input.KeyboardInput
import gg.norisk.subwaysurfers.client.lifecycle.ClientGamePreStartLifeCycle
import gg.norisk.subwaysurfers.client.lifecycle.ClientGameRunningLifeCycle
import gg.norisk.subwaysurfers.client.listener.ClientAnimationListener
import gg.norisk.subwaysurfers.client.listener.GameOverListener
import gg.norisk.subwaysurfers.client.mechanics.ClientCollisionManager
import gg.norisk.subwaysurfers.client.renderer.ShaderManager
import gg.norisk.subwaysurfers.common.collectible.Collectibles
import gg.norisk.subwaysurfers.registry.*
import gg.norisk.subwaysurfers.server.ServerConfig
import gg.norisk.subwaysurfers.server.command.StartCommand
import gg.norisk.subwaysurfers.server.listener.BasicListener
import gg.norisk.subwaysurfers.server.listener.MovementInputListener
import gg.norisk.subwaysurfers.server.listener.ScreenListener
import gg.norisk.subwaysurfers.server.mechanics.PatternManager
import gg.norisk.subwaysurfers.server.mechanics.PunishManager
import gg.norisk.subwaysurfers.server.mechanics.SpeedManager
import gg.norisk.subwaysurfers.worldgen.StructureManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.Identifier
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import org.slf4j.LoggerFactory
import kotlin.random.Random

object SubwaySurfers : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    override fun onInitialize() {
        EntityRegistry.registerEntityAttributes()
        SoundRegistry.init()
        BlockRegistry.init()
        ItemRegistry.init()
        NetworkRegistry.init()
        serverDevCommands()

        if (FabricLoader.getInstance().isDevelopmentEnvironment || FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            StartCommand.init()
            SpeedManager.init()
            PunishManager.init()
            PatternManager.init()
            ServerConfig.init()
            BasicListener.init()
            ScreenListener.init()
            MovementInputListener.init()
        }

        Collectibles.register() // register entities, items etc. for surfer items
    }

    override fun onInitializeClient() {
        EntityRendererRegistry.init()
        ClientCollisionManager.init()
        ClientSettings.init()
        KeyboardInput.init()
        InGameHud.init()
        BlockRendererRegistry.init()
        ClientAnimationListener.init()
        GameOverListener.init()
        ShaderManager.init()
        StructureManager.initClient()
        ClientGamePreStartLifeCycle.init()
        ClientGameRunningLifeCycle.init()
        Collectibles.registerClient()
        devCommands()
    }

    fun String.toId() = Identifier("subwaysurfers", this)
    val noriskSkin = "textures/norisk_skin.png".toId()
    val policeSkin = "textures/policeman.png".toId()
    val logger = LoggerFactory.getLogger("subwaysurfers")

    private fun serverDevCommands() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("head") {
                runs {
                    val player = this.source.playerOrThrow
                    player.equipStack(EquipmentSlot.HEAD, player.mainHandStack)
                }
            }
            command("reloadchunks") {
                runs {
                    val player = this.source.playerOrThrow
                    player.serverWorld.onPlayerRespawned(player)
                }
            }
        }
    }

    private fun devCommands() {
        clientCommand("curvedshader") {
            runs {
                ShaderManager.loadCurvedShader()
            }
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            clientCommand("testscreen") {
                literal("gameover") {
                    runs {
                        mcCoroutineTask(delay = 1.ticks, client = true) {
                            MinecraftClient.getInstance().setScreen(GameOverScreen(Random.nextInt(), Random.nextInt()))
                        }
                    }
                }
            }
        }
    }

    override fun onInitializeServer() {
    }
}
