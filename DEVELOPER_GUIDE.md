# CustomGLTF Developer Guide & API Reference (v1.2)

Welcome to **CustomGLTF** (Minecraft 1.20.1 Fabric) — the high-performance 3D rendering library for Minecraft mod developers.

CustomGLTF empowers you to load, animate, attach, and render standard **glTF 2.0 / GLB** models directly in Minecraft with full hardware skinning, bone sockets, procedural animation, and seamless transparency handling.

---

## Table of Contents
1. [Gradle Dependency & Setup](#1-gradle-dependency--setup)
2. [Loading a glTF Model](#2-loading-a-gltf-model)
3. [Rendering Models](#3-rendering-models)
4. [Animation Controller & State Machine](#4-animation-controller--state-machine)
5. [Bone Sockets & Attachment System](#5-bone-sockets--attachment-system)
6. [Procedural Bone Overrides](#6-procedural-bone-overrides)
7. [Bounding Boxes & Hitboxes](#7-bounding-boxes--hitboxes)
8. [Transparency & Water Rendering](#8-transparency--water-rendering)
9. [Production-Ready Code Examples](#9-production-ready-code-examples)
   - [BlockEntity Renderer Template](#a-blockentity-renderer)
   - [LivingEntity / Mob Renderer Template](#b-livingentity-renderer)
   - [Gun / Weapon / Item In-Hand Renderer Template](#c-gun--item-renderer)

---

## 1. Gradle Dependency & Setup

Add CustomGLTF to your `build.gradle`:
```groovy
repositories {
    maven {
        url = "https://cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    modImplementation "curse.maven:customgltf-PROJECT_ID:FILE_ID"
    include "curse.maven:customgltf-PROJECT_ID:FILE_ID"
}
```

Or if you are developing locally with the jar in `libs/`:
```groovy
dependencies {
    modImplementation files("libs/CustomGLTF-1.2.jar")
    include files("libs/CustomGLTF-1.2.jar")
}
```

In `fabric.mod.json`:
```json
{
  "depends": {
    "customgltf": ">=1.2"
  }
}
```

---

## 2. Loading a glTF Model

Implement `IGltfModelReceiver` to register and load your `.gltf` or `.glb` model:

```java
package mymod.client;

import com.modularmods.customgltf.CustomGLTF;
import com.modularmods.customgltf.IGltfModelReceiver;
import com.modularmods.customgltf.RenderedGltfModel;
import de.javagl.jgltf.model.GltfModel;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class MyModelLoader implements IGltfModelReceiver {

    public static final MyModelLoader INSTANCE = new MyModelLoader();
    private RenderedGltfModel model;

    public void register() {
        CustomGLTF.getInstance().addGltfModelReceiver(this);
    }

    @Override
    public ResourceLocation getModelLocation() {
        // Points to src/main/resources/assets/mymod/models/my_model.gltf
        return new ResourceLocation("mymod", "models/my_model.gltf");
    }

    @Override
    public boolean isReceiveSharedModel(GltfModel gltfModel, List<Runnable> onFinish) {
        return true;
    }

    @Override
    public void onReceiveSharedModel(RenderedGltfModel model) {
        this.model = model;
    }

    public RenderedGltfModel getModel() {
        return model;
    }
}
```

---

## 3. Rendering Models

CustomGLTF provides high-level rendering methods that automatically convert `PoseStack` transformations and adapt to Vanilla or Shaderpack (Iris) pipelines.

### One-Line Rendering:
```java
import com.modularmods.customgltf.api.GltfRenderHelper;

// Render directly using instance method
model.render(poseStack);

// Or using static helper
GltfRenderHelper.renderModel(model, poseStack);
```

### Color Tinting (e.g. Hurt Flash, Team Colors, Cloaking):
```java
// Tint red for damage flash: (r, g, b, a)
model.renderWithTint(poseStack, 1.0F, 0.3F, 0.3F, 1.0F);

// Semi-transparent ghost mode (50% alpha)
model.renderWithTint(poseStack, 1.0F, 1.0F, 1.0F, 0.5F);
```

---

## 4. Animation Controller & State Machine

CustomGLTF includes a modern `GltfAnimationController` that parses all animations from your model by name, supports smooth cross-fading, loop modes, variable playback speed, and keyframe event callbacks.

```java
import com.modularmods.customgltf.animation.GltfAnimationController;

// 1. Get the animation controller from the model
GltfAnimationController anim = model.getAnimationController();

// 2. Play an animation (e.g. "idle" looping)
anim.play("idle", true);

// 3. Smoothly cross-fade to a new animation (e.g. transition to "run" over 0.2s)
anim.crossFade("run", 0.2F, true);

// 4. Adjust playback speed (e.g. 1.5x fast-forward or -1.0x reverse)
anim.setSpeed(1.5F);

// 5. Add timestamp-based sound / particle event callbacks
anim.addEventListener("reload", 1.25F, () -> {
    player.playSound(SoundEvents.IRON_TRAPDOOR_OPEN, 1.0F, 1.0F);
});
anim.addEventListener("shoot", 0.05F, () -> {
    level.addParticle(ParticleTypes.FLASH, x, y, z, 0, 0, 0);
});

// 6. Update in your entity / blockentity tick method
anim.update(0.05F); // 1 tick = 0.05 seconds (at 20 TPS)
```

---

## 5. Bone Sockets & Attachment System

Need to attach a gun sight/optic to a weapon rail, spawn muzzle flash particles at the barrel tip, seat a player in a vehicle, or attach a sword to a character's hand? Use `GltfNodeAttachment`:

```java
import com.modularmods.customgltf.api.GltfNodeAttachment;
import org.joml.Vector3f;

// 1. Attach and render an item/model onto a bone socket (e.g. "scope_rail")
poseStack.pushPose();
if (model.applyNodeTransform("scope_rail", poseStack)) {
    // Everything rendered here is now positioned & rotated at the scope_rail socket!
    renderScopeItem(poseStack);
}
poseStack.popPose();

// 2. Query 3D world position for particles / projectiles (e.g. "muzzle")
Vector3f muzzlePos = model.getNodePosition("muzzle");
if (muzzlePos != null) {
    level.addParticle(ParticleTypes.SMOKE, 
        entity.getX() + muzzlePos.x, 
        entity.getY() + muzzlePos.y, 
        entity.getZ() + muzzlePos.z, 
        0, 0.1, 0);
}
```

---

## 6. Procedural Bone Overrides

Directly rotate or translate bones from Java code (e.g. rotating tank turrets, aiming gun barrels with player look direction, turning vehicle steering wheels and speedometers):

```java
import com.modularmods.customgltf.animation.GltfProceduralController;

GltfProceduralController controller = model.getProceduralController();

// Aim turret towards target pitch and yaw (in degrees)
controller.setNodeRotationDegrees("turret", 0.0F, entity.getYRot(), 0.0F);
controller.setNodeRotationDegrees("barrel", entity.getXRot(), 0.0F, 0.0F);

// Turn steering wheel based on vehicle steering angle
controller.setNodeRotationDegrees("steering_wheel", 0.0F, 0.0F, steeringAngle);

// Spin vehicle wheel based on speed
controller.setNodeRotationDegrees("wheel_FL", wheelRotationDeg, 0.0F, 0.0F);

// Apply overrides before rendering
controller.applyOverrides();
```

---

## 7. Bounding Boxes & Hitboxes

Compute exact 3D bounding boxes from your model for hitboxes, selection boxes, or raycasting:

```java
import net.minecraft.world.phys.AABB;

// Get overall model bounding box
AABB box = model.getBoundingBox();

// Use for custom entity collision / interaction box
entity.setBoundingBox(box.move(entity.position()));
```

---

## 8. Transparency & Water Rendering

CustomGLTF automatically processes glTF 2.0 `alphaMode`:
- **`MASK` (Cutout)**: Discards transparent pixels (`discard;`), allowing water, glass, and translucent particles behind the model to be rendered correctly without void holes.
- **`BLEND` (Translucent)**: Enables hardware alpha blending.
- **`OPAQUE` (Solid)**: Renders fully opaque geometry.

If your model has transparent glass or cutout grates, it will work automatically without any extra configuration!

---

## 9. Production-Ready Code Examples

### A. BlockEntity Renderer

```java
package mymod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.modularmods.customgltf.RenderedGltfModel;
import com.modularmods.customgltf.api.GltfRenderHelper;
import mymod.block.entity.CustomMachineBlockEntity;
import mymod.client.MyModelLoader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CustomMachineRenderer implements BlockEntityRenderer<CustomMachineBlockEntity> {

    public CustomMachineRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(CustomMachineBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderedGltfModel model = MyModelLoader.INSTANCE.getModel();
        if (model == null) return;

        poseStack.pushPose();
        
        // Center on block
        poseStack.translate(0.5, 0.0, 0.5);
        
        // Rotate according to block facing
        poseStack.mulPose(Axis.YP.rotationDegrees(be.getBlockFacing().toYRot()));

        // Update animation
        model.getAnimationController().update(partialTick * 0.05F);

        // Render model
        model.render(poseStack);

        poseStack.popPose();
    }
}
```

---

### B. LivingEntity Renderer

```java
package mymod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.modularmods.customgltf.RenderedGltfModel;
import mymod.client.MyModelLoader;
import mymod.entity.CustomBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CustomBossRenderer extends EntityRenderer<CustomBossEntity> {

    public CustomBossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CustomBossEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        RenderedGltfModel model = MyModelLoader.INSTANCE.getModel();
        if (model == null) return;

        poseStack.pushPose();

        // Rotate to entity look direction
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Procedural head aim
        model.getProceduralController()
             .setNodeRotationDegrees("head", entity.getXRot(), 0.0F, 0.0F)
             .applyOverrides();

        // Hurt flash effect (tint red when damaged)
        if (entity.hurtTime > 0) {
            model.renderWithTint(poseStack, 1.0F, 0.4F, 0.4F, 1.0F);
        } else {
            model.render(poseStack);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomBossEntity entity) {
        return null;
    }
}
```

---

### C. Gun / Item Renderer

```java
package mymod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.customgltf.RenderedGltfModel;
import mymod.client.GunModelLoader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CustomGunRenderer {

    public static void renderGun(ItemStack stack, ItemDisplayContext transformType,
                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                 int packedLight, int packedOverlay) {
        RenderedGltfModel gunModel = GunModelLoader.INSTANCE.getModel();
        if (gunModel == null) return;

        poseStack.pushPose();

        // Render base gun
        gunModel.render(poseStack);

        // Attach optic / scope if present in item NBT
        if (stack.hasTag() && stack.getTag().contains("Optic")) {
            poseStack.pushPose();
            if (gunModel.applyNodeTransform("scope_mount", poseStack)) {
                RenderedGltfModel opticModel = GunModelLoader.OPTIC_RED_DOT.getModel();
                if (opticModel != null) {
                    opticModel.render(poseStack);
                }
            }
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
```
