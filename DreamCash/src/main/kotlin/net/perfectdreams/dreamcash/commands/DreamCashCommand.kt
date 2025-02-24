package net.perfectdreams.dreamcash.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.dao.CashInfo
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("pesadelos", "dreamcash", "cash")) {
    @Subcommand
    fun root(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(sender.uniqueId)
            }

            val cash = cashInfo?.cash ?: 0

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §eVocê tem §c${cash} pesadelos§e! Que tal gastar na nossa loja? §6/lojacash")
        }
    }

    @Subcommand
    fun checkPlayerCash(sender: CommandSender, name: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()))
            }

            val cash = cashInfo?.cash ?: 0

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §b${name}§e tem §c$cash pesadelos§e!")
        }
    }

    @Subcommand(["pagar", "pay"])
    fun payPlayerCash(sender: Player, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var yourCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(sender.uniqueId)
            }

            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            yourCashInfo = yourCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(sender.uniqueId) {
                    this.cash = 0
                }
            }

            if (howMuch > yourCashInfo.cash) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cVocê não tem tantos pesadelos!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray())) {
                    this.cash = 0
                }
            }

            transaction(Databases.databaseNetwork) {
                yourCashInfo.cash -= howMuch
                receiverCashInfo.cash += howMuch
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você pagou §c${howMuch} pesadelos§a para §b${name}§a!")
        }
    }

    @Subcommand(["give"])
    @SubcommandPermission("dreamcash.give")
    fun givePlayerCash(sender: CommandSender, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray())) {
                    this.cash = 0
                }
            }

            transaction(Databases.databaseNetwork) {
                receiverCashInfo.cash += howMuch
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você deu §c${howMuch} pesadelos§a para §b${name}§a!")
        }
    }

    @Subcommand(["set"])
    @SubcommandPermission("dreamcash.set")
    fun setPlayerCash(sender: CommandSender, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray())) {
                    this.cash = 0
                }
            }

            transaction(Databases.databaseNetwork) {
                receiverCashInfo.cash = howMuch.toLong()
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você setou §c${howMuch} pesadelos§a para §b${name}§a!")
        }
    }
}