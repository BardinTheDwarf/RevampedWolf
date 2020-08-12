package baguchan.revampedwolf.container;

import baguchan.revampedwolf.entity.IWolfArmor;
import baguchan.revampedwolf.entity.IWolfInventory;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class WolfInventoryContainer extends Container {
    private final IInventory wolfInventory;
    private final WolfEntity wolf;

    public WolfInventoryContainer(int id, PlayerInventory playerInventory, final int entityID) {
        super(WolfContainers.WOLF_INVENTORY, id);

        this.wolf = (WolfEntity) playerInventory.player.world.getEntityByID(entityID);
        this.wolfInventory = ((IWolfInventory) wolf).getWolfInventory();
        int i = 3;
        wolfInventory.openInventory(playerInventory.player);
        this.addSlot(new Slot(wolfInventory, 0, 8, 18) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return wolf instanceof IWolfArmor && ((IWolfArmor) wolf).isWolfArmor(stack);
            }

            /**
             * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
             * case of armor slots)
             */
            public int getSlotStackLimit() {
                return 1;
            }
        });

        for (int i1 = 0; i1 < 3; ++i1) {
            for (int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
            }
        }

        for (int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
        }

    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.wolfInventory.isUsableByPlayer(playerIn) && this.wolf.isAlive() && this.wolf.getDistance(playerIn) < 8.0F;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            int i = this.wolfInventory.getSizeInventory();
            if (index < i) {
                if (!this.mergeItemStack(itemstack1, i, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).isItemValid(itemstack1)) {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i <= 1 || !this.mergeItemStack(itemstack1, 1, i, false)) {
                int j = i + 27;
                int k = j + 9;
                if (index >= j && index < k) {
                    if (!this.mergeItemStack(itemstack1, i, j, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= i && index < j) {
                    if (!this.mergeItemStack(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemstack1, j, j, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.wolfInventory.closeInventory(playerIn);
    }

    public WolfEntity getWolf() {
        return wolf;
    }
}