package com.modularmods.customgltf.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.modularmods.customgltf.iris.IrisRenderingHook;

import net.minecraft.client.renderer.RenderStateShard;

@Mixin(RenderStateShard.class)
public class MixinRenderStateShard {

	@Inject(method = "setupRenderState()V", at = @At("TAIL"))
	private void afterSetupRenderState(CallbackInfo ci) {
		IrisRenderingHook.irisHookAfterSetupRenderState((RenderStateShard)(Object)this);
	}
}
