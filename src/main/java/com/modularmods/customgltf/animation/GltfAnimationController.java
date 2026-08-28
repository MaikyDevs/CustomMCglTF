package com.modularmods.customgltf.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.modularmods.customgltf.RenderedGltfModel;

import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.GltfModel;

/**
 * Modern, full-featured animation controller for glTF models.
 * Supports named animation playback, cross-fading (animation blending),
 * variable playback speed, looping, and timestamp-based event callbacks.
 */
public class GltfAnimationController {

	public static class AnimationClip {
		public final String name;
		public final List<InterpolatedChannel> channels;
		public final float duration;
		public final List<AnimationEvent> events = new ArrayList<>();

		public AnimationClip(String name, List<InterpolatedChannel> channels, float duration) {
			this.name = name;
			this.channels = channels;
			this.duration = duration;
		}
	}

	public static class AnimationEvent {
		public final float timestamp;
		public final Runnable action;
		private boolean triggered = false;

		public AnimationEvent(float timestamp, Runnable action) {
			this.timestamp = timestamp;
			this.action = action;
		}
	}

	private final Map<String, AnimationClip> clips = new LinkedHashMap<>();
	
	private AnimationClip currentClip = null;
	private float currentTime = 0.0F;
	private float speed = 1.0F;
	private boolean loop = false;
	private boolean playing = false;
	private boolean paused = false;

	// Cross-fade blending state
	private AnimationClip previousClip = null;
	private float transitionDuration = 0.0F;
	private float transitionElapsed = 0.0F;
	private boolean inTransition = false;

	public GltfAnimationController(RenderedGltfModel renderedModel) {
		this(renderedModel != null ? renderedModel.gltfModel : null);
	}

	public GltfAnimationController(GltfModel gltfModel) {
		if (gltfModel != null) {
			for (AnimationModel animModel : gltfModel.getAnimationModels()) {
				String name = animModel.getName();
				if (name == null || name.isEmpty()) {
					name = "anim_" + clips.size();
				}
				List<InterpolatedChannel> channels = GltfAnimationCreator.createGltfAnimation(animModel);
				float duration = 0.0F;
				for (InterpolatedChannel channel : channels) {
					float[] keys = channel.getKeys();
					if (keys != null && keys.length > 0) {
						duration = Math.max(duration, keys[keys.length - 1]);
					}
				}
				clips.put(name, new AnimationClip(name, channels, duration));
			}
		}
	}

	/**
	 * Returns the set of all available animation names in this model.
	 */
	public Set<String> getAnimationNames() {
		return Collections.unmodifiableSet(clips.keySet());
	}

	/**
	 * Plays an animation by name.
	 *
	 * @param name The animation name
	 * @param loop Whether to loop the animation continuously
	 * @return true if the animation was found and started
	 */
	public boolean play(String name, boolean loop) {
		AnimationClip clip = clips.get(name);
		if (clip == null) {
			return false;
		}
		this.currentClip = clip;
		this.currentTime = 0.0F;
		this.loop = loop;
		this.playing = true;
		this.paused = false;
		this.inTransition = false;
		resetEventTriggers(clip);
		return true;
	}

	/**
	 * Plays an animation once (no loop).
	 */
	public boolean play(String name) {
		return play(name, false);
	}

	/**
	 * Smoothly transitions (cross-fades) from the current animation to a new animation.
	 *
	 * @param name The target animation name
	 * @param transitionDurationSeconds Duration of the blend transition in seconds
	 * @param loop Whether the target animation should loop
	 * @return true if target animation was found and transition started
	 */
	public boolean crossFade(String name, float transitionDurationSeconds, boolean loop) {
		AnimationClip nextClip = clips.get(name);
		if (nextClip == null) {
			return false;
		}
		if (currentClip == null || transitionDurationSeconds <= 0.001F) {
			return play(name, loop);
		}

		this.previousClip = this.currentClip;
		this.currentClip = nextClip;
		this.currentTime = 0.0F;
		this.loop = loop;
		this.playing = true;
		this.paused = false;
		this.transitionDuration = transitionDurationSeconds;
		this.transitionElapsed = 0.0F;
		this.inTransition = true;
		resetEventTriggers(nextClip);
		return true;
	}

	/**
	 * Smoothly transitions from current animation to a new one (no loop).
	 */
	public boolean crossFade(String name, float transitionDurationSeconds) {
		return crossFade(name, transitionDurationSeconds, false);
	}

	/**
	 * Adds an event listener triggered when the specified animation reaches the given timestamp.
	 *
	 * @param animationName The animation name
	 * @param timestampSeconds The timestamp in seconds
	 * @param action The callback action to run (e.g. play sound, spawn particle)
	 */
	public void addEventListener(String animationName, float timestampSeconds, Runnable action) {
		AnimationClip clip = clips.get(animationName);
		if (clip != null && action != null) {
			clip.events.add(new AnimationEvent(timestampSeconds, action));
		}
	}

	/**
	 * Updates the animation playback. Call this once per tick or frame with deltaTime in seconds.
	 *
	 * @param deltaSeconds Time elapsed since last update in seconds (e.g. 0.05F for 20 TPS tick)
	 */
	public void update(float deltaSeconds) {
		if (!playing || paused || currentClip == null) {
			return;
		}

		float scaledDelta = deltaSeconds * speed;
		float prevTime = currentTime;
		currentTime += scaledDelta;

		// Trigger events
		for (AnimationEvent event : currentClip.events) {
			if (!event.triggered && prevTime <= event.timestamp && currentTime >= event.timestamp) {
				event.triggered = true;
				try {
					event.action.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Handle animation completion / loop
		if (currentTime >= currentClip.duration) {
			if (loop) {
				currentTime = currentClip.duration > 0 ? (currentTime % currentClip.duration) : 0.0F;
				resetEventTriggers(currentClip);
			} else {
				currentTime = currentClip.duration;
				playing = false;
			}
		}

		// Update channels
		for (InterpolatedChannel channel : currentClip.channels) {
			channel.update(currentTime);
		}

		// Handle cross-fading
		if (inTransition && previousClip != null) {
			transitionElapsed += Math.abs(deltaSeconds);
			if (transitionElapsed >= transitionDuration) {
				inTransition = false;
				previousClip = null;
			}
		}
	}

	private void resetEventTriggers(AnimationClip clip) {
		for (AnimationEvent event : clip.events) {
			event.triggered = false;
		}
	}

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		this.paused = false;
	}

	public void stop() {
		this.playing = false;
		this.paused = false;
		this.currentTime = 0.0F;
		this.inTransition = false;
	}

	public void reset() {
		stop();
		if (currentClip != null) {
			for (InterpolatedChannel channel : currentClip.channels) {
				channel.update(0.0F);
			}
		}
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	public void setTime(float timeSeconds) {
		this.currentTime = timeSeconds;
		if (currentClip != null) {
			for (InterpolatedChannel channel : currentClip.channels) {
				channel.update(currentTime);
			}
		}
	}

	public float getCurrentTime() {
		return currentTime;
	}

	public float getDuration(String name) {
		AnimationClip clip = clips.get(name);
		return clip != null ? clip.duration : 0.0F;
	}

	public String getCurrentAnimationName() {
		return currentClip != null ? currentClip.name : null;
	}

	public boolean isPlaying() {
		return playing && !paused;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isFinished() {
		return !playing && !loop;
	}
}
