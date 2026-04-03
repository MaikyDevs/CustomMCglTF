package com.modularmods.customgltf.iris;

import com.modularmods.customgltf.RenderedGltfModel;
import com.modularmods.customgltf.RenderedGltfSceneGL30;

public class RenderedGltfSceneGL30Iris extends RenderedGltfSceneGL30 {

	@Override
	public void renderForVanilla() {
		vanillaRenderCommands.forEach(Runnable::run);
		
		RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
	}

	@Override
	public void renderForShaderMod() {
		shaderModRenderCommands.forEach(Runnable::run);
		
		RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
	}

}
