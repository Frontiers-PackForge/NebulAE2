package com.ghostipedia.nebulaeae2.stockpile;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.AEGuiHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.screen.UISettings;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public final class StockpileTopperUI {
    private static final String PREFIX = "gui.nebulaeae2.stockpile.";

    private StockpileTopperUI() {}

    public static void build(MEStockpileTopperPartMachine machine, ParentWidget<?> mainWidget,
            PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var online = new BooleanSyncValue(machine::isOnline);
        var refilling = new BooleanSyncValue(machine::isRefilling);
        var working = new BooleanSyncValue(machine::isWorkingEnabled);
        var stored = new LongSyncValue(machine::getWatchedAmount);
        syncManager.syncValue("stockpile_online", online);
        syncManager.syncValue("stockpile_refilling", refilling);
        syncManager.syncValue("stockpile_working", working);
        syncManager.syncValue("stockpile_stored", stored);
        var watcher = stackValue(machine::getWatcher);
        syncManager.syncValue("stockpile_watcher", watcher);

        var contents = Flow.col().coverChildren().childPadding(3);
        contents.child(Text.lang(PREFIX + "item_inputs").asWidget()
                .tooltipDynamic(tooltip -> tooltip.addLine(Component.translatable(PREFIX + "duplicate"))));
        var items = Flow.row().coverChildren();
        var fluids = Flow.row().coverChildren();
        for (int index = 0; index < 9; index++) {
            int slot = index;
            items.child(slot(syncManager, "stockpile_item_" + index, Kind.ITEM,
                    () -> machine.getItems().getConfigurableSlot(slot).getConfig(),
                    stack -> machine.setItemFilter(slot, stack)));
            fluids.child(slot(syncManager, "stockpile_fluid_" + index, Kind.FLUID,
                    () -> machine.getFluids().getConfigurableSlot(slot).getConfig(),
                    stack -> machine.setFluidFilter(slot, stack)));
        }
        contents.child(items);
        contents.child(Text.lang(PREFIX + "fluid_inputs").asWidget()
                .tooltipDynamic(tooltip -> tooltip.addLine(Component.translatable(PREFIX + "duplicate"))));
        contents.child(fluids);
        contents.child(Flow.row().coverChildren().childPadding(5)
                .child(new FilterSlot(syncManager, "stockpile_watch", Kind.EITHER, watcher, machine::setWatcher))
                .child(Text.lang(PREFIX + "watcher").asWidget()));
        contents.child(Text.dynamic(() -> Component.translatable(PREFIX + "target", units(watcher.getValue())))
                .asWidget());
        contents.child(amountField(new LongSyncValue(machine::getTarget,
                value -> machine.setTarget(Math.max(0, value))).allowC2S()));
        contents.child(Text.dynamic(() -> Component.translatable(PREFIX + "refill_gap", units(watcher.getValue())))
                .asWidget());
        contents.child(amountField(new LongSyncValue(machine::getRefillGap,
                value -> machine.setRefillGap(Math.max(0, value))).allowC2S()));
        contents.child(Text.dynamic(() -> Component.translatable(PREFIX + "stored",
                Long.toString(stored.getLongValue()), units(watcher.getValue()))).asWidget());
        contents.child(Text.dynamic(() -> Component.translatable(PREFIX + (!online.getBoolValue() ? "offline"
                : working.getBoolValue() && refilling.getBoolValue() ? "refilling" : "paused"))).asWidget());
        mainWidget.child(contents.center().margin(4));
    }

    private static TextFieldWidget amountField(LongSyncValue value) {
        return new TextFieldWidget().size(162, 16).value(value)
                .setMaxLength(19).setPattern(Pattern.compile("[0-9]*"))
                .setValidator(text -> {
                    if (text.isEmpty()) return "0";
                    try {
                        return Long.toString(Math.max(0, Long.parseLong(text)));
                    } catch (NumberFormatException ignored) {
                        return Long.toString(Long.MAX_VALUE);
                    }
                });
    }

    private static Component units(@Nullable GenericStack stack) {
        return Component.translatable(PREFIX + (stack != null && stack.what() instanceof AEFluidKey
                ? "millibuckets" : "items"));
    }

    private static FilterSlot slot(PanelSyncManager syncManager, String id, Kind kind,
            Supplier<GenericStack> getter, Consumer<GenericStack> setter) {
        var value = stackValue(getter);
        syncManager.syncValue(id, value);
        return new FilterSlot(syncManager, id, kind, value, setter);
    }

    private static GenericSyncValue<RegistryFriendlyByteBuf, GenericStack> stackValue(Supplier<GenericStack> getter) {
        return GenericSyncValue.<RegistryFriendlyByteBuf, GenericStack>builder(GenericStack.class)
                .getter(getter).deserializer(GenericStack::readBuffer)
                .serializer((buffer, stack) -> GenericStack.writeBuffer(stack, buffer))
                .copyImmutable().build();
    }

    enum Kind {
        ITEM, FLUID, EITHER;

        boolean accepts(@Nullable GenericStack stack) {
            return stack == null || stack.what() instanceof AEItemKey && this != FLUID
                    || stack.what() instanceof AEFluidKey && this != ITEM;
        }

        @Nullable GenericStack fromItem(ItemStack item) {
            if (item.isEmpty()) return null;
            var wrapped = GenericStack.unwrapItemStack(item);
            if (wrapped != null) return accepts(wrapped) ? wrapped : null;
            if (this != ITEM) {
                var fluid = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
                if (!fluid.isEmpty()) return new GenericStack(AEFluidKey.of(fluid), fluid.getAmount());
            }
            return this == FLUID ? null : GenericStack.fromItemStack(item);
        }

        @Nullable GenericStack fromEmi(EmiStack ingredient) {
            var identity = ingredient.copy().setAmount(1);
            if (this == FLUID || identity.getKeyOfType(Fluid.class) != null) {
                var fluid = EmiStackConverter.FLUID.convertFrom(identity);
                if (fluid == null || fluid.isEmpty()) return null;
                var stack = new GenericStack(AEFluidKey.of(fluid), 1);
                return accepts(stack) ? stack : null;
            }
            var item = EmiStackConverter.ITEM.convertFrom(identity);
            if (item == null || item.isEmpty()) return null;
            var stack = GenericStack.fromItemStack(item);
            return accepts(stack) ? stack : null;
        }
    }

    private static final class FilterSlot extends Widget<FilterSlot>
            implements Interactable, GhostIngredientSlot<GenericStack> {
        private final PanelSyncManager syncManager;
        private final String id;
        private final Kind kind;
        private final GenericSyncValue<RegistryFriendlyByteBuf, GenericStack> value;

        private FilterSlot(PanelSyncManager syncManager, String id, Kind kind,
                GenericSyncValue<RegistryFriendlyByteBuf, GenericStack> value, Consumer<GenericStack> setter) {
            this.syncManager = syncManager;
            this.id = id;
            this.kind = kind;
            this.value = value;
            size(18);
            syncManager.registerServerSyncedAction(id + "_cursor", buffer -> {
                boolean clear = buffer.readBoolean();
                if (clear) {
                    setter.accept(null);
                } else {
                    var stack = kind.fromItem(syncManager.getPlayer().containerMenu.getCarried());
                    if (stack != null && kind.accepts(stack)) setter.accept(new GenericStack(stack.what(), 1));
                }
            });
            syncManager.registerServerSyncedAction(id + "_ghost", buffer -> {
                var stack = GenericStack.readBuffer(buffer);
                if (stack != null && stack.amount() > 0 && kind.accepts(stack)) {
                    setter.accept(new GenericStack(stack.what(), 1));
                }
            });
            tooltipAutoUpdate(true);
            tooltipDynamic(tooltip -> {
                var stack = value.getValue();
                if (stack != null) tooltip.addLine(stack.what().getDisplayName());
                tooltip.addLine(Component.translatable(PREFIX + (kind == Kind.FLUID
                        ? "ghost_fluid_hint" : "ghost_hint")));
            });
        }

        @Override
        public void onInit() {
            super.onInit();
            getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            (kind == Kind.FLUID ? GTGuiTextures.FLUID_SLOT : GTGuiTextures.SLOT).draw(context, 0, 0, 18, 18);
            var stack = value.getValue();
            if (stack == null) {
                GTGuiTextures.CONFIG_ARROW_DARK.draw(context, 0, 0, 18, 18);
            } else if (stack.what() instanceof AEItemKey item) {
                context.getGraphics().renderItem(item.toStack(1), 1, 1);
            } else if (stack.what() instanceof AEFluidKey) {
                AEGuiHelper.drawFluid(context.getGraphics(), stack, 1, 1);
            }
        }

        @Override
        public Result onMousePressed(int button) {
            if (button != 0 && button != 1) return Result.IGNORE;
            syncManager.callSyncedAction(id + "_cursor", buffer -> buffer.writeBoolean(button == 1));
            return Result.SUCCESS;
        }

        @Override
        public void setGhostIngredient(GenericStack ingredient) {
            if (kind.accepts(ingredient)) {
                syncManager.callSyncedAction(id + "_ghost", buffer -> GenericStack.writeBuffer(ingredient, buffer));
            }
        }

        @Override
        public @Nullable GenericStack castGhostIngredientIfValid(Object ingredient) {
            if (!areAncestorsEnabled()) return null;
            GenericStack stack = null;
            if (ingredient instanceof EmiStack emi) {
                stack = kind.fromEmi(emi);
            } else if (ingredient instanceof GenericStack generic) {
                stack = generic;
            } else if (ingredient instanceof ItemStack item) {
                stack = kind.fromItem(item);
            } else if (ingredient instanceof FluidStack fluid && !fluid.isEmpty()) {
                stack = new GenericStack(AEFluidKey.of(fluid), fluid.getAmount());
            }
            return kind.accepts(stack) ? stack : null;
        }

        @Override
        public boolean ingredientHandlingOverride(Object ingredient) {
            if (!(ingredient instanceof EmiStack emi) || !areAncestorsEnabled()) return false;
            var stack = kind.fromEmi(emi);
            if (stack == null) return false;
            setGhostIngredient(stack);
            return true;
        }

        @Override
        public Class<GenericStack> ingredientClass() {
            return GenericStack.class;
        }
    }
}
