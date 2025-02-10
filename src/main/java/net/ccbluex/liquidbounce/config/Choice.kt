package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.event.Listenable

/**
 * @author yuchenxue
 * @date 2025/02/10
 */

abstract class Choice(name: String) : Configurable(name), Listenable {

    abstract val choices: ChoiceConfigurable<*>

    open fun enable() {}

    open fun disable() {}

    override val parent: Listenable?
        get() = choices.listenable

    private val isActive: Boolean get() = choices.current == this

    override fun handleEvents(): Boolean = super.handleEvents() && isActive

    override val values: MutableList<Value<*>>
        get() = super.values.map {
            it.setSupport { old -> old && isActive }
        }.toMutableList()
}