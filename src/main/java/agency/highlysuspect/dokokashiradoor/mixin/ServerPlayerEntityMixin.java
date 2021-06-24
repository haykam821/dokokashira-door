package agency.highlysuspect.dokokashiradoor.mixin;

import agency.highlysuspect.dokokashiradoor.tp.DokoServerPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(method = "moveToWorld", at = @At("HEAD"))
	public void whenMovingToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		DokoServerPlayNetworkHandler.getFor((ServerPlayerEntity) (Object) this).onDimensionChange(destination);
	}
}
