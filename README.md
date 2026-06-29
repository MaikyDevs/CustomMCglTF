# CustomGLTF

A glTF 2.0 model loader library for Minecraft 1.20.1 Fabric with shader support.

## What is this?

CustomGLTF lets you load and render 3D models in glTF format inside Minecraft. It's a library mod - other mods use it to add custom 3D models to the game.

## Features

- Load glTF 2.0 and GLB files
- Full skeletal animation support
- Iris Shaders integration
- OptiFine shader support (via OptiFabric)
- Hardware-accelerated model rendering
- Normal and specular map support for shaders
- Multiple OpenGL compatibility profiles

## Installation

### For Players

1. Install Fabric Loader 0.14.21 or newer
2. Install Fabric API 0.83.1+1.20.1 or newer
3. Download CustomGLTF and place in your mods folder
4. (Optional) Install Iris Shaders for shader support

### For Mod Developers

Add to your `build.gradle`:

```groovy
repositories {
    maven { url "https://cursemaven.com" }
}

dependencies {
    modImplementation "curse.maven:customgltf-PROJECT_ID:FILE_ID"
    include "curse.maven:customgltf-PROJECT_ID:FILE_ID"
}
```

See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for detailed implementation instructions.

## Configuration

Config file: `config/customgltf.properties`

```properties
# OpenGL profile selection
# Options: AUTO, GL43, GL40, GL33, GL30
# AUTO selects the best available for your hardware
RenderedModelGLProfile=AUTO
```

## What Changed from MCglTF?

This is a port of MCglTF to Minecraft 1.20.1 with the following changes:

- Updated to Minecraft 1.20.1
- Updated Fabric API and Loader
- Updated Iris Shaders integration
- Package renamed: `com.modularmods.mcgltf` → `com.modularmods.customgltf`
- Main class renamed: `MCglTF` → `CustomGLTF`
- Mod ID changed: `mcgltf` → `customgltf`
- OptiFine integration now uses reflection (no compile dependency)

## Requirements

- Minecraft: 1.20.1
- Fabric Loader: 0.14.21+
- Fabric API: 0.83.1+1.20.1
- Java: 17 or higher

**Optional:**
- Iris Shaders (for shader support)
- OptiFabric (for OptiFine shader support)

## Building

```bash
# Use Java 17-21 (Java 24 is not supported by Gradle 8.8)
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10

# Build
gradlew clean build

# Output
build/libs/CustomGLTF-1.20.1-Fabric-1.0.0.0.jar
```

## Credits

- **Original MCglTF**: TimLee9024 & Protoxy
- **JglTF Library**: Marco Hutter (javagl.de)
- **1.20.1 Port**: Maiky

## License

MIT License

Copyright (c) 2024 Maiky (CustomGLTF port)
Copyright (c) 2020-2023 TimLee9024 (Original MCglTF)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Links

- [Developer Guide](DEVELOPER_GUIDE.md) - How to use CustomGLTF in your mod
- [Original MCglTF](https://github.com/ModularMods/MCglTF) - The original project
- [glTF Format](https://www.khronos.org/gltf/) - Learn about glTF 2.0

## Support

Found a bug or need help? Create an issue on GitHub or leave a comment on CurseForge.

## Next Goals

- Port to Forge
