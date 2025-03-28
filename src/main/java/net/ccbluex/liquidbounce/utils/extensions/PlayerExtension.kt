/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.extensions

import akka.actor.Kill
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import de.florianmichael.vialoadingbase.ViaLoadingBase
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getFixedSensitivityAngle
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.*
import net.minecraftforge.event.ForgeEventFactory

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity) = eyes.distanceTo(getNearestPointBB(eyes, entity.hitBox))

fun Entity.getDistanceToBox(box: AxisAlignedBB) = eyes.distanceTo(getNearestPointBB(eyes, box))

fun EntityPlayerSP.isNearEdge(threshold: Float): Boolean {
    val playerPos = Vec3(posX, posY, posZ)
    val blockPos = BlockPos(playerPos)

    for (x in -3..3) {
        for (z in -3..3) {
            val checkPos = blockPos.add(x, -1, z)
            if (worldObj.isAirBlock(checkPos)) {
                val checkPosCenter = Vec3(checkPos.x + 0.5, checkPos.y.toDouble(), checkPos.z + 0.5)
                val distance = playerPos.distanceTo(checkPosCenter)
                if (distance <= threshold) {
                    return true
                }
            }
        }
    }
    return false
}

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing() = mc.netHandler.getPlayerInfo(uniqueID)?.responseTime ?: 0

fun Entity.isAnimal() =
    this is EntityAnimal
            || this is EntitySquid
            || this is EntityGolem
            || this is EntityBat

fun Entity.isMob() =
    this is EntityMob
            || this is EntityVillager
            || this is EntitySlime
            || this is EntityGhast
            || this is EntityDragon

fun EntityPlayer.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return friendsConfig.isFriend(stripColor(entityName))
}

var Entity?.rotation
    get() = Rotation(this?.rotationYaw ?: 0f, this?.rotationPitch ?: 0f)
    set(value) {
        this?.run {
            rotationYaw = value.yaw
            rotationPitch = value.pitch
        }
    }
var Entity?.prevRotation
    get() = Rotation(this?.prevRotationYaw ?: 0f, this?.prevRotationPitch ?: 0f)
    set(value) {
        this?.run {
            prevRotationYaw = value.yaw
            prevRotationPitch = value.pitch
        }
    }

val Entity.hitBox: AxisAlignedBB
    get() {
        val borderSize = collisionBorderSize.toDouble()
        return entityBoundingBox.expand(borderSize, borderSize, borderSize)
    }

val Entity.eyes: Vec3
    get() = getPositionEyes(1f)

val Entity.prevPos: Vec3
    get() = Vec3(prevPosX, prevPosY, prevPosZ)

val Entity.currPos: Vec3
    get() = this.positionVector

val Entity.lastTickPos: Vec3
    get() = Vec3(lastTickPosX, lastTickPosY, lastTickPosZ)

val EntityLivingBase?.isMoving: Boolean
    get() = this?.run { moveForward != 0F || moveStrafing != 0F } == true

val EntityPlayerSP.airTicks
    get() = MovementUtils.airTicks

val EntityPlayerSP.groundTicks
    get() = MovementUtils.groundTicks

val Entity.isInLiquid: Boolean
    get() = isInWater || isInLava

fun Entity.setPosAndPrevPos(currPos: Vec3, prevPos: Vec3 = currPos, lastTickPos: Vec3? = null) {
    setPosition(currPos.xCoord, currPos.yCoord, currPos.zCoord)
    prevPosX = prevPos.xCoord
    prevPosY = prevPos.yCoord
    prevPosZ = prevPos.zCoord

    lastTickPos?.let {
        this.lastTickPosX = it.xCoord
        this.lastTickPosY = it.yCoord
        this.lastTickPosZ = it.zCoord
    }
}

fun EntityPlayerSP.setFixedSensitivityAngles(yaw: Float? = null, pitch: Float? = null) {
    if (yaw != null) fixedSensitivityYaw = yaw

    if (pitch != null) fixedSensitivityPitch = pitch
}

var EntityPlayerSP.fixedSensitivityYaw
    get() = getFixedSensitivityAngle(mc.thePlayer.rotationYaw)
    set(yaw) {
        rotationYaw = getFixedSensitivityAngle(yaw, rotationYaw)
    }

