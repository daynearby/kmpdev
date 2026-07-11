package com.example.kmpdev.app.ui

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode

object NoRipple : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        IndicationInstance(interactionSource)

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this

    private class IndicationInstance(private val interactionSource: InteractionSource) :
        Modifier.Node(), DrawModifierNode {

        override fun onAttach() {
        }

        override fun ContentDrawScope.draw() {
            drawContent()
        }
    }
}