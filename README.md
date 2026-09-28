# Dragon Ride

`/dragonride` drops a shrunken ender dragon at your feet and sits you on it.

```
/dragonride                  a 0.25-scale dragon, no AI
/dragonride 0.6              same, bigger
/dragonride 0.4 true         leave the flight AI on
```

Scale takes anything from `0.05` to `16.0`. With `fly` false, which is the default, the dragon gets
`NoAI` and stays put - a stable mount you can pose and ride. With `fly` true it runs its own flight
patterns, which look right but are not steerable; nothing here makes a dragon controllable.

## The part that took the work

Vanilla ignores the `minecraft:scale` attribute on the ender dragon. It has a custom renderer that
never consults it, so setting the attribute shrinks the hitbox and nothing else - you get a
full-size dragon with a tiny collision box. Two client mixins fix that:

`EnderDragonRendererMixin` captures the entity's scale into the render state, then wraps the submit
call in a matching push/scale/pop so the whole model draws at that size. The render state has no
reference back to the entity, so a duck interface on it carries the value across.

`LivingEntityScaleMixin` deals with a second problem. The dragon's attribute map is lazy, so
`getScale()` reads the supplier default of 1.0 even after the base value is set, which leaves the
hitbox, the passenger seat and the 16-block ride camera all at full size. Reading the attribute
instance directly gives the real number, and the camera distance is scaled by it, so riding a
mini-dragon sits the camera where a horse would.

## Not server-side

Unlike most of the mods here, this one needs the client. The command is server-side but the render
mixins are not, so a player without the mod sees a full-size dragon with a shrunken hitbox.

## Requirements

| Mod | Version |
| --- | --- |
| `dragonride-0.1.0.jar` | this mod, on both sides |
| [Fabric API](https://modrinth.com/mod/fabric-api) | `0.155.2+26.2` (or compatible) |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | `1.13.12+kotlin.2.4.0` (or compatible) |

Minecraft **26.2**, Fabric Loader **0.19.3+**, **Java 25**.

## Limits

Only ender dragons are touched - an unscaled dragon reads 1.0 and is unaffected. The dragon is a
real entity, so it persists until killed. There is no command to remove one.
