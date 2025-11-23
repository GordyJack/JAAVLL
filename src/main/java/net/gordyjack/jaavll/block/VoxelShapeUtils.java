package net.gordyjack.jaavll.block;

import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

import java.util.*;

//@SuppressWarnings("unused")
public interface VoxelShapeUtils {
    // Cache: for each facing, map original→rotated shape
     Map<Direction, Map<VoxelShape, VoxelShape>> CACHE = new EnumMap<>(Direction.class);

    /**
     * Returns a rotated copy of 'shape' so it appears as if originally
     * built facing DOWN but is now facing 'facing'.
     */
    static VoxelShape rotateShape(VoxelShape shape, Direction facing) {
        if (facing == Direction.DOWN) return shape;  // no-op
        // get or create inner cache for this facing
        Map<VoxelShape, VoxelShape> inner = CACHE.computeIfAbsent(facing, d -> new IdentityHashMap<>());
        // compute on first request, reuse thereafter
        return inner.computeIfAbsent(shape, s -> computeRotatedShape(s, facing));
    }

    // Core rotation routine: transform each bounding-AABB, union the results
    private static VoxelShape computeRotatedShape(VoxelShape in, Direction facing) {
        VoxelShape result = Shapes.empty();
        for (AABB b : in.toAabbs()) {
            AABB r = rotateAABB(b, facing);
            result = Shapes.or(result,
                    Shapes.create(r.minX, r.minY, r.minZ, r.maxX, r.maxY, r.maxZ));
        }
        // optional: result = VoxelShapes.optimize(result);
        return result;
    }

    // Rotate a single AABB 'b' by transforming its two corner points
    private static AABB rotateAABB(AABB b, Direction f) {
        Vec3 p1 = new Vec3(b.minX, b.minY, b.minZ);
        Vec3 p2 = new Vec3(b.maxX, b.maxY, b.maxZ);
        Vec3 q1 = applyTransforms(p1, f);
        Vec3 q2 = applyTransforms(p2, f);

        double minX = Math.min(q1.x, q2.x);
        double minY = Math.min(q1.y, q2.y);
        double minZ = Math.min(q1.z, q2.z);
        double maxX = Math.max(q1.x, q2.x);
        double maxY = Math.max(q1.y, q2.y);
        double maxZ = Math.max(q1.z, q2.z);
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // Apply the same rotation recipe you use for models:
    // DOWN = none, UP = X180; SOUTH = X90; NORTH = X90→Y180; EAST = X90→Y270; WEST = X90→Y90
    private static Vec3 applyTransforms(Vec3 v, Direction f) {
        return switch (f) {
            case DOWN -> v;
            case UP -> rotateX180(v);
            case NORTH -> rotateX270(v);
            case SOUTH -> rotateY180(rotateX270(v));
            case EAST -> rotateY270(rotateX270(v));
            case WEST -> rotateY90(rotateX270(v));
        };
    }

    // 90° about the X axis:  y→z, z→1–y
    private static Vec3 rotateX90(Vec3 v) {
        return new Vec3(v.x, v.z, 1 - v.y);
    }

    // 180° about the X axis: y→1–y, z→1–z
    private static Vec3 rotateX180(Vec3 v) {
        return new Vec3(v.x, 1 - v.y, 1 - v.z);
    }

    // 270° about the X axis (or 90° CCW): y→1–z, z→y
    private static Vec3 rotateX270(Vec3 v) {
        return new Vec3(v.x, 1 - v.z, v.y);
    }

    // 90° about the Y axis: x→z, z→1–x
    private static Vec3 rotateY90(Vec3 v) {
        return new Vec3(v.z, v.y, 1 - v.x);
    }

    // 180° about the Y axis: x→1–x, z→1–z
    private static Vec3 rotateY180(Vec3 v) {
        return new Vec3(1 - v.x, v.y, 1 - v.z);
    }

    // 270° about the Y axis (or 90° CCW): x→1–z, z→x
    private static Vec3 rotateY270(Vec3 v) {
        return new Vec3(1 - v.z, v.y, v.x);
    }
    
    /**
     * Calculates the number of 90-degree rotations needed based on the direction.
     *
     * @param toDirection the direction to align the shape with after rotation
     * @return the number of times to rotate by 90 degrees
     */
    private int calculateRotationTimes(Direction toDirection) {
        return toDirection.get2DDataValue() + 2 % 4;
    }
    
    /**
     * Performs a single 90-degree rotation on the buffer shapes.
     *
     * @param buffer the array of VoxelShapes where buffer[0] is the shape to rotate and buffer[1] is an empty shape
     * @return the array of VoxelShapes after rotation
     */
    private VoxelShape[] rotateOnce(VoxelShape[] buffer) {
        buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
        
        // Prepare for the next iteration or final result.
        VoxelShape rotatedShape = buffer[1];
        buffer[1] = Shapes.empty();
        
        // Return the updated buffer array.
        return new VoxelShape[]{rotatedShape, buffer[1]};
    }
    
    default VoxelShape flipShape(VoxelShape shape) {
        return flipShape(shape, Direction.Axis.Y);
    }
    default VoxelShape flipShape(VoxelShape shape, Direction.Axis axis) {
        VoxelShape flippedShape = Shapes.empty();
        
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    double minX = x / 16.0;
                    double minY = y / 16.0;
                    double minZ = z / 16.0;
                    double maxX = (x + 1) / 16.0;
                    double maxY = (y + 1) / 16.0;
                    double maxZ = (z + 1) / 16.0;
                    
                    switch (axis) {
                        case X -> {
                            double temp = minX;
                            minX = 1 - maxX;
                            maxX = 1 - temp;
                        }
                        case Y -> {
                            double temp = minY;
                            minY = 1 - maxY;
                            maxY = 1 - temp;
                        }
                        case Z -> {
                            double temp = minZ;
                            minZ = 1 - maxZ;
                            maxZ = 1 - temp;
                        }
                    }
                    
                    if (Shapes.joinIsNotEmpty(shape, Shapes.create(minX, minY, minZ, maxX, maxY, maxZ), BooleanOp.AND)) {
                        flippedShape = Shapes.or(flippedShape, Block.box(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return flippedShape;
    }
    static VoxelShape mergeShapes(VoxelShape shape1, VoxelShape shape2) {
        return Shapes.or(shape1, shape2);
    }
    static VoxelShape intersectShapes(VoxelShape shape1, VoxelShape shape2) {
        return Shapes.join(shape1, shape2, BooleanOp.AND);
    }
    static VoxelShape translateShape(VoxelShape fromShape, Direction direction, int pixels) {
        // Get the bounding AABB of the original shape
        var AABB = fromShape.bounds();
        
        // Calculate the offset in each direction based on the pixels
        double offsetX = 0;
        double offsetY = 0;
        double offsetZ = 0;

        switch (direction) {
            case NORTH -> offsetZ = -pixels;
            case SOUTH -> offsetZ = pixels;
            case EAST -> offsetX = pixels;
            case WEST -> offsetX = -pixels;
            case UP -> offsetY = pixels;
            case DOWN -> offsetY = -pixels;
        }
        
        // Translate the shape by adjusting its bounding AABB with the offset
        return Block.box(
                AABB.minX*16 + offsetX, AABB.minY*16 + offsetY, AABB.minZ*16 + offsetZ,
                AABB.maxX*16 + offsetX, AABB.maxY*16 + offsetY, AABB.maxZ*16 + offsetZ);
    }
}
