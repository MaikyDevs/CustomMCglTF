package com.modularmods.customgltf;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;

public class RenderedGltfSceneGL40 extends RenderedGltfScene {

	@Override
	protected void performSkinning() {
		if(!skinningCommands.isEmpty()) {
			GL20.glUseProgram(CustomGLTF.getInstance().getGlProgramSkinnig());
			GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
			skinningCommands.forEach(Runnable::run);
			GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
			GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
			GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
		}
	}

}
