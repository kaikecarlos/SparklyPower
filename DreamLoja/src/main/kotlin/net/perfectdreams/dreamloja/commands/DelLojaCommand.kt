package net.perfectdreams.dreamloja.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DelLojaCommand(val m: DreamLoja) : SparklyCommand(arrayOf("delloja")) {
	@Subcommand
	fun root(player: Player) {
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction(Databases.databaseNetwork) {
				Shops.deleteWhere {
					(Shops.owner eq player.uniqueId) and (Shops.shopName eq "loja")
				}
			}

			switchContext(SynchronizationContext.SYNC)

			player.sendMessage("${DreamLoja.PREFIX} §aSua loja foi deletada com sucesso!")
		}
	}
}