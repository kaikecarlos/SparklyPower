package net.perfectdreams.dreamtrails.utils

import net.perfectdreams.dreamtrails.trails.TrailData
import org.bukkit.Material
import org.bukkit.Particle

object Trails {
    val _trails = listOf(
        TrailData(
            "§cto pistola grrr",
            Material.EMERALD,
            Particle.VILLAGER_ANGRY,
            offsetX = 0.0,
            offsetY = 0.0,
            offsetZ = 0.0,
            locationOffsetY = 1.5,
            locationDirectionOffset = 0.0,
            cooldown = 250
        ),

        TrailData(
            "§cCorações, mostrando que você é iludido por todos.",
            Material.POPPY,
            Particle.HEART,
            offsetX = 0.0,
            offsetY = 0.0,
            offsetZ = 0.0,
            locationOffsetY = 2.0,
            locationDirectionOffset = 0.0,
            cooldown = 250
        ),

        TrailData(
            "§fSoltando vapor pelo fiofo",
            Material.COBWEB,
            Particle.CLOUD,
            count = 0,
            offsetX = 0.0,
            offsetY = 0.2,
            offsetZ = 0.0
        ),

        // ===[ OLD ]===
        TrailData(
            "§7Fumaça pelo fiofo",
            Material.CAMPFIRE,
            Particle.CAMPFIRE_COSY_SMOKE,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§fEnd Rod",
            Material.END_ROD,
            Particle.END_ROD,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§7Estrelinhas",
            Material.FIREWORK_ROCKET,
            Particle.FIREWORKS_SPARK,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§1Efeitos de Ender Dragon",
            Material.DRAGON_HEAD,
            Particle.DRAGON_BREATH,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§cFoguinho",
            Material.LAVA_BUCKET,
            Particle.FLAME,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§bEfeito Azulzinho",
            Material.TRADER_LLAMA_SPAWN_EGG,
            Particle.SNEEZE,
            0,
            0.0,
            0.1,
            0.0
        ),
        TrailData(
            "§aTotem",
            Material.TOTEM_OF_UNDYING,
            Particle.TOTEM,
            0,
            0.0,
            0.1,
            0.0
        ),

        TrailData(
            "§aComposters",
            Material.COMPOSTER,
            Particle.COMPOSTER
        ),
        TrailData(
            "§fEfeito de Dano Crítico",
            Material.IRON_SWORD,
            Particle.CRIT
        ),
        TrailData(
            "§fEfeito de Dano Crítico com §nmagia§f, wow!",
            Material.DIAMOND_SWORD,
            Particle.CRIT_MAGIC
        ),
        TrailData(
            "§fDamage Indicator",
            Material.STONE_SWORD,
            Particle.DAMAGE_INDICATOR
        ),
        TrailData(
            "§7Coisas de Golfinhos",
            Material.DOLPHIN_SPAWN_EGG,
            Particle.DOLPHIN
        ),
        TrailData(
            "§bÁgua",
            Material.WATER_BUCKET,
            Particle.FALLING_WATER
        ),
        TrailData(
            "§bÁgua²",
            Material.WATER_BUCKET,
            Particle.WATER_SPLASH
        ),
        TrailData(
            "§cLava",
            Material.LAVA_BUCKET,
            Particle.FALLING_LAVA
        ),
        TrailData(
            "§cLava²",
            Material.LAVA_BUCKET,
            Particle.LAVA
        ),
        TrailData(
            "§eMel",
            Material.HONEY_BLOCK,
            Particle.FALLING_HONEY
        ),
        TrailData(
            "§6Nectar",
            Material.HONEYCOMB_BLOCK,
            Particle.FALLING_NECTAR
        ),
        TrailData(
            "§7Mesa de Encantamento",
            Material.ENCHANTING_TABLE,
            Particle.ENCHANTMENT_TABLE
        ),
        TrailData(
            "§1Nautilus",
            Material.NAUTILUS_SHELL,
            Particle.NAUTILUS
        ),
        TrailData(
            "§dNotas Musicais",
            Material.NOTE_BLOCK,
            Particle.NOTE
        ),
        TrailData(
            "§5Portal do Nether",
            Material.OBSIDIAN,
            Particle.PORTAL
        ),
        TrailData(
            "§aSlime",
            Material.SLIME_BLOCK,
            Particle.SLIME
        ),
        TrailData(
            "§fNeve",
            Material.SNOW_BLOCK,
            Particle.SNOW_SHOVEL
        ),
        TrailData(
            "§cMagia",
            Material.EXPERIENCE_BOTTLE,
            Particle.SPELL
        ),
        TrailData(
            "§aMagia²",
            Material.EXPERIENCE_BOTTLE,
            Particle.SPELL_INSTANT
        ),
        TrailData(
            "§aMagia³",
            Material.EXPERIENCE_BOTTLE,
            Particle.SPELL_MOB
        ),
        TrailData(
            "§aMagia³+¹",
            Material.EXPERIENCE_BOTTLE,
            Particle.SPELL_MOB_AMBIENT
        ),
        TrailData(
            "§aMagia³+2",
            Material.EXPERIENCE_BOTTLE,
            Particle.SPELL_WITCH
        ),
        TrailData(
            "§cTown Aura",
            Material.JUNGLE_WOOD,
            Particle.TOWN_AURA
        )
    )

    val trails = _trails.associateBy { it.particle }
}