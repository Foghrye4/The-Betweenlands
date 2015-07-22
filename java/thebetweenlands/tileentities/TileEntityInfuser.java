package thebetweenlands.tileentities;

import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import thebetweenlands.blocks.BLFluidRegistry;

public class TileEntityInfuser extends TileEntityBasicInventory implements IFluidHandler {

	public final FluidTank waterTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 3);
	public int stirProgress = 90;
	public int temp;
	public int evaporation;
	public float objectVelocity;
	public float objectRotation;
	public int itemBob;
	public boolean countUp = true;
	public boolean hasInfusion = false;

	public TileEntityInfuser() {
		super(4, "infuser");
		waterTank.setFluid(new FluidStack(BLFluidRegistry.swampWater, 0));
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
				objectVelocity -= Math.signum(objectVelocity) * 0.05F;
				objectRotation += objectVelocity;
				if (objectRotation >= 360.0F)
					objectRotation -= 360.0F;
				else if (this.objectRotation <= 360.0F)
					objectRotation += 360.0F;
				if (Math.abs(objectVelocity) <= 1.0F && getWorldObj().rand.nextInt(15) == 0)
					objectVelocity = worldObj.rand.nextFloat() * 18.0F - 9.0F;
				if(countUp && itemBob <= 20) {
					itemBob++;
					if(itemBob == 20)
						countUp = false;
				}
				if(!countUp && itemBob >= 0) {
					itemBob--;
					if(itemBob == 0)
						countUp = true;
				}	
			return;
		}

		if (stirProgress < 90) {
			stirProgress++;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		if (stirProgress == 89) {
			if(temp == 100 && !hasInfusion) {
				if (inventory[0] == null || inventory[1] == null || inventory[2] == null || inventory[3] == null)
					return;
				else {
					hasInfusion = true;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
			evaporation = 0;
		}
		if(worldObj.getBlock(xCoord, yCoord - 1, zCoord) == Blocks.fire && temp < 100 && getWaterAmount() > 0) {
			if(worldObj.getWorldTime()%12 == 0) {
				temp++;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
		if(worldObj.getBlock(xCoord, yCoord - 1, zCoord) != Blocks.fire && temp > 0) {
			if(worldObj.getWorldTime()%6 == 0) {
				temp--;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
		if(temp == 100) {
			evaporation++;
			if(evaporation >= 600 && getWaterAmount() >= FluidContainerRegistry.BUCKET_VOLUME) {
				extractFluids(new FluidStack(BLFluidRegistry.swampWater, FluidContainerRegistry.BUCKET_VOLUME));
			}
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		if(temp < 100 && evaporation > 0) {
			evaporation--;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null)
			return 0;
		else if (resource.getFluid() == BLFluidRegistry.swampWater)
			return waterTank.fill(resource, doFill);
		else
			return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] infos = new FluidTankInfo[1];
		infos[0] = new FluidTankInfo(waterTank.getFluid(), waterTank.getCapacity());
		return infos;
	}

	public ItemStack fillTankWithBucket(ItemStack bucket) {
		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(bucket);
		if (fluid != null) {
			int amountFilled = fill(ForgeDirection.UNKNOWN, fluid, false);
			if (amountFilled == fluid.amount) {
				fill(ForgeDirection.UNKNOWN, fluid, true);
				if(temp >= 3) {
					temp = temp - temp / 3;
					evaporation = 0;
				}
				return FluidContainerRegistry.drainFluidContainer(bucket);
			}
		}
		return bucket;
	}

	public void extractFluids(FluidStack fluid) {
		if (fluid.isFluidEqual(waterTank.getFluid()))
			waterTank.drain(fluid.amount, true);
		if (getWaterAmount() == 0) {
			if (hasInfusion) {
				for (int i = 0; i < getSizeInventory(); i++) {
					setInventorySlotContents(i, null);
				}
				if (evaporation >= 600) {
					// TODO Make this a toxic cloud entity - a job for Sam's expert render skills :P
					EntityPig piggy = new EntityPig(worldObj);
					piggy.setLocationAndAngles(xCoord + 0.5D, yCoord + 1D, zCoord + 0.5D, MathHelper.wrapAngleTo180_float(worldObj.rand.nextFloat() * 360.0F), 0.0F);
					worldObj.spawnEntityInWorld(piggy);
				}
			}
			hasInfusion = false;
			evaporation = 0;
			temp = 0;
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public int getWaterAmount() {
		return waterTank.getFluidAmount();
	}

	public int getTanksFullValue() {
		return waterTank.getCapacity();
	}

	public int getScaledWaterAmount(int scale) {
		return waterTank.getFluid() != null ? (int) ((float) waterTank.getFluid().amount / (float) waterTank.getCapacity() * scale) : 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("stirProgress", stirProgress);
		nbt.setInteger("evaporation", evaporation);
		nbt.setInteger("temp", temp);
		nbt.setBoolean("hasInfusion", hasInfusion);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		waterTank.readFromNBT(nbt.getCompoundTag("waterTank"));
		stirProgress = nbt.getInteger("stirProgress");
		evaporation = nbt.getInteger("evaporationt");
		temp = nbt.getInteger("temp");
		hasInfusion = nbt.getBoolean("hasInfusion");
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("stirProgress", stirProgress);
		nbt.setInteger("evaporation", evaporation);
		nbt.setInteger("temp", temp);
		nbt.setBoolean("hasInfusion", hasInfusion);
		for (int i = 0; i < getSizeInventory(); i++) {
		if(inventory[i] != null) {
			NBTTagCompound itemStackCompound = inventory[i].writeToNBT(new NBTTagCompound());
			nbt.setTag("crushedItem" + i, itemStackCompound);
		} else
			nbt.setTag("crushedItem" + i, null);
		}
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		waterTank.readFromNBT(packet.func_148857_g().getCompoundTag("waterTank"));
		stirProgress = packet.func_148857_g().getInteger("stirProgress");
		evaporation = packet.func_148857_g().getInteger("evaporation");
		temp = packet.func_148857_g().getInteger("temp");
		hasInfusion = packet.func_148857_g().getBoolean("hasInfusion");
		for (int i = 0; i < getSizeInventory(); i++) {
		NBTTagCompound itemStackCompound = packet.func_148857_g().getCompoundTag("crushedItem" + i);
		if(itemStackCompound != null && itemStackCompound.getShort("id") != 0)
			inventory[i] = ItemStack.loadItemStackFromNBT(itemStackCompound);
		else
			inventory[i] = null;
		}
	}

}