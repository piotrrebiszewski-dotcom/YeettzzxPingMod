package net.anarchypingmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AnarchyPingModClient implements ClientModInitializer {

    public static String channelCode = "3123";
    public static boolean showSkinHead = true;
    public static boolean showDistance = true;
    public static boolean showXYZ = true;

    private static KeyBinding pingKeyBinding;
    private static BlockPos currentPingPos = null;
    private static long pingTimestamp = 0;

    @Override
    public void onInitializeClient() {
        pingKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anarchypingmod.ping",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_KEY_MIDDLE,
                "category.anarchypingmod.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (pingKeyBinding.wasPressed()) {
                handlePing(client);
            }
        });

        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    private void handlePing(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        HitResult hit = client.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            currentPingPos = blockHit.getBlockPos();
            pingTimestamp = System.currentTimeMillis();

            String message = String.format("[%s] Ping: X:%d Y:%d Z:%d", 
                    channelCode, currentPingPos.getX(), currentPingPos.getY(), currentPingPos.getZ());
            
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || currentPingPos == null) return;

        if (System.currentTimeMillis() - pingTimestamp > 10000) {
            currentPingPos = null;
            return;
        }

        int x = 10;
        int y = 10;

        if (showXYZ) {
            String coordsText = String.format("Ping: X: %d | Y: %d | Z: %d", 
                    currentPingPos.getX(), currentPingPos.getY(), currentPingPos.getZ());
            context.drawTextWithShadow(client.textRenderer, coordsText, x, y, 0x00FF88);
            y += 12;
        }

        if (showDistance) {
            Vec3d playerPos = client.player.getPos();
            double distance = Math.sqrt(currentPingPos.getSquaredDistance(playerPos));
            String distText = String.format("Dystans: %.1fm", distance);
            context.drawTextWithShadow(client.textRenderer, distText, x, y, 0xFFFFFF);
        }
    }
}
