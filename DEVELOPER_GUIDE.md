# CustomGLTF Developer Guide

## Overview

CustomGLTF is a library for loading and rendering glTF 2.0 models in Minecraft 1.20.1 Fabric. This is a port of MCglTF with updated dependencies and renamed packages.

## What Changed from MCglTF

### Package Names
- `com.modularmods.mcgltf` → `com.modularmods.customgltf`
- All class imports need to be updated

### Main Class
- `MCglTF` → `CustomGLTF`
- `MCglTF.getInstance()` → `CustomGLTF.getInstance()`

### Version Changes
- Minecraft: 1.19.3 → 1.20.1
- Fabric Loader: 0.14.12 → 0.14.21
- Fabric API: 0.72.0+1.19.3 → 0.83.1+1.20.1
- Iris Shaders: Updated to 1.7.5 for 1.20.1

### Iris API Changes
The Iris package was renamed in newer versions:
```java
// Old (1.19.3)
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;

// New (1.20.1)
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
```

### OptiFine Integration
OptiFine support now uses reflection to avoid compile-time dependencies:
```java
// Old: Direct class access (caused compile errors)
net.optifine.shaders.Shaders.isShaderPackInitialized

// New: Reflection-based (no compile dependency needed)
Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
```

### Mod ID
- `mcgltf` → `customgltf`
- Resource locations need updating: `new ResourceLocation("customgltf", "...")`

## How to Use CustomGLTF in Your Mod

### 1. Add Dependency

Add to your `build.gradle`:
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
    // Add CustomGLTF as a dependency
    modImplementation "curse.maven:customgltf-PROJECT_ID:FILE_ID"
    include "curse.maven:customgltf-PROJECT_ID:FILE_ID"
}
```

Or if you have the JAR locally:
```groovy
dependencies {
    modImplementation files("libs/CustomGLTF-1.20.1-Fabric-1.0.0.0.jar")
    include files("libs/CustomGLTF-1.20.1-Fabric-1.0.0.0.jar")
}
```

### 2. Add to fabric.mod.json

```json
{
  "depends": {
    "customgltf": ">=1.0.0"
  }
}
```

### 3. Basic Usage

#### Loading a Model

```java
import com.modularmods.customgltf.CustomGLTF;
import com.modularmods.customgltf.IGltfModelReceiver;
import com.modularmods.customgltf.RenderedGltfModel;
import net.minecraft.resources.ResourceLocation;

public class MyModelLoader implements IGltfModelReceiver {
    private RenderedGltfModel model;
    
    public MyModelLoader() {
        // Register this receiver to load a model
        CustomGLTF.getInstance().addGltfModelReceiver(this);
    }
    
    @Override
    public ResourceLocation getModelLocation() {
        // Return the location of your glTF model file
        return new ResourceLocation("mymod", "models/my_model.gltf");
    }
    
    @Override
    public boolean isReceiveSharedModel(GltfModel gltfModel, List<Runnable> onFinish) {
        // Return true if you want to receive this model
        return true;
    }
    
    @Override
    public void onReceiveSharedModel(RenderedGltfModel model) {
        // Store the rendered model for later use
        this.model = model;
    }
}
```

#### Rendering a Model

```java
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public void renderMyModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    if (model != null) {
        poseStack.pushPose();
        
        // Position and scale your model
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.scale(1.0f, 1.0f, 1.0f);
        
        // Render all scenes in the model
        for (RenderedGltfScene scene : model.renderedGltfScenes) {
            scene.renderForPlayer(poseStack);
        }
        
        poseStack.popPose();
    }
}
```

#### Animations

```java
import com.modularmods.customgltf.animation.GltfAnimationCreator;
import com.modularmods.customgltf.animation.InterpolatedChannel;

public class AnimatedModel {
    private List<InterpolatedChannel> animations;
    private float animationTime = 0.0f;
    
    public void setupAnimations(RenderedGltfModel model) {
        // Get the first animation
        if (!model.gltfAnimations.isEmpty()) {
            animations = model.gltfAnimations.get(0);
        }
    }
    
    public void tick() {
        animationTime += 0.05f; // Adjust speed as needed
        
        if (animations != null) {
            for (InterpolatedChannel channel : animations) {
                channel.update(animationTime);
            }
        }
    }
}
```

### 4. Loading Models from Resources

Place your glTF files in your mod's resources:
```
src/main/resources/
  assets/
    mymod/
      models/
        my_model.gltf
        my_model.bin
        textures/
          my_texture.png
```

For external resources (using extras):
```java
// In your glTF file, add extras to buffers/images:
{
  "buffers": [{
    "uri": "data.bin",
    "byteLength": 1024,
    "extras": {
      "resourceLocation": "mymod:models/data.bin"
    }
  }]
}
```

### 5. Shader Support

CustomGLTF automatically integrates with:
- **Iris Shaders**: Full support for shader packs
- **OptiFine**: Via OptiFabric (using reflection)

The library handles normal maps and specular maps when shaders are active.

## OpenGL Profiles

CustomGLTF supports different OpenGL profiles for compatibility:

- **GL43**: Best performance, uses compute shaders (OpenGL 4.3+)
- **GL40**: Good performance, uses transform feedback (OpenGL 4.0+)
- **GL33**: Compatible mode (OpenGL 3.3+)
- **GL30**: CPU skinning fallback (OpenGL 3.0+)
- **AUTO**: Automatically selects best available

Users can configure this in `config/customgltf.properties`:
```properties
RenderedModelGLProfile=AUTO
```

## Example: Block Entity with glTF Model

```java
public class MyBlockEntity extends BlockEntity {
    private static MyModelLoader modelLoader;
    
    static {
        // Initialize once
        modelLoader = new MyModelLoader();
    }
    
    public MyBlockEntity(BlockPos pos, BlockState state) {
        super(MY_BLOCK_ENTITY, pos, state);
    }
}

public class MyBlockEntityRenderer implements BlockEntityRenderer<MyBlockEntity> {
    @Override
    public void render(MyBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        
        // Render the glTF model
        MyBlockEntity.modelLoader.render(poseStack, packedLight);
        
        poseStack.popPose();
    }
}
```

## Common Issues

### Models don't load
- Check that the glTF file path is correct
- Ensure the model is in your mod's resources
- Check logs for parsing errors

### Textures are missing
- Make sure texture files are in the correct location
- Check that URIs in the glTF file match your resource paths
- Use extras.resourceLocation for custom resource locations

### Performance issues
- Try a different OpenGL profile
- Reduce model complexity
- Check if animations are too complex

### Shader compatibility
- Update Iris to latest version
- Ensure shader pack supports PBR materials
- Check logs for shader-related errors

## Building from Source

```bash
# Set Java 17-21 (not Java 24!)
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10

# Build
gradlew build

# Output: build/libs/CustomGLTF-1.20.1-Fabric-1.0.0.0.jar
```

## Credits

- Original MCglTF by TimLee9024
- JglTF library by Marco Hutter
- Port to 1.20.1 and rename by Maiky

## License

MIT License - Same as original MCglTF

## Support

For issues or questions, please create an issue on the GitHub repository or leave a comment on CurseForge.
