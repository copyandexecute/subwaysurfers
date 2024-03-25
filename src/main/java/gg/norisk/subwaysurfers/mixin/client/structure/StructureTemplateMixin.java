package gg.norisk.subwaysurfers.mixin.client.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import gg.norisk.subwaysurfers.client.lifecycle.ClientGameRunningLifeCycle;
import gg.norisk.subwaysurfers.client.structure.ClientStructureTemplate;
import gg.norisk.subwaysurfers.entity.OriginMarker;
import gg.norisk.subwaysurfers.entity.UUIDMarker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.WorldAccess;
import net.silkmc.silk.core.world.block.BlockInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin implements ClientStructureTemplate {

    @Shadow
    @Final
    private List<StructureTemplate.PalettedBlockInfoList> blockInfoLists;

    @Shadow
    @Final
    private List<StructureTemplate.StructureEntityInfo> entities;

    @Shadow
    private Vec3i size;

    @Shadow
    public static void updateCorner(WorldAccess worldAccess, int i, VoxelSet voxelSet, int j, int k, int l) {
    }

    @Unique
    @Override
    public void tick(@NotNull PlayerEntity player) {
    }

    @Unique
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();

    @Unique
    private void spawnEntitiesClient(
            ClientWorld world,
            BlockPos blockPos,
            BlockMirror blockMirror,
            BlockRotation blockRotation,
            BlockPos blockPos2,
            @Nullable BlockBox blockBox,
            boolean bl,
            @Nullable Set<Entity> entities
    ) {
        for (StructureTemplate.StructureEntityInfo structureEntityInfo : this.entities) {
            BlockPos blockPos3 = transformAround(structureEntityInfo.blockPos, blockMirror, blockRotation, blockPos2).add(blockPos);
            if (blockBox == null || blockBox.contains(blockPos3)) {
                NbtCompound nbtCompound = structureEntityInfo.nbt.copy();
                Vec3d vec3d = transformAround(structureEntityInfo.pos, blockMirror, blockRotation, blockPos2);
                Vec3d vec3d2 = vec3d.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                NbtList nbtList = new NbtList();
                nbtList.add(NbtDouble.of(vec3d2.x));
                nbtList.add(NbtDouble.of(vec3d2.y));
                nbtList.add(NbtDouble.of(vec3d2.z));
                nbtCompound.put("Pos", nbtList);
                nbtCompound.remove("UUID");
                EntityType.getEntityFromNbt(nbtCompound, world).ifPresent(entity -> {
                    float f = entity.applyRotation(blockRotation);
                    f += entity.applyMirror(blockMirror) - entity.getYaw();
                    entity.refreshPositionAndAngles(vec3d2.x, vec3d2.y, vec3d2.z, f, entity.getPitch());
                    if (entity instanceof UUIDMarker uuidMarker) {
                        //TODO this is fine for now ig
                        if (MinecraftClient.getInstance().player != null) {
                            uuidMarker.setOwner(MinecraftClient.getInstance().player.getUuid());
                        }
                    }
                    if (entity instanceof OriginMarker originMarker) {
                        originMarker.setOrigin(entity.getBlockPos());
                    }

                    if (entities != null) {
                        entities.add(entity);
                    } else {
                        world.addEntity(entity);
                        entity.streamSelfAndPassengers().forEach(world::spawnEntity);
                    }
                });
            }
        }
    }

    @Shadow
    public static BlockPos transform(StructurePlacementData structurePlacementData, BlockPos blockPos) {
        return null;
    }

    @Shadow
    public static BlockPos transformAround(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos blockPos2) {
        return null;
    }

    @Shadow
    public static Vec3d transformAround(Vec3d vec3d, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos blockPos) {
        return null;
    }

    @Unique
    private static List<StructureTemplate.StructureBlockInfo> processClient(
            ClientWorld serverWorldAccess,
            BlockPos blockPos,
            BlockPos blockPos2,
            StructurePlacementData structurePlacementData,
            List<StructureTemplate.StructureBlockInfo> list
    ) {
        List<StructureTemplate.StructureBlockInfo> list2 = new ArrayList<>();
        List<StructureTemplate.StructureBlockInfo> list3 = new ArrayList<>();

        for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            BlockPos blockPos3 = transform(structurePlacementData, structureBlockInfo.comp_1341()).add(blockPos);
            StructureTemplate.StructureBlockInfo structureBlockInfo2 = new StructureTemplate.StructureBlockInfo(
                    blockPos3, structureBlockInfo.comp_1342(), structureBlockInfo.comp_1343() != null ? structureBlockInfo.comp_1343().copy() : null
            );
            Iterator<StructureProcessor> iterator = structurePlacementData.getProcessors().iterator();

            while (structureBlockInfo2 != null && iterator.hasNext()) {
                structureBlockInfo2 = iterator.next().process(serverWorldAccess, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlacementData);
            }

            if (structureBlockInfo2 != null) {
                list3.add(structureBlockInfo2);
                list2.add(structureBlockInfo);
            }
        }
        return list3;
    }

    @Unique
    public boolean placeClient(
            @NotNull ClientWorld world,
            @NotNull BlockPos blockPos, @NotNull
            BlockPos blockPos2,
            @NotNull StructurePlacementData structurePlacementData,
            @NotNull Random random, int i,
            boolean ignoreAir,
            @Nullable Map<BlockPos, BlockState> blocks, @Nullable Set<Entity> entities) {
        if (this.blockInfoLists.isEmpty()) {
            return false;
        } else {
            List<StructureTemplate.StructureBlockInfo> list = structurePlacementData.getRandomBlockInfos(this.blockInfoLists, blockPos).getAll();
            if ((!list.isEmpty() || !structurePlacementData.shouldIgnoreEntities() && !this.entities.isEmpty())
                    && this.size.getX() >= 1
                    && this.size.getY() >= 1
                    && this.size.getZ() >= 1) {
                BlockBox blockBox = structurePlacementData.getBoundingBox();
                List<BlockPos> list2 = Lists.newArrayListWithCapacity(structurePlacementData.shouldPlaceFluids() ? list.size() : 0);
                List<BlockPos> list3 = Lists.newArrayListWithCapacity(structurePlacementData.shouldPlaceFluids() ? list.size() : 0);
                List<Pair<BlockPos, NbtCompound>> list4 = Lists.newArrayListWithCapacity(list.size());
                int j = Integer.MAX_VALUE;
                int k = Integer.MAX_VALUE;
                int l = Integer.MAX_VALUE;
                int m = Integer.MIN_VALUE;
                int n = Integer.MIN_VALUE;
                int o = Integer.MIN_VALUE;

                for (StructureTemplate.StructureBlockInfo structureBlockInfo : processClient(world, blockPos, blockPos2, structurePlacementData, list)) {
                    BlockPos blockPos3 = structureBlockInfo.comp_1341();
                    if (blockBox == null || blockBox.contains(blockPos3)) {
                        FluidState fluidState = structurePlacementData.shouldPlaceFluids() ? world.getFluidState(blockPos3) : null;
                        BlockState blockState = structureBlockInfo.comp_1342().mirror(structurePlacementData.getMirror()).rotate(structurePlacementData.getRotation());
                        if (structureBlockInfo.comp_1343() != null) {
                            BlockEntity blockEntity = world.getBlockEntity(blockPos3);
                            Clearable.clear(blockEntity);
                            //world.setBlockState(blockPos3, Blocks.BARRIER.getDefaultState(), 20);
                            //ClientGameStartLifeCycle.INSTANCE.getFakeBlocks().add(new BlockInfo(Blocks.AIR.getDefaultState(), blockPos3));
                        }

                        if (blockState.isAir() && ignoreAir) {
                            continue;
                        }

                        if (blocks != null) {
                            blocks.put(blockPos3, blockState);
                        } else if (world.setBlockState(blockPos3, blockState, i)) {
                            ClientGameRunningLifeCycle.INSTANCE.getFakeBlocks().add(new BlockInfo(Blocks.AIR.getDefaultState(), blockPos3));
                            j = Math.min(j, blockPos3.getX());
                            k = Math.min(k, blockPos3.getY());
                            l = Math.min(l, blockPos3.getZ());
                            m = Math.max(m, blockPos3.getX());
                            n = Math.max(n, blockPos3.getY());
                            o = Math.max(o, blockPos3.getZ());
                            list4.add(Pair.of(blockPos3, structureBlockInfo.comp_1343()));
                            if (structureBlockInfo.comp_1343() != null) {
                                BlockEntity blockEntity = world.getBlockEntity(blockPos3);
                                if (blockEntity != null) {
                                    if (blockEntity instanceof LootableInventory) {
                                        structureBlockInfo.comp_1343().putLong("LootTableSeed", random.nextLong());
                                    }

                                    blockEntity.readNbt(structureBlockInfo.comp_1343());
                                }
                            }

                            if (fluidState != null) {
                                if (blockState.getFluidState().isStill()) {
                                    list3.add(blockPos3);
                                } else if (blockState.getBlock() instanceof FluidFillable) {
                                    ((FluidFillable) blockState.getBlock()).tryFillWithFluid(world, blockPos3, blockState, fluidState);
                                    if (!fluidState.isStill()) {
                                        list2.add(blockPos3);
                                    }
                                }
                            }
                        }


                    }
                }

                boolean bl = true;
                Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while (bl && !list2.isEmpty()) {
                    bl = false;
                    Iterator<BlockPos> iterator = list2.iterator();

                    while (iterator.hasNext()) {
                        BlockPos blockPos4 = iterator.next();
                        FluidState fluidState2 = world.getFluidState(blockPos4);

                        for (int p = 0; p < directions.length && !fluidState2.isStill(); ++p) {
                            BlockPos blockPos5 = blockPos4.offset(directions[p]);
                            FluidState fluidState3 = world.getFluidState(blockPos5);
                            if (fluidState3.isStill() && !list3.contains(blockPos5)) {
                                fluidState2 = fluidState3;
                            }
                        }

                        if (fluidState2.isStill()) {
                            BlockState blockState2 = world.getBlockState(blockPos4);
                            Block block = blockState2.getBlock();
                            if (block instanceof FluidFillable) {
                                ((FluidFillable) block).tryFillWithFluid(world, blockPos4, blockState2, fluidState2);
                                bl = true;
                                iterator.remove();
                            }
                        }
                    }
                }

                if (j <= m) {
                    if (!structurePlacementData.shouldUpdateNeighbors()) {
                        VoxelSet voxelSet = new BitSetVoxelSet(m - j + 1, n - k + 1, o - l + 1);

                        for (Pair<BlockPos, NbtCompound> pair : list4) {
                            BlockPos blockPos6 = pair.getFirst();
                            voxelSet.set(blockPos6.getX() - j, blockPos6.getY() - k, blockPos6.getZ() - l);
                        }

                        updateCorner(world, i, voxelSet, j, k, l);
                    }

                    for (Pair<BlockPos, NbtCompound> pair2 : list4) {
                        BlockPos blockPos7 = pair2.getFirst();
                        if (!structurePlacementData.shouldUpdateNeighbors()) {
                            BlockState blockState2 = world.getBlockState(blockPos7);
                            BlockState blockState3 = Block.postProcessState(blockState2, world, blockPos7);
                            if (blockState2 != blockState3) {
                                //world.setBlockState(blockPos7, blockState3, i & -2 | 16);
                                //ClientGameStartLifeCycle.INSTANCE.getFakeBlocks().add(new BlockInfo(Blocks.AIR.getDefaultState(), blockPos7));
                            }

                            world.updateNeighbors(blockPos7, blockState3.getBlock());
                        }

                        if (pair2.getSecond() != null) {
                            BlockEntity blockEntity = world.getBlockEntity(blockPos7);
                            if (blockEntity != null) {
                                blockEntity.markDirty();
                            }
                        }
                    }
                }

                if (!structurePlacementData.shouldIgnoreEntities()) {
                    this.spawnEntitiesClient(
                            world,
                            blockPos,
                            structurePlacementData.getMirror(),
                            structurePlacementData.getRotation(),
                            structurePlacementData.getPosition(),
                            blockBox,
                            structurePlacementData.shouldInitializeMobs(),
                            entities
                    );
                }

                return true;
            } else {
                return false;
            }
        }
    }
}
