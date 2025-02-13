package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.features.module.modules.movement.NoWeb

open class NoWebMode(name: String) : Choice(name) {
    override val choices: ChoiceConfigurable<*>
        get() = NoWeb.mode
}
