package net.perfectdreams.dreamloja.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.LongIdTable

object VoteSigns : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.tablePrefix}_votesigns"

    val owner = uuid("owner").index()
    val worldName = text("world_name")
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
}