var EntityPlayerSP.fixedSensitivityPitch
    get() = getFixedSensitivityAngle(rotationPitch)
    set(pitch) {
        rotationPitch = getFixedSensitivityAngle(pitch.coerceIn(-90f, 90f), rotationPitch)
    }

val IMixinEntity.interpolatedPosition
    get() = Vec3(lerpX, lerpY, lerpZ)

// Makes fixedSensitivityYaw, ... += work
operator fun EntityPlayerSP.plusAssign(value: Float) {
    fixedSensitivityYaw += value
    fixedSensitivityPitch += value
}

fun Entity.interpolatedPosition(start: Vec3, extraHeight: Float? = null) = Vec3(
    start.xCoord + (posX - start.xCoord) * mc.timer.renderPartialTicks,
    start.yCoord + (posY - start.yCoord) * mc.timer.renderPartialTicks + (extraHeight ?: 0f),
    start.zCoord + (posZ - start.zCoord) * mc.timer.renderPartialTicks
)

fun EntityPlayerSP.stopY() {
    motionY = 0.0
}

fun EntityPlayerSP.stopXZ() {
    motionX = 0.0
    motionZ = 0.0
}

fun EntityPlayerSP.stop() {
    stopXZ()
    stopY()
}

/**
 * Its sole purpose is to prevent duplicate sprint state updates.
 */
infix fun EntityLivingBase.setSprintSafely(new: Boolean) {
    if (new == isSprinting) {
        return
    }

    isSprinting = new
}

// Modified mc.playerController.onPlayerRightClick() that sends correct stack in its C08
fun EntityPlayerSP.onPlayerRightClick(
    clickPos: BlockPos, side: EnumFacing, clickVec: Vec3,
    stack: ItemStack? = inventory.mainInventory[SilentHotbar.currentSlot],
): Boolean {
    val controller = mc.playerController ?: return false

    controller.syncCurrentPlayItem()

    if (clickPos !in worldObj.worldBorder)
        return false

    val (facingX, facingY, facingZ) = (clickVec - clickPos.toVec()).toFloatArray()

    val sendClick = {
        sendPacket(C08PacketPlayerBlockPlacement(clickPos, side.index, stack, facingX, facingY, facingZ))
        true
    }

    // If player is a spectator, send click and return true
    if (controller.isSpectator)
        return sendClick()

    val item = stack?.item

    if (item?.onItemUseFirst(stack, this, worldObj, clickPos, side, facingX, facingY, facingZ) == true)
        return true

    val blockState = clickPos.state

    // If click had activated a block, send click and return true
    if ((!isSneaking || item == null || item.doesSneakBypassUse(worldObj, clickPos, this))
        && blockState?.block?.onBlockActivated(
            worldObj,
            clickPos,
            blockState,
            this,
            side,
            facingX,
            facingY,
            facingZ
        ) == true
    )
        return sendClick()

    if (item is ItemBlock && !item.canPlaceBlockOnSide(worldObj, clickPos, side, this, stack))
        return false

    sendClick()

    if (stack == null)
        return false

    val prevMetadata = stack.metadata
    val prevSize = stack.stackSize

    return stack.onItemUse(this, worldObj, clickPos, side, facingX, facingY, facingZ).also {
        if (controller.isInCreativeMode) {
            stack.itemDamage = prevMetadata
            stack.stackSize = prevSize
        } else if (stack.stackSize <= 0) {
            ForgeEventFactory.onPlayerDestroyItem(this, stack)
        }
    }
}

// Modified mc.playerController.sendUseItem() that sends correct stack in its C08
fun EntityPlayerSP.sendUseItem(stack: ItemStack): Boolean {
    if (mc.playerController.isSpectator)
        return false

    mc.playerController?.syncCurrentPlayItem()

    sendPacket(C08PacketPlayerBlockPlacement(stack))

    val prevSize = stack.stackSize

    val newStack = stack.useItemRightClick(worldObj, this)

    return if (newStack != stack || newStack.stackSize != prevSize) {
        if (newStack.stackSize <= 0) {
            mc.thePlayer.inventory.mainInventory[SilentHotbar.currentSlot] = null
            ForgeEventFactory.onPlayerDestroyItem(mc.thePlayer, newStack)
        } else
            mc.thePlayer.inventory.mainInventory[SilentHotbar.currentSlot] = newStack

        true
    } else false
}

