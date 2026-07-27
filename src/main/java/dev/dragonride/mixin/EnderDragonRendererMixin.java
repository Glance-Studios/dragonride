package dev.dragonride.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dragonride.DragonScaleAccess;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the ender dragon respect the {@code minecraft:scale} attribute, which vanilla's custom
 * dragon renderer ignores. The entity's scale is captured into the render state, then the submit
 * call is wrapped in a matching push/scale/pop so the whole model draws at that scale.
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {

    @Unique
    private static float dragonride$lastScale = Float.NaN;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;F)V",
        at = @At("TAIL")
    )
    private void dragonride$captureScale(EnderDragon entity, EnderDragonRenderState state, float partialTick, CallbackInfo ci) {
        // getScale() reads AttributeMap.getValue(), which returns the supplier DEFAULT for the dragon
        // (its lazy attribute map never stores SCALE), so it's stuck at 1.0. Read the instance directly.
        AttributeInstance inst = entity.getAttribute(Attributes.SCALE);
        float s = inst != null ? (float) inst.getValue() : 1.0f;
        ((DragonScaleAccess) state).dragonride$setScale(s);
        if (s != dragonride$lastScale) {
            dragonride$lastScale = s;
            LoggerFactory.getLogger("dragonride").info(
                "[diag] client: getScale()={}, getAttribute(SCALE).getValue()={}", entity.getScale(), s);
        }
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void dragonride$pushScale(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        poseStack.pushPose();
        float s = ((DragonScaleAccess) state).dragonride$getScale();
        if (s != 1.0f) {
            poseStack.scale(s, s, s);
        }
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void dragonride$popScale(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        poseStack.popPose();
    }
}
