package com.modularmods.customgltf.api;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.customgltf.CustomGLTF;
import com.modularmods.customgltf.RenderedGltfModel;
import com.modularmods.customgltf.RenderedGltfScene;

/**
 * High-level helper class for easily rendering glTF models in Minecraft 1.20.1.
 * Handles PoseStack transformations, lighting, and GL state safety.
 */
public final class GltfRenderHelper {

	private GltfRenderHelper() {
		// Utility class
	}

	/**
	 * Renders a full glTF model using the given PoseStack.
	 *
	 * @param model The rendered glTF model
	 * @param poseStack The Minecraft PoseStack for position, rotation, and scale
	 */
	public static void renderModel(RenderedGltfModel model, PoseStack poseStack) {
		renderModelWithTint(model, poseStack, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	/**
	 * Renders a full glTF model using the given PoseStack and custom color tint.
	 *
	 * @param model The rendered glTF model
	 * @param poseStack The Minecraft PoseStack
	 * @param r Red tint (0.0 to 1.0)
	 * @param g Green tint (0.0 to 1.0)
	 * @param b Blue tint (0.0 to 1.0)
	 * @param a Alpha tint (0.0 to 1.0)
	 */
	public static void renderModelWithTint(RenderedGltfModel model, PoseStack poseStack, float r, float g, float b, float a) {
		if (model == null || model.renderedGltfScenes == null || model.renderedGltfScenes.isEmpty()) {
			return;
		}

		CustomGLTF.checkAndWarnShaders();
		RenderedGltfModel.setCurrentPose(poseStack);
		
		Vector4f prevShaderColor = null;
		if (r != 1.0F || g != 1.0F || b != 1.0F || a != 1.0F) {
			float[] currentColor = RenderSystem.getShaderColor();
			prevShaderColor = new Vector4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
			RenderSystem.setShaderColor(r, g, b, a);
		}

		for (RenderedGltfScene scene : model.renderedGltfScenes) {
			scene.renderForVanilla();
		}

		if (prevShaderColor != null) {
			RenderSystem.setShaderColor(prevShaderColor.x, prevShaderColor.y, prevShaderColor.z, prevShaderColor.w);
		}
	}

	/**
	 * Renders a single scene from a glTF model using the given PoseStack.
	 *
	 * @param scene The rendered glTF scene
	 * @param poseStack The Minecraft PoseStack
	 */
	public static void renderScene(RenderedGltfScene scene, PoseStack poseStack) {
		renderSceneWithTint(scene, poseStack, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	/**
	 * Renders a single scene from a glTF model with a color tint.
	 *
	 * @param scene The rendered glTF scene
	 * @param poseStack The Minecraft PoseStack
	 * @param r Red tint (0.0 to 1.0)
	 * @param g Green tint (0.0 to 1.0)
	 * @param b Blue tint (0.0 to 1.0)
	 * @param a Alpha tint (0.0 to 1.0)
	 */
	public static void renderSceneWithTint(RenderedGltfScene scene, PoseStack poseStack, float r, float g, float b, float a) {
		if (scene == null) {
			return;
		}

		CustomGLTF.checkAndWarnShaders();
		RenderedGltfModel.setCurrentPose(poseStack);

		Vector4f prevShaderColor = null;
		if (r != 1.0F || g != 1.0F || b != 1.0F || a != 1.0F) {
			float[] currentColor = RenderSystem.getShaderColor();
			prevShaderColor = new Vector4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
			RenderSystem.setShaderColor(r, g, b, a);
		}

		scene.renderForVanilla();

		if (prevShaderColor != null) {
			RenderSystem.setShaderColor(prevShaderColor.x, prevShaderColor.y, prevShaderColor.z, prevShaderColor.w);
		}
	}

	public static void setupLightAndOverlay(int packedLight, int packedOverlay) {
		int u = packedLight & 0xFFFF;
		int v = (packedLight >> 16) & 0xFFFF;
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, u, v);
		
		int ou = packedOverlay & 0xFFFF;
		int ov = (packedOverlay >> 16) & 0xFFFF;
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, ou, ov);
	}

	public static void renderModel(RenderedGltfModel model, PoseStack poseStack, int packedLight, int packedOverlay) {
		renderModelWithTint(model, poseStack, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void renderModelWithTint(RenderedGltfModel model, PoseStack poseStack, int packedLight, int packedOverlay, float r, float g, float b, float a) {
		setupLightAndOverlay(packedLight, packedOverlay);
		renderModelWithTint(model, poseStack, r, g, b, a);
	}

	public static void renderScene(RenderedGltfScene scene, PoseStack poseStack, int packedLight, int packedOverlay) {
		renderSceneWithTint(scene, poseStack, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void renderSceneWithTint(RenderedGltfScene scene, PoseStack poseStack, int packedLight, int packedOverlay, float r, float g, float b, float a) {
		setupLightAndOverlay(packedLight, packedOverlay);
		renderSceneWithTint(scene, poseStack, r, g, b, a);
	}
}
