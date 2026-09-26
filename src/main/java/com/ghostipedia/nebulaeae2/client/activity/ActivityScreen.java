package com.ghostipedia.nebulaeae2.client.activity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import com.ghostipedia.nebulaeae2.activity.ActivityNetworking;
import com.ghostipedia.nebulaeae2.activity.ActivityRequest;
import com.ghostipedia.nebulaeae2.activity.ActivityResponse;
import com.ghostipedia.nebulaeae2.activity.ActivityViewData;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.widgets.AETextField;
import appeng.menu.me.crafting.CraftingCPUMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ActivityScreen extends AESubScreen<CraftingCPUMenu, AEBaseScreen<CraftingCPUMenu>> {
    private static final String[] TABS = {"manual", "automated", "flow"};
    private static final String[] STATUSES = {"all", "active", "completed", "canceled"};
    private static final String[] WINDOWS = {"1m", "30m", "1h", "6h", "12h", "24h"};
    private static final String[] SORTS = {"amount", "name", "incoming", "outgoing", "net"};
    private static final String[] SORT_ICONS = {"◆", "A", "+", "−", "="};
    private static final int ROW_TOP = 90;
    private static final int ROW_HEIGHT = 15;
    private final Button[] sortButtons = new Button[SORTS.length];
    private final AETextField search;
    private final Button filter;
    private final Button mineButton;
    private final Button previous;
    private final Button next;
    private final Button detailsBack;
    private int tab;
    private int page;
    private int status;
    private int window;
    private int detailPage;
    private int serial;
    private long nextRefresh;
    private boolean mine;
    private String sort = "amount";
    private boolean reverseSort;
    private UUID selected;
    private CompoundTag data = new CompoundTag();

    public ActivityScreen(AEBaseScreen<CraftingCPUMenu> parent) {
        super(parent, "/screens/nebulae_activity.json");
        setTextContent(TEXT_ID_DIALOG_TITLE, text("title"));
        widgets.addButton("cpuBack", text("return_cpu"), this::returnToParent);
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            widgets.addButton(TABS[i], text(TABS[i]), () -> {
                tab = index; page = 0; selected = null; data = new CompoundTag(); refresh();
            });
        }
        search = widgets.addTextField("search");
        search.setMaxLength(128);
        search.setPlaceholder(text("search"));
        search.setResponder(value -> { page = 0; nextRefresh = 0; });
        filter = widgets.addButton("filter", text("all"), () -> {
            if (tab == 2) window = (window + 1) % WINDOWS.length;
            else status = (status + 1) % STATUSES.length;
            page = 0; refresh();
        });
        mineButton = widgets.addButton("mine", text("everyone"), () -> { mine = !mine; page = 0; refresh(); });
        previous = widgets.addButton("previous", Component.literal("<"), () -> {
            if (selected == null) page = Math.max(0, page - 1);
            else detailPage = Math.max(0, detailPage - 1);
            refresh();
        });
        next = widgets.addButton("next", Component.literal(">"), () -> {
            if (selected == null) page++;
            else detailPage++;
            refresh();
        });
        detailsBack = widgets.addButton("detailsBack", text("back"), () -> { selected = null; refresh(); });
        for (int i = 0; i < SORTS.length; i++) {
            String mode = SORTS[i];
            sortButtons[i] = widgets.addButton("sort_" + mode, Component.literal(SORT_ICONS[i]), () -> {
                if (sort.equals(mode)) reverseSort = !reverseSort;
                else { sort = mode; reverseSort = false; }
                page = 0;
                refresh();
            });
        }
        ActivityNetworking.setReceiver(ActivityScreen::receive);
    }

    public static void open(AEBaseScreen<CraftingCPUMenu> parent) {
        parent.switchToScreen(new ActivityScreen(parent));
    }

    private static void receive(ActivityResponse response) {
        if (Minecraft.getInstance().screen instanceof ActivityScreen screen
                && response.menuId() == screen.menu.containerId && response.serial() == screen.serial
                && response.data() != null && ActivityViewData.matches(response.data(), screen.tab, null)) {
            screen.data = response.data();
        }
    }

    private void refresh() {
        serial++;
        PacketDistributor.sendToServer(new ActivityRequest(menu.containerId, serial, tab, page, window, search.getValue(),
                status == 0 ? "" : STATUSES[status], tab == 0 && mine, selected, detailPage, sort, reverseSort));
        nextRefresh = System.currentTimeMillis() + 1_000;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (System.currentTimeMillis() >= nextRefresh) refresh();
        filter.setMessage(tab == 2 ? Component.literal(WINDOWS[window]) : text(STATUSES[status]));
        mineButton.visible = tab == 0 && selected == null;
        mineButton.setMessage(text(mine ? "mine" : "everyone"));
        filter.visible = selected == null;
        search.visible = selected == null;
        detailsBack.visible = selected != null;
        for (int i = 0; i < SORTS.length; i++) {
            boolean current = sort.equals(SORTS[i]);
            boolean ascending = SORTS[i].equals("name") != (current && reverseSort);
            sortButtons[i].visible = tab == 2;
            sortButtons[i].setMessage(Component.literal(SORT_ICONS[i] + (current ? ascending ? " ↑" : " ↓" : "")));
            sortButtons[i].setTooltip(Tooltip.create(text("sort." + SORTS[i]).copy().append("\n")
                    .append(text("sort." + (SORTS[i].equals("name")
                            ? ascending ? "az" : "za" : ascending ? "lowest" : "highest")))
                    .append("\n").append(text(current ? "sort.reverse" : "sort.select"))));
        }
        var detail = data.getCompound("detail");
        int current = selected == null ? page : detailPage;
        int total = !ActivityViewData.matches(data, tab, selected) ? 0
                : selected == null ? data.getInt("total") : detail.getInt("total");
        previous.active = current > 0;
        next.active = (long) (current + 1) * 6 < total;
    }

    @Override
    public void drawFG(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(graphics, offsetX, offsetY, mouseX, mouseY);
        int color = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        int muted = style.getColor(PaletteColor.MUTED_TEXT_COLOR).toARGB();
        var rows = ActivityViewData.rows(data, tab, selected);
        if (selected != null) graphics.drawString(font, text("planned"), 8, 47, color, false);
        else if (tab == 2) graphics.drawString(font, text("sort"), 8, 70, color, false);
        else {
            graphics.drawString(font, text("output_header"), 9, 75, muted, false);
            graphics.drawString(font, text("status_header"), 162, 75, muted, false);
        }
        if (rows.isEmpty()) {
            var empty = text(ActivityViewData.matches(data, tab, selected) ? "empty" : "loading");
            graphics.drawString(font, empty, (imageWidth - font.width(empty)) / 2, 130, muted, false);
        }
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.getCompound(i);
            int y = ROW_TOP + i * ROW_HEIGHT;
            var tooltip = new ArrayList<Component>();
            String label;
            if (selected != null) {
                var stack = GenericStack.readTag(minecraft.level.registryAccess(), row);
                label = stack == null ? "?" : stack.what().getDisplayName().getString() + "  " + quantity(stack.what(), stack.amount());
                tooltip.add(Component.literal(label));
                if (row.getBoolean("emitted")) tooltip.add(text("emitted"));
            } else if (tab == 2) {
                var flow = ActivityViewData.flowRow(row, minecraft.level.registryAccess());
                if (flow == null) continue;
                var key = flow.key();
                var incoming = flow.incoming();
                var outgoing = flow.outgoing();
                long coverage = data.getLong("coverage");
                label = key.getDisplayName().getString();
                tooltip.add(key.getDisplayName());
                tooltip.add(text("incoming", quantity(key, incoming), rate(key, incoming, coverage)));
                tooltip.add(text("outgoing", quantity(key, outgoing), rate(key, outgoing, coverage)));
                tooltip.add(text("net", quantity(key, incoming.subtract(outgoing)), rate(key, incoming.subtract(outgoing), coverage)));
                graphics.drawString(font, font.plainSubstrByWidth(label, 85), 9, y, color, false);
                String rates = "+" + rate(key, incoming, coverage) + " / -" + rate(key, outgoing, coverage);
                graphics.drawString(font, font.plainSubstrByWidth(rates, 129), 96, y, muted, false);
                label = null;
            } else {
                var output = GenericStack.readTag(minecraft.level.registryAccess(), row.getCompound("output"));
                label = output == null ? "?" : output.what().getDisplayName().getString() + "  " + quantity(output.what(), output.amount());
                tooltip.add(Component.literal(label));
                tooltip.add(text(row.getString("status")));
                tooltip.add(text("requester", row.getString("requester").isEmpty() ? text("unknown").getString() : row.getString("requester")));
                tooltip.add(text("started", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(row.getLong("started")))));
                tooltip.add(text("duration", duration(row.getLong("elapsed") / 1_000_000)));
                if (output != null) tooltip.add(text("delivered", quantity(output.what(), row.getLong("delivered"))));
                if (!row.getString("cpu").isEmpty()) tooltip.add(text("cpu", row.getString("cpu")));
                tooltip.add(text("details"));
                graphics.drawString(font, font.plainSubstrByWidth(text(row.getString("status")).getString(), 64), 162, y, muted, false);
            }
            if (label != null) graphics.drawString(font, font.plainSubstrByWidth(label, selected == null ? 149 : 213), 9, y, color, false);
            if (mouseX >= leftPos + 8 && mouseX < leftPos + 229 && mouseY >= topPos + y && mouseY < topPos + y + 14) {
                graphics.renderComponentTooltip(font, tooltip, mouseX - leftPos, mouseY - topPos);
            }
        }
        String footer = selected == null && tab == 2 ? text("recorded", duration(data.getLong("coverage"))).getString()
                : text("page", (selected == null ? page : detailPage) + 1).getString();
        graphics.drawString(font, font.plainSubstrByWidth(footer, 147), 43, 191, muted, false);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (selected == null && tab != 2 && button == 0 && x >= leftPos + 8 && x < leftPos + 229
                && y >= topPos + ROW_TOP && y < topPos + ROW_TOP + 6 * ROW_HEIGHT) {
            int index = (int) (y - topPos - ROW_TOP) / ROW_HEIGHT;
            var rows = ActivityViewData.rows(data, tab, null);
            if (index < rows.size() && rows.getCompound(index).hasUUID("id")) {
                selected = rows.getCompound(index).getUUID("id"); detailPage = 0; refresh(); return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selected != null) { selected = null; refresh(); }
            else returnToParent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static String quantity(AEKey key, long amount) { return quantity(key, BigInteger.valueOf(amount)); }

    private static String quantity(AEKey key, BigInteger amount) {
        return new BigDecimal(amount).divide(BigDecimal.valueOf(key.getAmountPerUnit()), 3, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString() + (key.getType().getId().getPath().equals("fluid") ? " B" : "");
    }

    private static String rate(AEKey key, BigInteger amount, long coverage) {
        return new BigDecimal(amount).multiply(BigDecimal.valueOf(60_000))
                .divide(BigDecimal.valueOf(Math.max(1, coverage)).multiply(BigDecimal.valueOf(key.getAmountPerUnit())), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString() + "/m";
    }

    private static String duration(long millis) {
        long seconds = Math.max(0, millis / 1_000);
        return seconds / 3_600 + "h " + seconds / 60 % 60 + "m " + seconds % 60 + "s";
    }

    private static Component text(String key, Object... args) { return Component.translatable("gui.nebulaeae2.activity." + key, args); }
}
