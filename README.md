# CustomGLTF (1.2)

A glTF 2.0 / GLB model loader and rendering library for Minecraft 1.20.1 Fabric.

## What is this?

CustomGLTF lets you load, animate, attach, and render standard 3D models in glTF/GLB format inside Minecraft 1.20.1 Fabric. It is designed as a developer library for mod creators to easily bring rich 3D models into their mods with high performance and minimal boilerplate.

## Features

- **glTF 2.0 & GLB Loading**: Loads standard `.gltf` and `.glb` files with mesh, materials, textures, and skeletal data.
- **Hardware-Accelerated Skinning**: Smooth GPU skeletal animation (OpenGL 4.3, 4.0, 3.3) with automatic fallback.
- **Built-in Animation Controller**: High-level state machine supporting crossfading, play/pause, looping, speed controls, and layer blending.
- **Bone Sockets & Attachment System**: Easily attach items, weapons, effects, or other models to specific animated bones/nodes (`model.getAttachment("hand_R").render(...)`).
- **Procedural Bone Overrides**: Dynamically rotate or translate specific bones via code (turret aiming, vehicle steering wheels, player head tracking).
- **Accurate Bounding Boxes**: Automatically calculate 3D bounding boxes from the model geometry for hitboxes and selection boxes.
- **Flawless Transparency & Water Rendering**: Proper alpha blending and cutout fragment discard ensures water, glass, and particles behind transparent glTF textures render without black boxes or occlusion artifacts.
- **Bilingual Shader Warning**: Automatic localized in-game chat warning (English & German) informing players when shaders are active (shaders are unsupported).

## Installation

### For Players

1. Install [Fabric Loader](https://fabricmc.net/) (0.14.21 or newer).
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) (0.83.1+1.20.1 or newer).
3. Download `CustomGLTF-1.2.jar` and place it into your `.minecraft/mods` folder.

> **Note on Shaders:** CustomGLTF does not support shader packs. If shaders are enabled, an automatic chat warning will notify you to turn shaders off for proper model rendering.

---

### For Mod Developers

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

Or for local development, place `CustomGLTF-1.2.jar` into your project's `libs/` folder:

```groovy
dependencies {
    modImplementation files("libs/CustomGLTF-1.2.jar")
    include files("libs/CustomGLTF-1.2.jar")
}
```

See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for full documentation and production-ready code examples.

---

## Configuration

Configuration file location: `config/customgltf.properties`

```properties
# OpenGL profile selection
# Options: AUTO, GL43, GL40, GL33, GL30
# AUTO selects the best available profile based on hardware
RenderedModelGLProfile=AUTO
```

---

## Building

```bash
# Ensure Java 17-21 is used
./gradlew build

# Output:
# build/libs/CustomGLTF-1.2.jar
```

---

## Credits

- **Original MCglTF**: TimLee9024 & Protoxy
- **JglTF Library**: Marco Hutter (javagl.de)
- **1.20.1 Port & Features**: Maiky

## License

MIT License
