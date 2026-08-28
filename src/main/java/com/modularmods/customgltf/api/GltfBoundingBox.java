package com.modularmods.customgltf.api;

import net.minecraft.world.phys.AABB;
import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorFloatData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;

/**
 * Utility for computing 3D Axis-Aligned Bounding Boxes (AABB) from glTF models.
 * Useful for entity hitboxes, block selection boxes, raycasting, and frustum culling.
 */
public final class GltfBoundingBox {

	private GltfBoundingBox() {
		// Utility class
	}

	/**
	 * Computes the overall Axis-Aligned Bounding Box (AABB) of the entire glTF model.
	 *
	 * @param gltfModel The glTF model
	 * @return An AABB enclosing all mesh primitives in the model
	 */
	public static AABB computeModelBounds(GltfModel gltfModel) {
		if (gltfModel == null || gltfModel.getMeshModels().isEmpty()) {
			return new AABB(0, 0, 0, 0, 0, 0);
		}

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (MeshModel mesh : gltfModel.getMeshModels()) {
			for (MeshPrimitiveModel primitive : mesh.getMeshPrimitiveModels()) {
				AccessorModel posAccessor = primitive.getAttributes().get("POSITION");
				if (posAccessor != null) {
					Number[] min = posAccessor.getMin();
					Number[] max = posAccessor.getMax();
					if (min != null && max != null && min.length >= 3 && max.length >= 3) {
						minX = Math.min(minX, min[0].doubleValue());
						minY = Math.min(minY, min[1].doubleValue());
						minZ = Math.min(minZ, min[2].doubleValue());
						maxX = Math.max(maxX, max[0].doubleValue());
						maxY = Math.max(maxY, max[1].doubleValue());
						maxZ = Math.max(maxZ, max[2].doubleValue());
					} else {
						AccessorData data = posAccessor.getAccessorData();
						if (data instanceof AccessorFloatData) {
							AccessorFloatData floatData = (AccessorFloatData) data;
							int count = posAccessor.getCount();
							for (int i = 0; i < count; i++) {
								float x = floatData.get(i, 0);
								float y = floatData.get(i, 1);
								float z = floatData.get(i, 2);
								minX = Math.min(minX, x);
								minY = Math.min(minY, y);
								minZ = Math.min(minZ, z);
								maxX = Math.max(maxX, x);
								maxY = Math.max(maxY, y);
								maxZ = Math.max(maxZ, z);
							}
						}
					}
				}
			}
		}

		if (Double.isInfinite(minX) || Double.isInfinite(maxX)) {
			return new AABB(-0.5, 0, -0.5, 0.5, 1.0, 0.5);
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	/**
	 * Computes the bounding box for a specific node and its meshes.
	 *
	 * @param nodeModel The NodeModel
	 * @return An AABB enclosing the node's mesh primitives
	 */
	public static AABB computeNodeBounds(NodeModel nodeModel) {
		if (nodeModel == null || nodeModel.getMeshModels().isEmpty()) {
			return new AABB(0, 0, 0, 0, 0, 0);
		}

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (MeshModel mesh : nodeModel.getMeshModels()) {
			for (MeshPrimitiveModel primitive : mesh.getMeshPrimitiveModels()) {
				AccessorModel posAccessor = primitive.getAttributes().get("POSITION");
				if (posAccessor != null) {
					Number[] min = posAccessor.getMin();
					Number[] max = posAccessor.getMax();
					if (min != null && max != null && min.length >= 3 && max.length >= 3) {
						minX = Math.min(minX, min[0].doubleValue());
						minY = Math.min(minY, min[1].doubleValue());
						minZ = Math.min(minZ, min[2].doubleValue());
						maxX = Math.max(maxX, max[0].doubleValue());
						maxY = Math.max(maxY, max[1].doubleValue());
						maxZ = Math.max(maxZ, max[2].doubleValue());
					}
				}
			}
		}

		if (Double.isInfinite(minX) || Double.isInfinite(maxX)) {
			return new AABB(0, 0, 0, 0, 0, 0);
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
