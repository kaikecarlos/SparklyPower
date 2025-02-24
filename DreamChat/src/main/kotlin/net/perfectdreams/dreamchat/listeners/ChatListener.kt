package net.perfectdreams.dreamchat.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreamcasamentos.DreamCasamentos
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.dao.ChatUser
import net.perfectdreams.dreamchat.dao.DiscordAccount
import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamchat.tables.DiscordAccounts
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamchat.utils.DiscordAccountInfo
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.DreamUtils.jsonParser
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class ChatListener(val m: DreamChat) : Listener {
	val chatCooldownCache = Caffeine.newBuilder()
		.expireAfterWrite(1L, TimeUnit.MINUTES)
		.build<Player, Long>()
		.asMap()

	val lastMessageCache = Caffeine.newBuilder()
		.expireAfterAccess(1L, TimeUnit.MINUTES)
		.build<Player, String>()
		.asMap()

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction {
				val result = ChatUsers.select { ChatUsers.id eq e.player.uniqueId }.firstOrNull() ?: return@transaction
				val nickname = result[ChatUsers.nickname] ?: return@transaction

				e.player.displayName = nickname
				e.player.playerListName = nickname
			}
		}
	}

	@EventHandler
	fun onLeave(e: PlayerQuitEvent) {
		m.lockedTells.remove(e.player)
		chatCooldownCache.remove(e.player)
		lastMessageCache.remove(e.player)
	}

	@EventHandler
	fun onTag(e: ApplyPlayerTagsEvent) {
		if (e.player.uniqueId == m.eventoChat.lastWinner) {
			e.tags.add(
				PlayerTag(
					"§b§lD",
					"§b§lDatilógrafo",
					listOf(
						"§r§b${e.player.displayName}§r§7 ficou atento no chat e",
						"§7e preparad${e.player.artigo} no teclado para conseguir",
						"§7vencer o Evento Chat em primeiro lugar!"
					),
					null,
					false
				)
			)
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onChat(e: AsyncPlayerChatEvent) {
		// TODO: Remover
		m.loadResponses()

		e.isCancelled = true

		val lockedTellPlayer = m.lockedTells[e.player]
		if (lockedTellPlayer != null) {
			if (Bukkit.getPlayerExact(lockedTellPlayer) != null) {
				scheduler().schedule(m) {
					e.player.performCommand("tell $lockedTellPlayer ${e.message}")
				}
				return
			} else {
				e.player.sendMessage("§cO seu chat travado foi desativado devido á saida do player §b${lockedTellPlayer}§c")
				e.player.sendMessage("§cPor segurança, nós não enviamos a sua última mensagem, já que ela iria para o chat normal e não para a sua conversa privada")
				e.isCancelled = true
				m.lockedTells.remove(e.player)
				return
			}
		}

		val player = e.player
		val rawMessage = e.message
		var message = rawMessage

		if (m.eventoChat.running && m.eventoChat.event.process(e.player, message))
			m.eventoChat.finish(player)

		val lastMessageSentAt = chatCooldownCache.getOrDefault(player, 0)
		val diff = System.currentTimeMillis() - lastMessageSentAt

		if (500 >= diff) {
			player.sendMessage("§cEspere um pouco para enviar outra mensagem no chat!")
			return
		}

		if (3500 >= diff) {
			val lastMessageContent = lastMessageCache[player]
			if (lastMessageContent != null) {
				if (5 > StringUtils.getLevenshteinDistance(lastMessageContent, message)) {
					player.sendMessage("§cNão mande mensagens iguais ou similares a última que você mandou!")
					return
				}
			}
		}

		val upperCaseChars = e.message.toCharArray().filter { it.isUpperCase() }
		val upperCasePercent = (e.message.length * upperCaseChars.size) / 100

		if (e.message.length >= 20 && upperCasePercent > 55) {
			player.sendMessage("§cEvite usar tanto CAPS LOCK em suas mensagens! Isso polui o chat!")
			e.message = e.message.toLowerCase()
		}

		chatCooldownCache[player] = System.currentTimeMillis()
		lastMessageCache[player] = e.message.toLowerCase()

		// Vamos verificar se o cara só está falando o nome do cara da Staff
		for (onlinePlayers in Bukkit.getOnlinePlayers()) {
			if (onlinePlayers.hasPermission("sparklypower.soustaff")) {
				if (message.equals(onlinePlayers.name, true)) {
					player.sendMessage("§cSe você quiser chamar alguém da Staff, por favor, coloque a pergunta JUNTO com a mensagem, obrigado! ^-^")
					return
				}
			}
		}

		message = message.translateColorCodes()

		if (!player.hasPermission("dreamchat.chatcolors")) {
			message = message.replace(DreamChat.CHAT_REGEX, "")
		}

		if (!player.hasPermission("dreamchat.chatformatting")) {
			message = message.replace(DreamChat.FORMATTING_REGEX, "")
		}

		if (ChatUtils.isMensagemPolemica(message)) {
			DreamNetwork.PANTUFA.sendMessageAsync(
				"387632163106848769",
				"**`" + player.name.replace("_", "\\_") + "` escreveu uma mensagem potencialmente polêmica no chat!**\n```" + message + "```\n"
			)
		}

		if (message.startsWith("./") || message.startsWith("-/")) { // Caso o player esteja dando um exemplo de um comando, por exemplo, "./survival"
			message = message.removePrefix(".")
		}

		message = ChatUtils.beautifyMessage(player, message)

		// Hora de "montar" a mensagem
		val textComponent = TextComponent()

		val playOneMinute = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

		var prefix = VaultUtils.chat.getPlayerPrefix(player)

		val api = LuckPermsProvider.get()

		val luckyUser = api.userManager.getUser(e.player.uniqueId)

		val chatUser = transaction(Databases.databaseNetwork) {
			ChatUser.find {
				ChatUsers.id eq e.player.uniqueId
			}.firstOrNull()
		}

		if ((luckyUser?.primaryGroup ?: "default") == "default" && (7200 * 20) > playOneMinute) {
			prefix = if (e.player.girl) {
				"§eNovata"
			} else {
				"§eNovato"
			}
		}

		if ((luckyUser?.primaryGroup ?: "default") == "default" && m.partners.contains(e.player.uniqueId)) {
			prefix = if (e.player.girl) {
				"§5§lParceira"
			} else {
				"§5§lParceiro"
			}
		}

		if ((luckyUser?.primaryGroup ?: "default") == "default" && m.artists.contains(e.player.uniqueId)) {
			prefix = "§5§lDesenhista"
		}

		if (chatUser != null) {
			if (chatUser.nickname != null && !e.player.hasPermission("dreamchat.nick")) {
				transaction(Databases.databaseNetwork) {
					chatUser.nickname = null
				}
				e.player.displayName = null
				e.player.playerListName = null
			}

			if (chatUser.tag != null && !e.player.hasPermission("dreamchat.querotag")) {
				transaction(Databases.databaseNetwork) {
					chatUser.tag = null
				}
			}

			if (chatUser.tag != null) {
				prefix = chatUser.tag
			}
		}

		textComponent += "§8[${prefix.translateColorCodes()}§8] ".toTextComponent().apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "kk eae men".toBaseComponent())
		}

		val tags = TextComponent()
		tags += "§8[".toTextComponent()

		val event = ApplyPlayerTagsEvent(player, mutableListOf())
		Bukkit.getPluginManager().callEvent(event)

		if (m.topEntries[0].equals(e.player.name, true)) {
			event.tags.add(
				PlayerTag(
					"§2§lO",
					"§2§lOstentador${if (player.girl) "a" else ""}",
					listOf(
						"§r§b${player.displayName}§r§7 é a pessoa mais ostentadora do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		if (m.topEntries[1].equals(e.player.name, true)) {
			event.tags.add(
				PlayerTag(
					"§2§lO",
					"§2§lMagnata",
					listOf(
						"§r§b${player.displayName}§r§7 é a segunda pessoa mais rica do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		if (m.topEntries[2].equals(e.player.name, true)) {
			event.tags.add(
				PlayerTag(
					"§2§lO",
					"§2§lRic${(player.artigo)}",
					listOf(
						"§r§b${player.displayName}§r§7 é a terceira pessoa mais rica do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		if (event.tags.isNotEmpty()) {
			// omg tags!
			for (tag in event.tags.filter { it.expanded }) {
				val textTag = "§8[${tag.tagName}§8] ".toTextComponent().apply {
					if (tag.description != null) {
						hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "§6✪ §f${tag.tagName} §6✪\n§7${tag.description.joinToString("\n§7")}".toBaseComponent())
					}
					if (tag.suggestCommand != null) {
						clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tag.suggestCommand)
					}
				}
				textComponent += textTag
			}

			for (tag in event.tags.filter { !it.expanded }) {
				tags += tag.small.toTextComponent().apply {
					if (tag.description != null) {
						hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "§6✪ §f${tag.tagName} §6✪\n§7${tag.description.joinToString("\n§7")}".toBaseComponent())
					}
					if (tag.suggestCommand != null) {
						clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tag.suggestCommand)
					}
				}
			}

			tags += "§8] "
			if (event.tags.count { !it.expanded } != 0) {
				textComponent += tags
			}
		}

		/* val panela = DreamPanelinha.INSTANCE.getPanelaByMember(player)
		if (panela != null) {
			val tag = "§8«§3${panela.tag}§8» ".toTextComponent()
			tag.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
					"""${panela.name}
						|§eDono: §b${panela.owner}
						|§eMembros: §6${panela.members.size}
						|§eKDR: §6${panela.calculateKDR()}
					""".trimMargin().toBaseComponent())
			textComponent += tag
		} */

		val casal = DreamCasamentos.INSTANCE.getMarriageFor(player)
		if (casal != null) {
			val heart = "§4❤ ".toTextComponent()
			val offlinePlayer1 = Bukkit.getOfflinePlayer(casal.player1)
			val offlinePlayer2 = Bukkit.getOfflinePlayer(casal.player2)
			heart.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "§4❤ §d§l${DreamCasamentos.INSTANCE.getShipName(offlinePlayer1?.name ?: "???", offlinePlayer2?.name ?: "???")} §4❤\n\n§6Casado com: §b${Bukkit.getOfflinePlayer(casal.getPartnerOf(player))?.name ?: "???"}".toBaseComponent())
			textComponent += heart
		}

		textComponent += TextComponent(*"§7${player.displayName}".translateColorCodes().toBaseComponent()).apply {
			clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "${player.name} ")
			var toDisplay = player.displayName

			if (!player.displayName.stripColorCode().contains(player.name)) {
				toDisplay = player.displayName + " §a(§b${player.name}§a)§r"
			}

			val input = playOneMinute / 20
			val numberOfDays = input / 86400
			val numberOfHours = input % 86400 / 3600
			val numberOfMinutes = input % 86400 % 3600 / 60

			val rpStatus = if (player.resourcePackStatus == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
				"§a✔"
			} else {
				"§c✗"
			}

			val aboutLines = mutableListOf(
				"§6✪ §a§lSobre ${player.artigo} §r§b${toDisplay}§r §6✪",
				"",
				"§eGênero: §d${if (!player.girl) { "§3♂" } else { "§d♀" }}",
				"§eGrana: §6${player.balance} Sonhos",
				"§eKDR: §6PvP é para os fracos, 2bj :3",
				"§eOnline no SparklyPower Survival por §6$numberOfDays dias§e, §6$numberOfHours horas §ee §6$numberOfMinutes minutos§e!",
				"§eVersão: §6Minecraft ${player.version.getName()}",
				"§eUsando a Resource Pack? $rpStatus"
			)

			val discordAccount = transaction(Databases.databaseNetwork) {
				DiscordAccount.find { DiscordAccounts.minecraftId eq player.uniqueId }.firstOrNull()
			}

			if (discordAccount != null) {
				val cachedDiscordAccount = m.cachedDiscordAccounts.getOrPut(discordAccount.discordId, {
					val request = HttpRequest.get("https://discordapp.com/api/v6/users/${discordAccount.discordId}")
						.userAgent("SparklyPower DreamChat")
						.header("Authorization", "Bot ${m.config.getString("pantufa-token")}")

					val statusCode = request.code()
					if (statusCode != 200)
						Optional.empty()
					else {
						val json = jsonParser.parse(request.body())

						Optional.of(
							DiscordAccountInfo(
								json["username"].string,
								json["discriminator"].string
							)
						)
					}
				})

				if (cachedDiscordAccount.isPresent) {
					val info = cachedDiscordAccount.get()
					aboutLines.add("§eDiscord: §6${info.name}§8#§6${info.discriminator} §8(§7${discordAccount.discordId}§8)")
				}
			}

			val adoption = DreamCasamentos.INSTANCE.getParentsOf(player)

			if (adoption != null) {
				aboutLines.add("")
				aboutLines.add("§eParentes: §b${Bukkit.getOfflinePlayer(adoption.player1)?.name ?: "???"} §b${Bukkit.getOfflinePlayer(adoption.player2)?.name ?: "???"}")
			}
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
				aboutLines.joinToString("\n").toBaseComponent()
			)
		}

		textComponent += " §6➤ ".toBaseComponent()

		val split = message.split("(?=\\b[ ])")
		var previous: String? = null
		for (piece in split) {
			var editedPiece = piece
			if (previous != null) {
				editedPiece = "$previous$editedPiece"
			}
			textComponent += editedPiece.toBaseComponent()
			previous = ChatColor.getLastColors(piece)
		}

		if (DreamChat.mutedUsers.contains(player.name)) { // Usuário está silenciado
			player.spigot().sendMessage(textComponent)

			for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission("pocketdreams.soustaff")}) {
				staff.sendMessage("§8[§cSILENCIADO§8] §b${player.name}§c: $message")
			}
			return
		}

		for (onlinePlayer in Bukkit.getOnlinePlayers()) {
			// Verificar se o player está ignorando o player que enviou a mensagem
			val isIgnoringTheSender = m.userData.getStringList("ignore.${onlinePlayer.uniqueId}").contains(player.uniqueId.toString())

			if (!isIgnoringTheSender)
				onlinePlayer.spigot().sendMessage(textComponent)
		}

		val calendar = Calendar.getInstance()
		m.chatLog.appendText("[${String.format("%02d", calendar[Calendar.DAY_OF_MONTH])}/${String.format("%02d", calendar[Calendar.MONTH] + 1)}/${String.format("%02d", calendar[Calendar.YEAR])} ${String.format("%02d", calendar[Calendar.HOUR_OF_DAY])}:${String.format("%02d", calendar[Calendar.MINUTE])}] ${player.name}: $message\n")

		// Tudo OK? Então vamos verificar se a mensagem tem algo de importante para nós respondermos
		for (response in DreamChat.botResponses) {
			if (response.handleResponse(message, e)) {
				val response = response.getResponse(message, e) ?: return
				ChatUtils.sendResponseAsBot(player, response)
				return
			}
		}

		// Vamos mandar no Biscord!
		DreamChat.CHAT_WEBHOOK.send(
			DiscordMessage(
				username = player.name,
				content = message.stripColorCode().replace(Regex("\\\\+@"), "@").replace("@", "@\u200B"),
				avatar = "https://sparklypower.net/api/v1/render/avatar?name=${player.name}&scale=16"
			)
		)
	}
}