package net.perfectdreams.dreamchat.tables

import org.jetbrains.exposed.dao.LongIdTable

object DiscordAccounts : LongIdTable() {
    val minecraftId = uuid("minecraft_id").index()
    val discordId = long("discord_id").index()
    val isConnected = bool("is_connected").index()
}