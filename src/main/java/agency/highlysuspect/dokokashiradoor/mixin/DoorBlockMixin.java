package agency.highlysuspect.dokokashiradoor.mixin;

import agency.highlysuspect.dokokashiradoor.gateway.GatewayPersistentState;
import agency.highlysuspect.dokokashiradoor.tp.ClientDoorTp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public class DoorBlockMixin extends Block {
	public DoorBlockMixin(Settings settings) {
		super(settings);
		throw new AssertionError("Dummy constructor");
	}
	
	@Inject(
		method = "onUse",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/BlockState;cycle(Lnet/minecraft/state/property/Property;)Ljava/lang/Object;"
		),
		cancellable = true
	)
	private void whenUsed(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		if(world.isClient() && player != null && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER && !state.get(DoorBlock.OPEN)) {
			boolean worked = ClientDoorTp.playerUseDoorClient(world, pos, state, player);
			if(worked) {
				cir.setReturnValue(ActionResult.success(true));
			}
		}
		
		//if(!world.isClient()) cir.setReturnValue(ActionResult.success(false));
	}
	
	@Inject(
		method = "neighborUpdate",
		at = @At("HEAD")
	)
	private void whenNeighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify, CallbackInfo ci) {
		if(world instanceof ServerWorld sworld && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
			GatewayPersistentState.getFor(sworld).helloDoor(sworld, pos.toImmutable());
		}
	}
	
	@Override
	//@SoftOverride (mixin blows up if i have this annotation, but it's true)
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		
		if(world instanceof ServerWorld sworld && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
			GatewayPersistentState.getFor(sworld).helloDoor(sworld, pos.toImmutable());
		}
	}
}
