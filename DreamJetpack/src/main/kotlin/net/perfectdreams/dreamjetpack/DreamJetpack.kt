package net.perfectdreams.dreamjetpack

import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.hasStoredMetadataWithKey
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DreamJetpack : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamJetpack
		val PREFIX = "§8[§a§lJetpack§8]§e"
		const val TAKE_DAMAGE_EVERY = 180
	}

	val flyingPlayers = mutableSetOf<Player>()
	val bossBars = mutableMapOf<Player, BossBar>()

	val jetpackType = Material.CHAINMAIL_CHESTPLATE
	var durabilityTicks = 0

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		registerCommand(object: SparklyCommand(arrayOf("dreamjetpack"), permission = "dreamjetpack.setup") {
			@Subcommand
			fun root(sender: CommandSender) {
				reloadConfig()
				sender.sendMessage("§aReload concluído!")
			}

			@Subcommand(["give"])
			fun root(sender: Player) {
				sender.inventory.addItem(
					ItemStack(Material.CHAINMAIL_CHESTPLATE)
						.rename("§6§lJetpack")
						.storeMetadata("isJetpack", "true")
				)
				sender.sendMessage("§aVocê recebeu uma Jetpack!")
			}
		})

		scheduler().schedule(this) {
			while (true) {
				val toBeRemoved = mutableSetOf<Player>()

				for (player in flyingPlayers) {
					if (!player.isValid) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.allowFlight = false
						continue
					}

					val chestplate = player.inventory.chestplate

					if (chestplate?.type != jetpackType) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.sendMessage("$PREFIX §cVocê não está mais usando a Jetpack!")
						player.allowFlight = false
						continue
					}

					val blacklistedWorlds = config.getStringList("blacklisted-worlds")

					if (blacklistedWorlds.contains(player.world.name)) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.sendMessage("$PREFIX §cVocê não pode voar aqui")
						player.allowFlight = false
						continue
					}

					if (!player.isFlying)
						continue

					if (chestplate.containsEnchantment(Enchantment.MENDING))
						chestplate.removeEnchantment(Enchantment.MENDING)

					val meta = chestplate.itemMeta as org.bukkit.inventory.meta.Damageable

					val applyDamage = when {
						player.hasPermission("dreamjetpack.vip++") -> 0
						player.hasPermission("dreamjetpack.vip+") -> 1
						player.hasPermission("dreamjetpack.vip") -> 2
						else -> 3
					}

					if (durabilityTicks % TAKE_DAMAGE_EVERY == 0) {
						meta.damage += applyDamage
					}

					if (meta.damage > 240) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.inventory.chestplate = ItemStack(Material.AIR)
						player.sendMessage("$PREFIX §cSua Jetpack está toda detonada e explodiu!")
						player.allowFlight = false
						continue
					}

					if (!bossBars.contains(player)) {
						val bossBar = Bukkit.createBossBar("...", BarColor.GREEN, BarStyle.SOLID)
						bossBars[player] = bossBar
						bossBar.addPlayer(player)
					}

					val bossBar = bossBars[player]
					if (bossBar != null) {
						val percentageRemaining = (240 - meta.damage).toDouble() / 240
						bossBar.progress = percentageRemaining

						bossBar.color = when {
							player.hasPermission("dreamjetpack.vip++") -> BarColor.PINK
							percentageRemaining >= 0.75 -> BarColor.GREEN
							percentageRemaining >= 0.25 -> BarColor.YELLOW
							else -> BarColor.RED
						}

						val timeRemaining = if (applyDamage == 0)
							-1
						else ((TAKE_DAMAGE_EVERY * (240 - meta.damage) / applyDamage) - durabilityTicks % TAKE_DAMAGE_EVERY)

						val minutes = timeRemaining / 60
						val seconds = timeRemaining % 60

						if (applyDamage == 0) {
							bossBar.title = "§dWoosh! §aVocê pode voar por mais §e∞§a!"
						} else {
							bossBar.title = "§dWoosh! §aVocê pode voar por mais §e$minutes minutos e $seconds segundos§a!"
						}
					}

					player.world.spawnParticle(Particle.SMOKE_NORMAL, player.location, 20, 1.0, 1.0, 1.0)

					chestplate.itemMeta = meta as ItemMeta
				}

				flyingPlayers.removeAll(toBeRemoved)

				durabilityTicks++

				waitFor(20)
			}
		}

		registerEvents(this)
	}

	@EventHandler
	fun onShift(e: PlayerToggleSneakEvent) {
		val chestplate = e.player.inventory.chestplate
		if (e.player.isOnGround && e.isSneaking && chestplate?.type == jetpackType) {
			var isJetpack = chestplate.hasStoredMetadataWithKey("isJetpack")

			if (!isJetpack && chestplate.itemMeta.displayName == "§6§lJetpack") {
				e.player.inventory.chestplate = chestplate.storeMetadata("isJetpack", "true")
				isJetpack = true
			}

			if (!isJetpack) {
				e.player.sendMessage("$PREFIX §cQue isso, comprou essa Jetpack no barzinho da esquina? Essa Jetpack é um risco a sua vida! Você apenas pode voar com jetpacks compradas na §6/loja§c!")
			} else {
				if (!e.player.allowFlight) {
					val blacklistedWorlds = config.getStringList("blacklisted-worlds")

					if (blacklistedWorlds.contains(e.player.world.name)) {
						e.player.sendMessage("$PREFIX §cVocê não pode voar aqui")
						return
					}

					e.player.allowFlight = true

					flyingPlayers.add(e.player)

					e.player.sendMessage("$PREFIX §eWoosh! Sua Jetpack foi ativada e está pronta para uso!")
				} else {
					e.player.allowFlight = false

					flyingPlayers.remove(e.player)
					bossBars[e.player]?.removeAll()
					bossBars.remove(e.player)

					e.player.sendMessage("$PREFIX §eSua Jetpack foi desativada!")
				}
			}
		}
	}
}