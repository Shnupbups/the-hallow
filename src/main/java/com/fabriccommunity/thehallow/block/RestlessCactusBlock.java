package com.fabriccommunity.thehallow.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import com.fabriccommunity.thehallow.entity.RestlessCactusEntity;
import com.fabriccommunity.thehallow.registry.HallowedBlocks;
import com.fabriccommunity.thehallow.registry.HallowedEntities;

import java.util.Iterator;
import java.util.Random;

public class RestlessCactusBlock extends Block {
	public static final IntProperty AGE = Properties.AGE_15;
	protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
	protected static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
	
	public RestlessCactusBlock(Block.Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!state.canPlaceAt(world, pos)) {
			world.breakBlock(pos, true);
		} else {
			BlockPos pos2 = pos.up();
			if (world.isAir(pos2)) {
				int height = 1;
				while (world.getBlockState(pos.down(height)).getBlock() == this) {
					height++;
				}
				
				if (height < 3) {
					int age = state.get(AGE);
					if (age == 15) {
						world.setBlockState(pos2, this.getDefaultState());
						BlockState state2 = state.with(AGE, 0);
						world.setBlockState(pos, state2, 4);
						state2.neighborUpdate(world, pos2, this, pos, false);
					} else {
						world.setBlockState(pos, state.with(AGE, age + 1), 4);
					}
					
				}
			}
			if (world.getBlockState(pos.down(1)).getBlock() == HallowedBlocks.TAINTED_SAND && random.nextInt(2) == 0) {
				int height = 0;
				int age = 0;
				for (height = 0; world.getBlockState(pos.up(height)).getBlock() == this; height++) {
					age = world.getBlockState(pos.up(height)).get(AGE);
					world.setBlockState(pos.up(height), Blocks.AIR.getDefaultState());
				}
				
				RestlessCactusEntity entity = new RestlessCactusEntity(HallowedEntities.RESTLESS_CACTUS, world);
				entity.setPosition(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
				entity.setCactusHeight(height);
				entity.age = age;
				world.spawnEntity(entity);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
		return COLLISION_SHAPE;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
		return OUTLINE_SHAPE;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState state2, IWorld world, BlockPos pos, BlockPos pos2) {
		if (!state.canPlaceAt(world, pos)) {
			world.getBlockTickScheduler().schedule(pos, this, 1);
		}
		
		return super.getStateForNeighborUpdate(state, direction, state2, world, pos, pos2);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Iterator<Direction> iterator = Direction.Type.HORIZONTAL.iterator();
		
		Direction direction;
		Material material;
		do {
			if (!iterator.hasNext()) {
				Block block = world.getBlockState(pos.down()).getBlock();
				return (block == HallowedBlocks.RESTLESS_CACTUS || block == HallowedBlocks.TAINTED_SAND) && !world.getBlockState(pos.up()).getMaterial().isLiquid();
			}
			
			direction = iterator.next();
			BlockState state2 = world.getBlockState(pos.offset(direction));
			material = state2.getMaterial();
		} while (!material.isSolid() && !world.getFluidState(pos.offset(direction)).matches(FluidTags.LAVA));
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		entity.damage(DamageSource.CACTUS, 1.0F);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean canPlaceAtSide(BlockState state, BlockView view, BlockPos pos, BlockPlacementEnvironment environment) {
		return false;
	}
}
