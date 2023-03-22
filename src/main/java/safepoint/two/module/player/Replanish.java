package safepoint.two.module.player;

import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import safepoint.two.core.decentralized.concurrent.utils.Timer;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import safepoint.two.core.settings.impl.DoubleSetting;

@ModuleInfo(name = "Replanish", category = Module.Category.Player, description = "no food loss")
public class Replanish extends Module {

    DoubleSetting percent = new DoubleSetting("Percent", 1.0d, 1.0d, 99.0d, this);
    DoubleSetting delay = new DoubleSetting("Delay", 100.0d, 100.0d, 1000.0d, this);
    BooleanSetting wait = new BooleanSetting("Wait", false, this);


    private final Map<Integer, ItemStack> hotbar = new ConcurrentHashMap<>();

    private final Timer timer = new Timer();

    private int refillSlot = -1;

    @Override
    public void onDisable() {
        super.onDisable();

        // reset values
        hotbar.clear();
        refillSlot = -1;
    }

    @Override
    public void onTick() {
        if(mc.player==null || mc.world==null)return;

        if (refillSlot == -1) {

            for (int i = 0; i < 9; ++i) {

                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                if (hotbar.getOrDefault(i, null) == null) {

                    if (stack.getItem().equals(Items.AIR)) {
                        continue;
                    }

                    hotbar.put(i, stack);
                    continue;
                }

                double percentage = ((double) stack.getCount() / (double) stack.getMaxStackSize()) * 100.0;

                if (percentage <= percent.getValue()) {

                    if (stack.getItem().equals(Items.END_CRYSTAL) && wait.getValue()) {
                        continue;
                    }

                    if (!timer.passed((int) delay.getValue().longValue())) {

                        refillSlot = i;
                    }

                    else {

                        fillStack(i, stack);

                        timer.reset();
                    }

                    break;
                }
            }
        }

        else {
            if (timer.passed((int) delay.getValue().longValue())) {

                fillStack(refillSlot, hotbar.get(refillSlot));

                timer.reset();
                refillSlot = -1;
            }
        }
    }

    private void fillStack(int slot, ItemStack stack) {

        if (slot != -1 && stack != null) {
            int replenishSlot = -1;

            for (int i = 9; i < 36; ++i) {
                ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

                if (!itemStack.isEmpty()) {

                    if (!stack.getDisplayName().equals(itemStack.getDisplayName())) {
                        continue;
                    }

                    if (stack.getItem() instanceof ItemBlock) {
                        if (!(itemStack.getItem() instanceof ItemBlock)) {
                            continue;
                        }

                        ItemBlock hotbarBlock = (ItemBlock) stack.getItem();
                        ItemBlock inventoryBlock = (ItemBlock) itemStack.getItem();

                        if (!hotbarBlock.getBlock().equals(inventoryBlock.getBlock())) {
                            continue;
                        }
                    }

                    else {
                        if (!stack.getItem().equals(itemStack.getItem())) {
                            continue;
                        }
                    }

                    replenishSlot = i;
                }
            }

            if (replenishSlot != -1) {

                int total = stack.getCount() + mc.player.inventory.getStackInSlot(replenishSlot).getCount();

                mc.playerController.windowClick(0, replenishSlot, 0, ClickType.PICKUP, mc.player);

                mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);

                if (total >= stack.getMaxStackSize()) {
                    mc.playerController.windowClick(0, replenishSlot, 0, ClickType.PICKUP, mc.player);
                }

                refillSlot = -1;
            }
        }
    }
}