fun EntityPlayerSP.tryJump() {
    if (!mc.gameSettings.keyBindJump.isKeyDown) {
        jump()
    }
}

fun EntityPlayerSP.attackEntityWithModifiedSprint(
    entity: Entity, affectMovementBySprint: Boolean? = null, swing: () -> Unit
) {
    if (!ViaLoadingBase.getInstance().targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_12_2)) swing()

    MovementUtils.affectSprintOnAttack = affectMovementBySprint

    try {
        mc.playerController?.attackEntity(this, entity)
    } catch (any: Exception) {
        // Unlikely to happen, but if it does, we just want to make sure affectSprintOnAttack is null.
        any.printStackTrace()
    }

    MovementUtils.affectSprintOnAttack = null

   if (ViaLoadingBase.getInstance().targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_12_2)) swing()

    CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)
}

//fun Entity.getCustomPositionVector(): Vector3d {
//    return Vector3d(posX, posY, posZ)
//}

fun Entity.getPositionEyes(partialTicks: Float): Vec3 {
    if (partialTicks == 1.0f) {
        return Vec3(this.posX, this.posY + this.getEyeHeight().toDouble(), this.posZ)
    } else {
        val d0: Double = this.prevPosX + (this.posX - this.prevPosX) * partialTicks.toDouble()
        val d1: Double =
            this.prevPosY + ((this.posY - this.prevPosY) * partialTicks.toDouble()) + (this.getEyeHeight().toDouble())
        val d2: Double = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks.toDouble()
        return Vec3(d0, d1, d2)
    }
}

fun Entity.rayTrace(blockReachDistance: Double, partialTicks: Float): MovingObjectPosition {
    val vec3: Vec3 = this.getPositionEyes(partialTicks)
    val vec31: Vec3 = this.getLook(partialTicks)
    val vec32 = vec3.addVector(
        vec31.xCoord * blockReachDistance,
        vec31.yCoord * blockReachDistance,
        vec31.zCoord * blockReachDistance
    )
    return this.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)
}

fun Entity.rayTraceCustom(blockReachDistance: Float, yaw: Float, pitch: Float): MovingObjectPosition {
    val vec3: Vec3 = this.getPositionEyes(1.0f)
    val vec31: Vec3 = this.getLookCustom(yaw, pitch)
    val vec32 = vec3.addVector(
        vec31.xCoord * blockReachDistance,
        vec31.yCoord * blockReachDistance,
        vec31.zCoord * blockReachDistance
    )
    return this.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)
}

fun Entity.getLookCustom(yaw: Float, pitch: Float): Vec3 {
    return this.getVectorForRotation(pitch, yaw)
}

fun Entity.getVectorForRotation(p_getVectorForRotation_1_: Float, p_getVectorForRotation_2_: Float): Vec3 {
    val f = MathHelper.cos(-p_getVectorForRotation_2_ * 0.017453292f - 3.1415927f)
    val f1 = MathHelper.sin(-p_getVectorForRotation_2_ * 0.017453292f - 3.1415927f)
    val f2 = -MathHelper.cos(-p_getVectorForRotation_1_ * 0.017453292f)
    val f3 = MathHelper.sin(-p_getVectorForRotation_1_ * 0.017453292f)
    return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
}

val EntityPlayerSP.direction: Float
    get() = getMovementDirectionOfInput(DirectionalInput(movementInput))


fun EntityPlayerSP.getMovementDirectionOfInput(input: DirectionalInput): Float {
    return getMovementDirectionOfInput(if (KillAura.options.strict && currentRotation != null && KillAura.state) currentRotation!!.yaw else this.rotation.yaw, input)
}

fun getMovementDirectionOfInput(facingYaw: Float, input: DirectionalInput): Float {
    var actualYaw = facingYaw
    var forward = 1f

    // Check if client-user tries to walk backwards (+180 to turn around)
    if (input.backwards) {
        actualYaw += 180f
        forward = -0.5f
    } else if (input.forwards) {
        forward = 0.5f
    }

    // Check which direction the client-user tries to walk sideways
    if (input.left) {
        actualYaw -= 90f * forward
    }
    if (input.right) {
        actualYaw += 90f * forward
    }

    return actualYaw
}