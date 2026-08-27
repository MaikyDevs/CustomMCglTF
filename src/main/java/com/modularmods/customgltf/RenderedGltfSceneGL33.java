package com.modularmods.customgltf;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;

import net.minecraft.client.renderer.GameRenderer;

public class RenderedGltfSceneGL33 extends RenderedGltfScene {

	@Override
	public void renderForVanilla() {
		int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		
		if(!skinningCommands.isEmpty()) {
			GL20.glUseProgram(CustomGLTF.getInstance().getGlProgramSkinnig());
			GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
			skinningCommands.forEach(Runnable::run);
			GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
			GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
		}
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE = GameRenderer.getRendertypeEntitySolidShader();
		int entitySolidProgram = RenderedGltfModel.CURRENT_SHADER_INSTANCE.getId();
		GL20.glUseProgram(entitySolidProgram);
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_START.set(RenderSystem.getShaderFogStart());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_START.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_END.set(RenderSystem.getShaderFogEnd());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_END.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_COLOR.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_SHAPE.upload();
		
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.set(1.0F, 1.0F, 1.0F, 1.0F);
		RenderedGltfModel.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.upload();
		
		int sampler0Loc = GL20.glGetUniformLocation(entitySolidProgram, "Sampler0");
		if (sampler0Loc != -1) GL20.glUniform1i(sampler0Loc, 0);
		int sampler1Loc = GL20.glGetUniformLocation(entitySolidProgram, "Sampler1");
		if (sampler1Loc != -1) GL20.glUniform1i(sampler1Loc, 1);
		int sampler2Loc = GL20.glGetUniformLocation(entitySolidProgram, "Sampler2");
		if (sampler2Loc != -1) GL20.glUniform1i(sampler2Loc, 2);
		
		RenderSystem.setupShaderLights(RenderedGltfModel.CURRENT_SHADER_INSTANCE);
		RenderedGltfModel.LIGHT0_DIRECTION = new Vector3f(RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer());
		RenderedGltfModel.LIGHT1_DIRECTION = new Vector3f(RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer());
		
		vanillaRenderCommands.forEach(Runnable::run);
		
		if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL30.glBindVertexArray(currentVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
		
		GL20.glUseProgram(currentProgram);
		
		BufferUploader.reset();
		
		RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
	}

	@Override
	public void renderForShaderMod() {
		int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		
		if(!skinningCommands.isEmpty()) {
			GL20.glUseProgram(CustomGLTF.getInstance().getGlProgramSkinnig());
			GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
			skinningCommands.forEach(Runnable::run);
			GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
			GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
			GL20.glUseProgram(currentProgram);
		}
		
		RenderedGltfModel.MODEL_VIEW_MATRIX = GL20.glGetUniformLocation(currentProgram, "modelViewMatrix");
		RenderedGltfModel.MODEL_VIEW_MATRIX_INVERSE = GL20.glGetUniformLocation(currentProgram, "modelViewMatrixInverse");
		RenderedGltfModel.NORMAL_MATRIX = GL20.glGetUniformLocation(currentProgram, "normalMatrix");
		
		Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
		projectionMatrix.get(RenderedGltfModel.BUF_FLOAT_16);
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(currentProgram, "projectionMatrix"), false, RenderedGltfModel.BUF_FLOAT_16);
		(new Matrix4f(projectionMatrix)).invert().get(RenderedGltfModel.BUF_FLOAT_16);
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(currentProgram, "projectionMatrixInverse"), false, RenderedGltfModel.BUF_FLOAT_16);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		int currentTexture3 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		
		shaderModRenderCommands.forEach(Runnable::run);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture3);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
		
		if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL30.glBindVertexArray(currentVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
		
		BufferUploader.reset();
		
		RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
	}

}
