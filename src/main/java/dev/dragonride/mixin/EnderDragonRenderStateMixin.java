package dev.dragonride.mixin;

import dev.dragonride.DragonScaleAccess;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Adds a per-frame scale field to the dragon's render state (see {@link DragonScaleAccess}). */
@Mixin(EnderDragonRenderState.class)
public abstract class EnderDragonRenderStateMixin implements DragonScaleAccess {

    @Unique
    private float dragonride$scale = 1.0f;

    @Override
    public float dragonride$getScale() {
        return dragonride$scale;
    }

    @Override
    public void dragonride$setScale(float scale) {
        this.dragonride$scale = scale;
    }
}
