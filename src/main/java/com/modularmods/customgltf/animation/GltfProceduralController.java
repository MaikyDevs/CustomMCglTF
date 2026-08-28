package com.modularmods.customgltf.animation;

import java.util.HashMap;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.modularmods.customgltf.RenderedGltfModel;
import com.modularmods.customgltf.api.GltfNodeAttachment;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.NodeModel;

/**
 * Controller for procedural, code-driven bone manipulation.
 * Allows rotating turrets, aiming barrels, turning vehicle wheels/steering wheels,
 * and animating clock hands and speedometer needles via code.
 */
public class GltfProceduralController {

	private static class TransformOverride {
		float[] translation;
		float[] rotation;
		float[] scale;
		
		float[] initialTranslation;
		float[] initialRotation;
		float[] initialScale;
	}

	private final GltfModel gltfModel;
	private final Map<String, TransformOverride> overrides = new HashMap<>();

	public GltfProceduralController(RenderedGltfModel renderedModel) {
		this(renderedModel != null ? renderedModel.gltfModel : null);
	}

	public GltfProceduralController(GltfModel gltfModel) {
		this.gltfModel = gltfModel;
	}

	private TransformOverride getOrCreateOverride(String nodeName) {
		TransformOverride override = overrides.get(nodeName);
		if (override == null) {
			override = new TransformOverride();
			NodeModel node = GltfNodeAttachment.findNode(gltfModel, nodeName);
			if (node != null) {
				if (node.getTranslation() != null) {
					override.initialTranslation = node.getTranslation().clone();
				}
				if (node.getRotation() != null) {
					override.initialRotation = node.getRotation().clone();
				}
				if (node.getScale() != null) {
					override.initialScale = node.getScale().clone();
				}
			}
			overrides.put(nodeName, override);
		}
		return override;
	}

	/**
	 * Sets the rotation override for a named bone using a Quaternion.
	 */
	public GltfProceduralController setNodeRotation(String nodeName, Quaternionf rotation) {
		if (rotation == null) return this;
		TransformOverride override = getOrCreateOverride(nodeName);
		override.rotation = new float[]{rotation.x, rotation.y, rotation.z, rotation.w};
		return this;
	}

	/**
	 * Sets the rotation override for a named bone using Euler angles in radians (Pitch, Yaw, Roll / X, Y, Z).
	 */
	public GltfProceduralController setNodeRotation(String nodeName, float pitchRad, float yawRad, float rollRad) {
		Quaternionf quat = new Quaternionf().rotationXYZ(pitchRad, yawRad, rollRad);
		return setNodeRotation(nodeName, quat);
	}

	/**
	 * Sets the rotation override for a named bone using Euler angles in degrees.
	 */
	public GltfProceduralController setNodeRotationDegrees(String nodeName, float pitchDeg, float yawDeg, float rollDeg) {
		return setNodeRotation(nodeName, (float) Math.toRadians(pitchDeg), (float) Math.toRadians(yawDeg), (float) Math.toRadians(rollDeg));
	}

	/**
	 * Sets the translation override for a named bone.
	 */
	public GltfProceduralController setNodeTranslation(String nodeName, float x, float y, float z) {
		TransformOverride override = getOrCreateOverride(nodeName);
		override.translation = new float[]{x, y, z};
		return this;
	}

	/**
	 * Sets the translation override for a named bone.
	 */
	public GltfProceduralController setNodeTranslation(String nodeName, Vector3f offset) {
		if (offset == null) return this;
		return setNodeTranslation(nodeName, offset.x, offset.y, offset.z);
	}

	/**
	 * Sets the scale override for a named bone.
	 */
	public GltfProceduralController setNodeScale(String nodeName, float scaleX, float scaleY, float scaleZ) {
		TransformOverride override = getOrCreateOverride(nodeName);
		override.scale = new float[]{scaleX, scaleY, scaleZ};
		return this;
	}

	/**
	 * Sets uniform scale override for a named bone.
	 */
	public GltfProceduralController setNodeScale(String nodeName, float uniformScale) {
		return setNodeScale(nodeName, uniformScale, uniformScale, uniformScale);
	}

	/**
	 * Applies all active transform overrides to the glTF model nodes.
	 * Call this before rendering when you have updated bone transforms.
	 */
	public void applyOverrides() {
		for (Map.Entry<String, TransformOverride> entry : overrides.entrySet()) {
			NodeModel node = GltfNodeAttachment.findNode(gltfModel, entry.getKey());
			if (node != null) {
				TransformOverride override = entry.getValue();
				if (override.translation != null) {
					node.setTranslation(override.translation);
				}
				if (override.rotation != null) {
					node.setRotation(override.rotation);
				}
				if (override.scale != null) {
					node.setScale(override.scale);
				}
			}
		}
	}

	/**
	 * Resets a specific node back to its original glTF transform.
	 */
	public void resetNode(String nodeName) {
		TransformOverride override = overrides.remove(nodeName);
		if (override != null) {
			NodeModel node = GltfNodeAttachment.findNode(gltfModel, nodeName);
			if (node != null) {
				if (override.initialTranslation != null) {
					node.setTranslation(override.initialTranslation);
				}
				if (override.initialRotation != null) {
					node.setRotation(override.initialRotation);
				}
				if (override.initialScale != null) {
					node.setScale(override.initialScale);
				}
			}
		}
	}

	/**
	 * Clears all node overrides and restores original transforms.
	 */
	public void clearOverrides() {
		for (Map.Entry<String, TransformOverride> entry : overrides.entrySet()) {
			NodeModel node = GltfNodeAttachment.findNode(gltfModel, entry.getKey());
			if (node != null) {
				TransformOverride override = entry.getValue();
				if (override.initialTranslation != null) {
					node.setTranslation(override.initialTranslation);
				}
				if (override.initialRotation != null) {
					node.setRotation(override.initialRotation);
				}
				if (override.initialScale != null) {
					node.setScale(override.initialScale);
				}
			}
		}
		overrides.clear();
	}
}
