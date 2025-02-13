package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.event.Listenable

/**
 * @author yuchenxue
 * @date 2025/02/10
 */

open class ChoiceConfigurable<T: Choice>(
    name: String,
    val choices: Array<T>,
    default: T = choices[0],
    val listenable: Listenable? = null,
    displayable: (() -> Boolean) = { true }
) : Configurable(name) {
    var current = default

    val mode by +ListValue(name, choices.map { it.name }.toTypedArray(), default.name)
        .onChange { old, new ->
            choices.find { it.name == old }?.disable()
            val newChoice = choices.find { it.name == new } ?: choices[0]
            current = newChoice.apply { enable() }
            new
        }
        .setSupport {
            displayable.invoke()
        }

    fun getCurrentName() = current.name

    override val values: MutableList<Value<*>>
        get() = super.values.also {
            choices.forEach { choice ->
                it += choice.values
            }
        }
}