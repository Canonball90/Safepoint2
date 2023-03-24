package safepoint.two.utils.world;

import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int findHotbarItem(final Class<?> clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (clazz.isInstance(stack.getItem())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean isBlock(Item item, Class clazz) {
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            return clazz.isInstance(block);
        }
        return false;
    }

    public static void switchToHotbarSlot(final int slot, final boolean silent) {
        if (mc.player == null || mc.world == null || mc.player.inventory == null) {
            return;
        }
        if (mc.player.inventory.currentItem == slot || slot < 0) {
            return;
        }
        if (silent) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.playerController.updateController();
        } else {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }

    public static int findHotbarBlock(final Class<?> clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (clazz.isInstance(stack.getItem())) {
                    return i;
                }
                if (stack.getItem() instanceof ItemBlock) {
                    final Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (clazz.isInstance(block)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static int getItemSlot(Item items) {
        for (int i = 0; i < 36; ++i) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == items) {
                if (i < 9) {
                    i += 36;
                }

                return i;
            }
        }
        return -1;
    }

    public static int findItemInHotbar(Item itemToFind) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY) continue;
            stack.getItem();
            Item item = stack.getItem();
            if (!item.equals((Object)itemToFind)) continue;
            slot = i;
            break;
        }
        return slot;
    }

    public static int findWindowItem(Item item, int minimum, int maximum) {
        for (int i = minimum; i <= maximum; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack.getItem() == item) {
                return i;
            }
        }

        return - 1;
    }

    public static int findWindowItem(Item item, int count, int minimum, int maximum) {
        for (int i = minimum; i <= maximum; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack.getItem() == item && stack.getCount() >= count) {
                return i;
            }
        }

        return - 1;
    }

    public static int getItemCount(Item item) {
        int count = mc.player.inventory.mainInventory.stream()
                .filter(itemStack -> itemStack.getItem() == item)
                .mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == item) {
            count += mc.player.getHeldItemOffhand().getCount();
        }
        return count;
    }

    public static int findItemInHotBar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void switchToSlot(int slot) {
        if (mc.player.inventory.currentItem == slot
                || slot == -1) {
            return;
        }
        //Send packet to server
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        mc.player.inventory.currentItem = slot;
        mc.playerController.updateController();
    }

    //from earthheck
    public static void switchToSlotButBetter(int slot) {
        if (mc.player.inventory.currentItem == slot) return;
        mc.playerController.pickItem(slot);
    }

    public static int findBlockInHotBar(Block block) {
        return findItemInHotBar(Item.getItemFromBlock(block));
    }

    public static boolean isItemInHotbar(Item item) {
        boolean isItemPresent = false;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == item) {
                isItemPresent = true;
            }
        }
        return isItemPresent;
    }

    public static int fastestMiningTool(Block toMineBlockMaterial) {
        float fastestSpeed = 1.0f;
        int theSlot = mc.player.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemHoe || itemStack.getItem() instanceof ItemShears))
                continue;

            float mineSpeed = BlockUtil.blockBreakSpeed(toMineBlockMaterial.getDefaultState(), itemStack);

            if (mineSpeed > fastestSpeed) {
                fastestSpeed = mineSpeed;
                theSlot = i;
            }
        }

        return theSlot;
    }

    public static boolean isItemInInventory(Item item) {
        for (Slot slot : mc.player.inventoryContainer.inventorySlots) {
            if (slot.getStack().getItem() == item)
                return true;
        }
        return false;
    }

    public static int itemSlotIDinInventory(Item item) {
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryContainer.inventorySlots.get(i).getStack().getItem() == item)
                return i;
        }
        return 99999;
    }

    public static void swapItemFromInvToHotBar(Item item, int hotBarSlot) {
        int slotID = itemSlotIDinInventory(item);

        if (slotID != 99999) {
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, hotBarSlot + 36, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    public static int findItemInInv(Item item) {
        for (Pair<Integer, ItemStack> data : getInventoryAndHotbarSlots()) {
            if (data.equals(item)) {
                return data.getKey();
            }
        }
        return -999;
    }

    public static List<Pair<Integer, ItemStack>> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }

    private static List<Pair<Integer, ItemStack>> getInventorySlots(int current, int last) {
        List<Pair<Integer, ItemStack>> invSlots = new ArrayList<>();
        while (current <= last) {
            invSlots.add(new Pair<>(current, mc.player.inventoryContainer.getInventory().get(current)));
            current++;
        }
        return invSlots;
    }
}
