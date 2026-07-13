package net.mine_diver.amphetamine.mixin.client;

import net.mine_diver.amphetamine.client.render.FrustumIntersection;
import net.minecraft.client.render.FrustumData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FrustumData.class)
abstract class FrustumDataMixin {
    @Shadow public float[][] frustum;

    /**
     * @author Nitch
     * @reason Test the positive AABB vertex once per plane instead of all eight corners.
     */
    @Overwrite
    public boolean intersects(double minX, double minY, double minZ,
                              double maxX, double maxY, double maxZ) {
        return FrustumIntersection.intersects(this.frustum, minX, minY, minZ, maxX, maxY, maxZ);
    }
}
