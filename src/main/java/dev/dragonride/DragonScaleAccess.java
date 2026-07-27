package dev.dragonride;

/**
 * Duck interface mixed into {@code EnderDragonRenderState} so the renderer mixin can carry the
 * dragon's scale (captured from the entity during extractRenderState) into the render/submit call,
 * which only has access to the render state, not the entity.
 */
public interface DragonScaleAccess {
    float dragonride$getScale();

    void dragonride$setScale(float scale);
}
