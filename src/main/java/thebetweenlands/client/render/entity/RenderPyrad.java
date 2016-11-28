package thebetweenlands.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.render.entity.layer.LayerGlow;
import thebetweenlands.client.render.model.entity.ModelPyrad;
import thebetweenlands.client.render.shader.LightSource;
import thebetweenlands.client.render.shader.ShaderHelper;
import thebetweenlands.common.entity.mobs.EntityPyrad;

@SideOnly(Side.CLIENT)
public class RenderPyrad extends RenderLiving<EntityPyrad> {
	public final static ResourceLocation TEXTURE = new ResourceLocation("thebetweenlands:textures/entity/pyrad.png");

	public RenderPyrad(RenderManager manager) {
		super(manager, new ModelPyrad(), 0.5F);
		this.addLayer(new LayerGlow<EntityPyrad>(this, new ResourceLocation("thebetweenlands:textures/entity/pyrad_glow.png")));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPyrad entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityPyrad entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, Math.sin((entity.ticksExisted + partialTicks) / 10.0F) / 4.0F - 0.25D, 0);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.popMatrix();

		if(entity.getGlowTicks(partialTicks) > 0 && ShaderHelper.INSTANCE.isWorldShaderActive()) {
			double rx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
			double ry = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
			double rz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
			float brightness = entity.getGlowTicks(partialTicks) / 10.0F;
			ShaderHelper.INSTANCE.getWorldShader().addLight(new LightSource(rx, ry + entity.getEyeHeight(), rz,
					5.75f,
					134.0f / 255.0f * 10.0F * brightness,
					214.0f / 255.0f * 10.0F * brightness,
					55.0f / 255.0f * 10.0F * brightness));
		}
	}
}
