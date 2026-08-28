package com.modularmods.customgltf;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

public class RenderedGltfScene {

	public final List<Runnable> skinningCommands = new ArrayList<Runnable>();
	
	public final List<Runnable> vanillaRenderCommands = new ArrayList<Runnable>();
	
	public final List<Runnable> shaderModRenderCommands = new ArrayList<Runnable>();
	
	protected void performSkinning() {
		if(!skinningCommands.isEmpty()) {
			GL20.glUseProgram(CustomGLTF.getInstance().getGlProgramSkinnig());
			GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
			skinningCommands.forEach(Runnable::run);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
			GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
			GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
		}
	}

	public void renderForVanilla() {
		CustomGLTF.checkAndWarnShaders();

		int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		int prevVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int prevArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int prevElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		boolean prevCull = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		boolean prevBlend = GL11.glGetBoolean(GL11.GL_BLEND);
		boolean prevDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		boolean prevDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
		
		int prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		GlStateManager._activeTexture(GL13.GL_TEXTURE2);
		int prevTex2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager._activeTexture(GL13.GL_TEXTURE1);
		int prevTex1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager._activeTexture(GL13.GL_TEXTURE0);
		int prevTex0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		
		performSkinning();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE = GameRenderer.getRendertypeEntityCutoutShader();
		ShaderInstance shaderInstance = RenderedGltfModel.CURRENT_SHADER_INSTANCE;
		int entityProgram = shaderInstance.getId();
		GL20.glUseProgram(entityProgram);
		
		shaderInstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
		shaderInstance.PROJECTION_MATRIX.upload();
		
		shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.upload();
		
		shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
		shaderInstance.FOG_START.upload();
		
		shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
		shaderInstance.FOG_END.upload();
		
		shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		shaderInstance.FOG_COLOR.upload();
		
		shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		shaderInstance.FOG_SHAPE.upload();
		
		shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		shaderInstance.COLOR_MODULATOR.upload();
		
		int sampler0Loc = GL20.glGetUniformLocation(entityProgram, "Sampler0");
		if (sampler0Loc != -1) GL20.glUniform1i(sampler0Loc, 0);
		int sampler1Loc = GL20.glGetUniformLocation(entityProgram, "Sampler1");
		if (sampler1Loc != -1) GL20.glUniform1i(sampler1Loc, 1);
		int sampler2Loc = GL20.glGetUniformLocation(entityProgram, "Sampler2");
		if (sampler2Loc != -1) GL20.glUniform1i(sampler2Loc, 2);
		
		Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		GlStateManager._activeTexture(GL13.GL_TEXTURE0);
		
		RenderSystem.setupShaderLights(shaderInstance);
		RenderedGltfModel.LIGHT0_DIRECTION = new Vector3f(shaderInstance.LIGHT0_DIRECTION.getFloatBuffer());
		RenderedGltfModel.LIGHT1_DIRECTION = new Vector3f(shaderInstance.LIGHT1_DIRECTION.getFloatBuffer());
		
		// Set default vertex attributes
		GL20.glVertexAttrib4f(RenderedGltfModel.vaColor, 1.0F, 1.0F, 1.0F, 1.0F);
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 10);
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 240, 240);
		
		GlStateManager._enableDepthTest();
		GlStateManager._depthMask(true);
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		vanillaRenderCommands.forEach(Runnable::run);
		
		// Restore GL state
		GlStateManager._depthMask(prevDepthMask);
		if (prevDepthTest) GlStateManager._enableDepthTest();
		else GlStateManager._disableDepthTest();
		
		if (prevCull) GlStateManager._enableCull();
		else GlStateManager._disableCull();
		
		if (prevBlend) GlStateManager._enableBlend();
		else GlStateManager._disableBlend();
		
		GlStateManager._activeTexture(GL13.GL_TEXTURE2);
		GlStateManager._bindTexture(prevTex2);
		GlStateManager._activeTexture(GL13.GL_TEXTURE1);
		GlStateManager._bindTexture(prevTex1);
		GlStateManager._activeTexture(GL13.GL_TEXTURE0);
		GlStateManager._bindTexture(prevTex0);
		GlStateManager._activeTexture(prevActiveTexture);
		
		GlStateManager._glBindVertexArray(prevVAO);
		GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, prevArrayBuffer);
		GlStateManager._glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevElementArrayBuffer);
		
		GlStateManager._glUseProgram(prevProgram);
		Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
		BufferUploader.reset();
		
		RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
	}
	
	public void renderForShaderMod() {
		renderForVanilla();
	}

}
