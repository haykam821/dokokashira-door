package agency.highlysuspect.dokokashiradoor.tp;

import agency.highlysuspect.dokokashiradoor.gateway.Gateway;
import agency.highlysuspect.dokokashiradoor.gateway.GatewayPersistentState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class ServerDoorTp {
	public static void confirmDoorTeleport(BlockPos leftFromPos, BlockPos destPos, ServerPlayerEntity player) {
		Vec3d oldPos = player.getPos();
		float oldYaw = player.getYaw();
		float oldPitch = player.getPitch();
		
		boolean worked = confirmDoorTeleport0(leftFromPos, destPos, player);
		
		if(!worked) {
			//Bad player. Rubberband them
			player.networkHandler.requestTeleport(oldPos.x, oldPos.y, oldPos.z, oldYaw, oldPitch);
			
			//Hit em with an update as well
			DokoServerPlayNetworkHandler.getFor(player).panicMode = true;
		}
	}
	
	private static boolean confirmDoorTeleport0(BlockPos leftFromPos, BlockPos destPos, ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		GatewayPersistentState gps = GatewayPersistentState.getFor(world);
		
		//Too far away
		if(player.getPos().squaredDistanceTo(Vec3d.ofBottomCenter(leftFromPos)) > 6 * 6) return false;
		
		//Find the gateway at this position
		@SuppressWarnings("SuspiciousNameCombination") //leftFromPos - Spurious warning, but good effort intellij
		Gateway thisGateway = Gateway.readFromWorld(world, leftFromPos);
		if(thisGateway == null) return false;
		
		//Pop the next random seed
		DokoServerPlayNetworkHandler ext = DokoServerPlayNetworkHandler.getFor(player);
		if(!ext.hasRandomSeed()) return false;
		Random random = Random.create(ext.popRandomSeed());
		
		//Find a different gateway using that random seed
		@Nullable Gateway destination = gps.getAllGateways().findDifferentGateway(thisGateway, random, 10, candidate -> candidate.stillExistsInWorld(world));
		
		//Only if it exists and matches what the player expected it to be,
		if(destination == null || !destination.doorTopPos().equals(destPos)) return false;
		
		//tp the player to it
		destination.arrive(world, thisGateway, player);
		return true;
	}
}
