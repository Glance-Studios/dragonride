package dev.dragonride.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The ender dragon's attribute map is lazy, so {@code AttributeMap.getValue(SCALE)} behind
 * {@link LivingEntity#getScale()} returns the default 1.0 even once the base value is set, leaving
 * the hitbox, the seat and the 16-block CAMERA_DISTANCE at full size. Reading the SCALE attribute
 * instance directly gives the real value. Only ender dragons are touched.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityScaleMixin {

    @Inject(method = "getScale", at = @At("HEAD"), cancellable = true)
    private void dragonride$realDragonScale(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof EnderDragon) {
            AttributeInstance inst = ((LivingEntity) (Object) this).getAttribute(Attributes.SCALE);
            if (inst != null) {
                cir.setReturnValue((float) inst.getValue());
            }
        }
    }

    @Inject(method = "getAttributeValue(Lnet/minecraft/core/Holder;)D", at = @At("HEAD"), cancellable = true)
    private void dragonride$scaleCameraDistance(Holder<Attribute> holder, CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof EnderDragon && holder == Attributes.CAMERA_DISTANCE) {
            LivingEntity self = (LivingEntity) (Object) this;
            AttributeInstance scale = self.getAttribute(Attributes.SCALE);
            AttributeInstance cam = self.getAttribute(Attributes.CAMERA_DISTANCE);
            double s = scale != null ? scale.getValue() : 1.0;
            double base = cam != null ? cam.getValue() : 16.0;
            cir.setReturnValue(base * s);
        }
    }
}
