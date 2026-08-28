package com.modularmods.customgltf.animation;

public abstract class StepInterpolatedChannel extends InterpolatedChannel {

	/**
	 * The values. Each element of this array corresponds to one key
	 * frame time
	 */
	protected final float[][] values;
	
	public StepInterpolatedChannel(float[] timesS, float[][] values) {
		super(timesS);
		this.values = values;
	}
	
	@Override
	public void update(float timeS) {
		if(timesS == null || timesS.length == 0) return;
		float[] output = getListener();
		System.arraycopy(values[computeIndex(timeS, timesS)], 0, output, 0, output.length);
	}

}
