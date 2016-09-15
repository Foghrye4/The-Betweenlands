package thebetweenlands.util;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class FogGenerator {
	private static int lastCX, lastCZ;
	private static NoiseGeneratorPerlin fogNoiseGen;
	private static double[] fogChunkNoise = new double[256];

	/**
	 * Returns the fog range based on the player position.
	 * @param start
	 * @param end
	 * @param rng
	 * @return
	 */
	public static float[] getFogRange(float min, float max, long seed) {
		if(Minecraft.getMinecraft().thePlayer == null)
			return new float[]{min, max};
		double x = Minecraft.getMinecraft().thePlayer.posX;
		double z = Minecraft.getMinecraft().thePlayer.posZ;
		int cx = (int)((x - ((int)(Math.floor(x)) & 15)) / 16) - 1;
		int cz = (int)((z - ((int)(Math.floor(z)) & 15)) / 16);
		if(fogNoiseGen == null) {
			Random rnd = new Random();
			rnd.setSeed(seed);
			fogNoiseGen = new NoiseGeneratorPerlin(rnd, 4);
		}
		if(fogChunkNoise == null || lastCX != cx || lastCZ != cz) {
			lastCX = cx;
			lastCZ = cz;
			fogChunkNoise = fogNoiseGen.getRegion(
					fogChunkNoise, 
					(double) (cx * 16), (double) (cz * 16), 
					16, 16, 0.003D, 0.003D, 0.003D);
		}
		int ix = (int)(Math.floor(x)) & 15;
		int iz = (int)(Math.floor(z)) & 15;

		double noise = (Math.abs(fogChunkNoise[iz * 16 + ix]));

		float diff = max - min;

		float newMax = (float) Math.max(min, min + diff * (1.0F - noise));

		diff = (newMax - min) / 2.0F;

		float newMin = (float) (noise < 0.75F ? min + diff / 2.0F : (min + diff / 2.0F * (1.0F - (noise - 0.75F) * 2.0F)));

		return new float[]{newMin, newMax};
	}
}
