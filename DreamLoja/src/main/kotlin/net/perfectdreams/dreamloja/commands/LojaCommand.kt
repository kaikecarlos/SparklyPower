package net.perfectdreams.dreamloja.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.blacklistedTeleport
import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import net.perfectdreams.dreamloja.tables.UserShopVotes
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class LojaCommand(val m: DreamLoja) : SparklyCommand(arrayOf("loja")) {
	@Subcommand
	fun root(player: Player) {
		m.openMenu(player)
	}

	@Subcommand
	fun getShop(player: Player, ownerName: String) {
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val user = transaction(Databases.databaseNetwork) {
				User.find { Users.username eq ownerName }.firstOrNull()
			}

			if (user == null) {
				player.sendMessage("${DreamLoja.PREFIX} §cUsuário não existe!")
				return@schedule
			}

			val shop = transaction(Databases.databaseNetwork) {
				Shop.find { (Shops.owner eq user.id.value) and (Shops.shopName eq "loja") }.firstOrNull()
			}

			if (shop == null) {
				player.sendMessage("${DreamLoja.PREFIX} §cUsuário não possui loja!")
				return@schedule
			}

			val votes = transaction(Databases.databaseNetwork) {
				UserShopVotes.select {
					UserShopVotes.receivedBy eq user.id.value
				}.count()
			}

			switchContext(SynchronizationContext.SYNC)

			val location = shop.getLocation()
			if (location.isUnsafe || location.blacklistedTeleport) {
				player.sendMessage("${DreamLoja.PREFIX} §cLoja do usuário não é segura!")
				return@schedule
			}

			player.teleport(location)

			val fancyName = Bukkit.getPlayer(user.id.value)?.displayName ?: user.username

			player.sendTitle("§bLoja d${MeninaAPI.getArtigo(user.id.value)} $fancyName", "§bVotos: §e$votes", 10, 100, 10)

			player.world.spawnParticle(Particle.VILLAGER_HAPPY, player.location.add(0.0, 0.5, 0.0), 25, 0.5, 0.5, 0.5)
		}
	}
}