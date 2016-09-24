package thebetweenlands.common.capability.equipment;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import thebetweenlands.common.capability.base.EntityCapability;
import thebetweenlands.common.inventory.InventoryEquipment;
import thebetweenlands.common.lib.ModInfo;
import thebetweenlands.common.registries.CapabilityRegistry;

public class EquipmentEntityCapability extends EntityCapability<EquipmentEntityCapability, IEquipmentCapability, EntityPlayer> implements IEquipmentCapability {
	@Override
	public ResourceLocation getID() {
		return new ResourceLocation(ModInfo.ID, "equipment");
	}

	@Override
	protected Capability<IEquipmentCapability> getCapability() {
		return CapabilityRegistry.CAPABILITY_EQUIPMENT;
	}

	@Override
	protected Class<IEquipmentCapability> getCapabilityClass() {
		return IEquipmentCapability.class;
	}

	@Override
	protected EquipmentEntityCapability getDefaultCapabilityImplementation() {
		return new EquipmentEntityCapability();
	}

	@Override
	public boolean isApplicable(Entity entity) {
		return entity instanceof EntityPlayer;
	}



	private ItemStack[][] inventories;

	public EquipmentEntityCapability() {
		this.inventories = new ItemStack[EnumEquipmentInventory.values().length][];
		for(EnumEquipmentInventory inventory : EnumEquipmentInventory.values()) {
			this.inventories[inventory.id] = new ItemStack[inventory.maxSize];
		}
	}

	@Override
	public IInventory getInventory(EnumEquipmentInventory inventory) {
		return new InventoryEquipment(this, this.inventories[inventory.id]);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList inventoryList = new NBTTagList();
		for(int i = 0; i < this.inventories.length; i++) {
			NBTTagCompound inventoryNbt = new NBTTagCompound();
			NBTTagList slotList = new NBTTagList();
			for(int c = 0; c < this.inventories[i].length; c++) {
				ItemStack stack = this.inventories[i][c];
				if(stack != null) {
					NBTTagCompound slotNbt = new NBTTagCompound();
					slotNbt.setInteger("slot", c);
					slotNbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
					slotList.appendTag(slotNbt);
				}
			}
			if(slotList.tagCount() > 0) {
				inventoryNbt.setInteger("id", i);
				inventoryNbt.setTag("items", slotList);
				inventoryList.appendTag(inventoryNbt);
			}
		}
		if(inventoryList.tagCount() > 0)
			nbt.setTag("inventories", inventoryList);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(nbt.hasKey("inventories")) {
			NBTTagList inventoryList = nbt.getTagList("inventories", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < inventoryList.tagCount(); i++) {
				NBTTagCompound inventoryNbt = inventoryList.getCompoundTagAt(i);
				if(inventoryNbt.hasKey("items")) {
					int id = inventoryNbt.getInteger("id");
					if(id < this.inventories.length) {
						ItemStack[] inventoryStacks = this.inventories[id];
						NBTTagList slotList = inventoryNbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
						for(int c = 0; c < slotList.tagCount(); c++) {
							NBTTagCompound slotNbt = slotList.getCompoundTagAt(c);
							int slot = slotNbt.getInteger("slot");
							if(slot < inventoryStacks.length) {
								inventoryStacks[slot] = ItemStack.loadItemStackFromNBT(slotNbt.getCompoundTag("stack"));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void writeTrackingDataToNBT(NBTTagCompound nbt) {
		this.writeToNBT(nbt);
	}

	@Override
	public void readTrackingDataFromNBT(NBTTagCompound nbt) {
		this.readFromNBT(nbt);
	}

	@Override
	public int getTrackingTime() {
		return 0;
	}
}