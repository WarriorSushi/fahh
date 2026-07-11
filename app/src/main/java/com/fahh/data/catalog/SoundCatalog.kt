package com.fahh.data.catalog

import com.fahh.R
import com.fahh.data.model.Sound

/**
 * The bundled catalog is code-owned metadata. User unlock state lives in Room and is never
 * replaced when this list changes.
 */
object SoundCatalog {
    const val VERSION = 2
    private const val ORIGINAL_SOUND_COUNT = 12

    val sounds: List<Sound> = listOf(
        Sound("Fahh", R.raw.fahh, "F", isLocked = false, packName = "Free", id = "fahh"),
        Sound("Bruh", R.raw.bruh, "B", isLocked = false, packName = "Free", id = "bruh"),
        Sound("Vine Boom", R.raw.vine_boom, "V", isLocked = false, packName = "Free", id = "vine_boom"),
        Sound("Wow", R.raw.wow, "W", isLocked = false, packName = "Free", id = "wow"),
        Sound("Air Horn", R.raw.air_horn, "A", isLocked = true, packName = "Chaos", id = "air_horn"),
        Sound("Dun Dunnn", R.raw.dun_dun_dunn, "D", isLocked = true, packName = "Reaction", id = "dun_dunnn"),
        Sound("Oh My God", R.raw.oh_my_god_wow, "O", isLocked = true, packName = "Reaction", id = "oh_my_god"),
        Sound("Directed By", R.raw.directed_by, "R", isLocked = true, packName = "Classic", id = "directed_by"),
        Sound("Sudden Suspense", R.raw.sudden_suspense, "S", isLocked = true, packName = "Reaction", id = "sudden_suspense"),
        Sound("Yoooo Japan", R.raw.yoooooo_japan, "Y", isLocked = true, packName = "Chaos", id = "yoooo_japan"),
        Sound("Gop Gop Gop", R.raw.gop_gop_gop, "G", isLocked = true, packName = "Chaos", id = "gop_gop_gop"),
        Sound("Romantic", R.raw.romance_saxophone, "X", isLocked = true, packName = "Classic", id = "romantic"),
        Sound("Buzzer", R.raw.buzzer, "B", isLocked = false, packName = "Reaction", id = "buzzer"),
        Sound("Chicken Scream", R.raw.chicken_on_tree_screaming, "C", isLocked = false, packName = "Chaos", id = "chicken_scream"),
        Sound("Core Effect", R.raw.core_sound_effect, "C", isLocked = false, packName = "Reaction", id = "core_effect"),
        Sound("Crickets", R.raw.crickets, "C", isLocked = false, packName = "Reaction", id = "crickets"),
        Sound("Door Knock", R.raw.door_knocking, "D", isLocked = true, packName = "Classic", id = "door_knock"),
        Sound("Drum Roll", R.raw.drum_roll, "D", isLocked = true, packName = "Classic", id = "drum_roll"),
        Sound("Emotional Damage", R.raw.emotional_damage, "E", isLocked = true, packName = "Reaction", id = "emotional_damage"),
        Sound("FBI Open Up", R.raw.fbi_open_up, "F", isLocked = true, packName = "Chaos", id = "fbi_open_up"),
        Sound("GTA Wasted", R.raw.gta_v_wasted, "G", isLocked = true, packName = "Gamer", id = "gta_wasted"),
        Sound("John Cena", R.raw.his_name_is_john_cena, "J", isLocked = true, packName = "Classic", id = "john_cena"),
        Sound("Minecraft Hurt", R.raw.minecraft_hurt, "M", isLocked = true, packName = "Gamer", id = "minecraft_hurt"),
        Sound("Money", R.raw.money, "M", isLocked = true, packName = "Reaction", id = "money"),
        Sound("Roblox Oof", R.raw.roblox_oof, "R", isLocked = true, packName = "Gamer", id = "roblox_oof"),
        Sound("Sad Violin", R.raw.sad_violin, "S", isLocked = true, packName = "Classic", id = "sad_violin"),
        Sound("School Bell", R.raw.school_bell, "S", isLocked = true, packName = "Classic", id = "school_bell"),
        Sound("Short Rizz", R.raw.short_rizz, "R", isLocked = true, packName = "Chaos", id = "short_rizz"),
        Sound("Prowler", R.raw.spiderman_prowler, "P", isLocked = true, packName = "Drama", id = "prowler"),
        Sound("Spooderman", R.raw.spooderman, "S", isLocked = true, packName = "Chaos", id = "spooderman"),
        Sound("Undertaker Bell", R.raw.the_undertaker_bell, "U", isLocked = true, packName = "Classic", id = "undertaker_bell"),
        Sound("Ultra Suspense", R.raw.ultra_suspense, "U", isLocked = true, packName = "Drama", id = "ultra_suspense"),
        Sound("UwU", R.raw.uwu, "U", isLocked = true, packName = "Reaction", id = "uwu"),
        Sound("Whip", R.raw.whip, "W", isLocked = true, packName = "Reaction", id = "whip"),
        Sound("Why Are You Running", R.raw.why_are_you_running, "W", isLocked = true, packName = "Reaction", id = "why_are_you_running"),
        Sound("Womp Womp", R.raw.womp_womp_womp, "W", isLocked = true, packName = "Reaction", id = "womp_womp")
    )

    /** The first four additions stay free for every user, including existing installs. */
    val permanentlyFreeNewSoundIds: Set<String> = sounds
        .drop(ORIGINAL_SOUND_COUNT)
        .take(4)
        .map { it.id }
        .toSet()

    val defaultSelectedSound: Sound = sounds.first()
}
