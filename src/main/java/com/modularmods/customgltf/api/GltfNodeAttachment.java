package com.modularmods.customgltf.api;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.customgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;

/**
 * Utility for querying and attaching elements to named bones/nodes in glTF models.
 * Allows mounting weapons, attachments, muzzle flashes, seats, particles, and passengers.
 */
public final class GltfNodeAttachment {

	private GltfNodeAttachment() {
		// Utility class
	}

	/**
	 * Finds a NodeModel by name in the given glTF model.
	 *
	 * @param model The rendered glTF model
	 * @param nodeName The name of the node/bone to find
	 * @return The NodeModel if found, or null
	 */
	public static NodeModel findNode(RenderedGltfModel model, String nodeName) {
		if (model == null || model.gltfModel == null || nodeName == null) {
			return null;
		}
		return findNode(model.gltfModel, nodeName);
	}

	/**
	 * Finds a NodeModel by name in a GltfModel.
	 *
	 * @param gltfModel The GltfModel
	 * @param nodeName The name of the node to find
	 * @return The NodeModel if found, or null
	 */
	public static NodeModel findNode(GltfModel gltfModel, String nodeName) {
		if (gltfModel == null || nodeName == null) {
			return null;
		}
		for (NodeModel node : gltfModel.getNodeModels()) {
			if (nodeName.equals(node.getName())) {
				return node;
			}
		}
		for (SceneModel scene : gltfModel.getSceneModels()) {
			for (NodeModel rootNode : scene.getNodeModels()) {
				NodeModel found = searchNodeRecursive(rootNode, nodeName);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static NodeModel searchNodeRecursive(NodeModel current, String targetName) {
		if (targetName.equals(current.getName())) {
			return current;
		}
		for (NodeModel child : current.getChildren()) {
			NodeModel found = searchNodeRecursive(child, targetName);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Gets the global 4x4 transformation matrix for a named node in the model.
	 *
	 * @param model The rendered glTF model
	 * @param nodeName The name of the node
	 * @return A Matrix4f representing the node's global transform in model space, or null if not found
	 */
	public static Matrix4f getNodeGlobalTransform(RenderedGltfModel model, String nodeName) {
		NodeModel node = findNode(model, nodeName);
		if (node == null) {
			return null;
		}
		float[] transform = RenderedGltfModel.findGlobalTransform(node);
		Matrix4f matrix = new Matrix4f();
		matrix.setTransposed(transform);
		return matrix;
	}

	/**
	 * Gets the global 3D position vector of a named node in model space.
	 *
	 * @param model The rendered glTF model
	 * @param nodeName The name of the node
	 * @return A Vector3f containing (x, y, z) position, or null if not found
	 */
	public static Vector3f getNodePosition(RenderedGltfModel model, String nodeName) {
		Matrix4f transform = getNodeGlobalTransform(model, nodeName);
		if (transform == null) {
			return null;
		}
		Vector3f pos = new Vector3f();
		transform.getTranslation(pos);
		return pos;
	}

	/**
	 * Gets the global rotation quaternion of a named node in model space.
	 *
	 * @param model The rendered glTF model
	 * @param nodeName The name of the node
	 * @return A Quaternionf containing the rotation, or null if not found
	 */
	public static Quaternionf getNodeRotation(RenderedGltfModel model, String nodeName) {
		Matrix4f transform = getNodeGlobalTransform(model, nodeName);
		if (transform == null) {
			return null;
		}
		Quaternionf rotation = new Quaternionf();
		transform.getNormalizedRotation(rotation);
		return rotation;
	}

	/**
	 * Multiplies the given Minecraft PoseStack with the global transform of the named node.
	 * This makes subsequent rendering commands automatically positioned, rotated, and scaled
	 * at the socket bone's location (e.g. mounting a scope or rendering a muzzle flash).
	 *
	 * @param model The rendered glTF model
	 * @param nodeName The socket/bone node name
	 * @param targetPoseStack The PoseStack to transform
	 * @return true if the node was found and transform applied, false otherwise
	 */
	public static boolean applyNodeTransform(RenderedGltfModel model, String nodeName, PoseStack targetPoseStack) {
		Matrix4f transform = getNodeGlobalTransform(model, nodeName);
		if (transform == null || targetPoseStack == null) {
			return false;
		}
		targetPoseStack.mulPoseMatrix(transform);
		return true;
	}
}
