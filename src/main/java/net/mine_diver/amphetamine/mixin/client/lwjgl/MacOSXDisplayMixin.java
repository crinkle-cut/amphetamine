package net.mine_diver.amphetamine.mixin.client.lwjgl;

import net.mine_diver.amphetamine.client.lwjgl.MacOSXContextUpdateGate;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.lwjgl.opengl.MacOSXDisplay", remap = false)
abstract class MacOSXDisplayMixin {
    @Shadow
    private boolean updateNativeCursor;

    @Shadow
    public abstract int getX();

    @Shadow
    public abstract int getY();

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract int getHeight();

    @Unique
    private MacOSXContextUpdateGate amphetamine_contextUpdateGate;

    @Inject(method = {"createWindow", "destroyWindow"}, at = @At("HEAD"))
    private void amphetamine_resetContextUpdateGate(CallbackInfo ci) {
        amphetamine_getContextUpdateGate().reset();
    }

    @Inject(method = {"setScaleFactor", "reshape", "switchDisplayMode", "resetDisplayMode"}, at = @At("HEAD"))
    private void amphetamine_requireContextUpdate(CallbackInfo ci) {
        amphetamine_getContextUpdateGate().markDirty();
    }

    @Inject(method = "wasResized", at = @At("RETURN"))
    private void amphetamine_captureResize(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            amphetamine_getContextUpdateGate().markDirty();
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void amphetamine_skipRedundantContextUpdate(CallbackInfo ci) {
        MacOSXContextUpdateGate gate = amphetamine_getContextUpdateGate();
        long now = System.nanoTime();
        if (gate.shouldPollGeometry(now)) {
            int x = 0;
            int y = 0;
            if (!Display.isFullscreen()) {
                x = getX();
                y = getY();
            }
            gate.recordGeometry(x, y, getWidth(), getHeight());
        }
        if (!gate.shouldUpdate(now, updateNativeCursor))
            ci.cancel();
    }

    @Unique
    private MacOSXContextUpdateGate amphetamine_getContextUpdateGate() {
        if (amphetamine_contextUpdateGate == null)
            amphetamine_contextUpdateGate = new MacOSXContextUpdateGate();
        return amphetamine_contextUpdateGate;
    }
}
