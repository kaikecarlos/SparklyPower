package net.perfectdreams.dreamcash.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.node.types.PermissionNode
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.dao.CashInfo
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class LojaCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("lojacash", "cashloja")) {
    @Subcommand
    fun root(sender: Player) {
        showShopMenu(sender)
    }

    fun showShopMenu(sender: Player) {
        val menu = createMenu(45, "§a§lA Loja de seus §c§lPesadelos") {
            fun generateItemAt(x: Int, y: Int, type: Material, name: String, quantity: Long, callback: () -> (Unit)) {
                slot(x, y) {
                    item = ItemStack(type)
                        .rename(name)
                        .lore(
                            "§c$quantity pesadelos"
                        )

                    onClick {
                        checkIfPlayerHasSufficientMoney(sender, quantity) {
                            askForConfirmation(sender) {
                                sender.closeInventory()

                                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                    transaction(Databases.databaseNetwork) {
                                        try {
                                            Cash.takeCash(sender, quantity)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                            return@transaction
                                        }
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    callback.invoke()
                                    sender.sendMessage("§aObrigado pela compra! ^-^")

                                    Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou $name§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                }
                            }
                        }
                    }
                }
            }

            // VIPs
            generateItemAt(0, 0, Material.IRON_INGOT, "§b§lVIP §7(um mês • R$ 14,99)", 500) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip 32d")
            }
            generateItemAt(1, 0, Material.GOLD_INGOT, "§b§lVIP§e+ §7(um mês • R$ 29,99)", 1000) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip+ 32d")
            }
            generateItemAt(2, 0, Material.DIAMOND, "§b§lVIP§e++ §7(um mês • R$ 44,99)", 1500) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip++ 32d")
            }

            // Blocos de Proteção
            generateItemAt(0, 1, Material.DIRT, "§e8000 blocos de proteção", 15) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 8000")
            }
            generateItemAt(1, 1, Material.MYCELIUM, "§e16000 blocos de proteção", 30) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 16000")
            }
            generateItemAt(2, 1, Material.GRASS, "§e24000 blocos de proteção", 45) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 24000")
            }

            // Money
            generateItemAt(0, 2, Material.SIGN, "§a234375 Sonhos", 265) {
                sender.balance += 234375
            }
            generateItemAt(1, 2, Material.SIGN, "§a468750 Sonhos", 530) {
                sender.balance += 468750
            }
            generateItemAt(2, 2, Material.SIGN, "§a937500 Sonhos", 1045) {
                sender.balance += 937500
            }
            generateItemAt(3, 2, Material.SIGN, "§a1875000 Sonhos", 2090) {
                sender.balance += 1875000
            }
            generateItemAt(4, 2, Material.SIGN, "§a3750000 Sonhos", 4165) {
                sender.balance += 3750000
            }
            generateItemAt(5, 2, Material.SIGN, "§a7500000 Sonhos", 8325) {
                sender.balance += 7500000
            }
            generateItemAt(6, 2, Material.SIGN, "§a15000000 Sonhos", 16650) {
                sender.balance += 15000000
            }
            generateItemAt(7, 2, Material.SIGN, "§a30000000 Sonhos", 33300) {
                sender.balance += 30000000
            }
            generateItemAt(8, 2, Material.SIGN, "§a60000000 Sonhos", 66600) {
                sender.balance += 60000000
            }

            // Coisas Reais
            this.slot(0, 3) {
                item = ItemStack(Material.OAK_BOAT)
                    .rename("§aDiscord Nitro Classic")
                    .lore(
                        "§c1330 pesadelos"
                    )

                onClick {
                    it.closeInventory()

                    sender.sendMessage("§aPara comprar, mande uma mensagem para MrPowerGamerBR#4185 no Discord!")
                }
            }
            this.slot(1, 3) {
                item = ItemStack(Material.DARK_OAK_BOAT)
                    .rename("§aDiscord Nitro")
                    .lore(
                        "§c2660 pesadelos"
                    )

                onClick {
                    it.closeInventory()

                    sender.sendMessage("§aPara comprar, mande uma mensagem para MrPowerGamerBR#4185 no Discord!")
                }
            }

            // Special stuff
            this.slot(7, 4) {
                item = ItemStack(Material.NETHER_STAR)
                    .rename("§aComo conseguir pesadelos?")

                onClick {
                    it.closeInventory()

                    sender.sendMessage("§aExistem vários jeitos de conseguir §cpesadelos§a!")
                    sender.sendMessage("§8• §eNa nossa loja§b https://sparklypower.net/loja")
                    sender.sendMessage("§8• §eVotando no servidor §6/votar")
                    sender.sendMessage("§8• §eVencendo eventos no servidor")
                }
            }

            this.slot(8, 4) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lFechar menu")

                onClick {
                    it.closeInventory()
                }
            }

            this.slot(6,  4) {
                item = ItemStack(Material.BEACON)
                    .rename("§e§lInformações sobre o meu VIP ativo")

                onClick {
                    it.closeInventory()

                    val api = LuckPermsProvider.get()
                    val user = api.userManager.getUser(sender.uniqueId) ?: return@onClick
                    val vipPlusPlusPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip++" }
                    val vipPlusPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip+" }
                    val vipPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip" }

                    val time = vipPlusPlusPermission?.expiry ?: vipPlusPermission?.expiry ?: vipPermission?.expiry

                    if (time == null) {
                        sender.sendMessage("§cVocê não tem nenhum VIP ativo!")
                    } else {
                        sender.sendMessage("§eSeu VIP irá expirar em §6${DateUtils.formatDateDiff(time.toEpochMilli())}")
                    }
                }
            }
        }

        menu.sendTo(sender)
    }

    fun checkIfPlayerHasSufficientMoney(sender: Player, quantity: Long, callback: () -> (Unit)) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cash = transaction(Databases.databaseNetwork) {
                Cash.getCash(sender)
            }

            switchContext(SynchronizationContext.SYNC)

            if (quantity > cash) {
                sender.closeInventory()
                sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                return@schedule
            }

            callback.invoke()
        }
    }

    fun askForConfirmation(sender: Player, afterAccept: () -> (Unit)) {
        val menu = createMenu(9, "§a§lConfirme a sua compra!") {
            slot(3, 0) {
                item = ItemStack(Material.GREEN_WOOL)
                    .rename("§a§lQuero comprar!")

                onClick {
                    afterAccept.invoke()
                }
            }
            slot(5, 0) {
                item = ItemStack(Material.RED_WOOL)
                    .rename("§c§lTalvez outro dia...")

                onClick {
                    sender.closeInventory()
                    showShopMenu(sender)
                }
            }
        }

        menu.sendTo(sender)
    }
}