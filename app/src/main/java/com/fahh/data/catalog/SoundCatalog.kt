package com.fahh.data.catalog

import com.fahh.R
import com.fahh.data.model.Sound

/**
 * The bundled catalog is code-owned metadata. User unlock state lives in Room and is never
 * replaced when this list changes.
 */
object SoundCatalog {
    const val VERSION = 1

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
        Sound("Romantic", R.raw.romance_saxophone, "X", isLocked = true, packName = "Classic", id = "romantic")
    )

    val defaultSelectedSound: Sound = sounds.first()
}
