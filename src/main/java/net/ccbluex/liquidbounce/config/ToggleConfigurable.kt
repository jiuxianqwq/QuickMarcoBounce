package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.event.Listenable

/**
 * @author yuchenxue
 * @date 2025/02/10
 */

open class ToggleConfigurable(
    name: String,
    value: Boolean,
    private val listenable: Listenable? = null,
    displayable: (() -> Boolean) = { true }
) : Configurable(name), Listenable {

    val enable by +BoolValue(name, value)
        .onChange { _, new ->
            if (new) {
                enable()
            } else {
                disable()
            }
            new
        }
        .setSupport {
            displayable.invoke()
        }

    open fun enable() {}
    open fun disable() {}

    override val parent: Listenable?
        get() = listenable

    override fun handleEvents(): Boolean = super.handleEvents() && enable
}