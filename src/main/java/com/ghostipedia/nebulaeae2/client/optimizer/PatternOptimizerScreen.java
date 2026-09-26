package com.ghostipedia.nebulaeae2.client.optimizer;

import java.util.ArrayList;
import java.util.List;

import com.ghostipedia.nebulaeae2.optimizer.OptimizerMenu;
import com.ghostipedia.nebulaeae2.optimizer.OptimizerRequest;
import com.ghostipedia.nebulaeae2.optimizer.OptimizerSnapshot;
import com.ghostipedia.nebulaeae2.optimizer.PatternOptimization;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.widgets.AETextField;
import appeng.menu.AEBaseMenu;

public final class PatternOptimizerScreen<T extends AEBaseMenu> extends AESubScreen<T, AEBaseScreen<T>> {
    private final OptimizerMenu optimizer;
    private final List<Button> selections = new ArrayList<>();
    private final List<Button> details = new ArrayList<>();
    private final List<Button> protections = new ArrayList<>();
    private final AETextField multiplier;
    private final Button operationButton;
    private final Button apply;
    private String operation = "multiply";
    private int detailRow = -1;
    private int detailPage;
    private int lastMouseX;
    private int lastMouseY;
    private Component hoveredText;

    public PatternOptimizerScreen(AEBaseScreen<T> parent) {
        super(parent, "/screens/nebulae_pattern_optimizer.json");
        optimizer = (OptimizerMenu) menu;
        setTextContent(TEXT_ID_DIALOG_TITLE, text("title"));
        for (int i = 0; i < OptimizerSnapshot.PAGE_SIZE; i++) {
            final int row = i;
            selections.add(widgets.addButton("select" + i, Component.empty(), () -> {
                var snapshot = optimizer.nebulae$optimizerSnapshot();
                if (row < snapshot.rows().size()) {
                    request("select", snapshot.rows().get(row).index());
                }
            }));
            details.add(widgets.addButton("details" + i, text("details"), () -> {
                detailRow = row;
                detailPage = 0;
            }));
            protections.add(widgets.addButton("protect" + i, text("enabled_short"), () -> {
                var snapshot = optimizer.nebulae$optimizerSnapshot();
                if (row < snapshot.rows().size()) {
                    request("toggle", snapshot.rows().get(row).index());
                }
            }));
        }
        widgets.addButton("previous", Component.literal("<"), () -> {
            if (detailRow >= 0) {
                detailPage = Math.max(0, detailPage - 1);
            } else {
                request("page", optimizer.nebulae$optimizerSnapshot().page() - 1);
            }
        });
        widgets.addButton("next", Component.literal(">"), () -> {
            if (detailRow >= 0) {
                detailPage++;
            } else {
                request("page", optimizer.nebulae$optimizerSnapshot().page() + 1);
            }
        });
        multiplier = widgets.addTextField("factor");
        multiplier.setMaxLength(19);
        multiplier.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        multiplier.setValue("2");
        multiplier.setTextColor(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
        multiplier.setSelectionColor(style.getColor(PaletteColor.TEXTFIELD_SELECTION).toARGB());
        multiplier.setTooltipMessage(List.of(text("factor")));
        operationButton = widgets.addButton("operation", text(operation), this::cycleOperation);
        widgets.addButton("preview", text("preview"), () -> request("preview", 0));
        widgets.addButton("back", text("back"), () -> {
            if (detailRow >= 0) {
                detailRow = -1;
            } else {
                returnToParent();
            }
        });
        widgets.addButton("refresh", text("refresh"), () -> {
            detailRow = -1;
            request("refresh", 0);
        });
        apply = widgets.addButton("apply", text("apply"), () -> request("apply", 0));
        request("refresh", 0);
    }

    private void cycleOperation() {
        operation = switch (operation) {
            case "multiply" -> "divide";
            case "divide" -> "restore";
            default -> "multiply";
        };
        operationButton.setMessage(text(operation));
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    private void request(String action, int index) {
        long factor = 1;
        if (multiplier != null && !operation.equals("restore")) {
            try {
                factor = Long.parseLong(multiplier.getValue());
            } catch (NumberFormatException exception) {
                if (action.equals("preview")) {
                    return;
                }
            }
        }
        optimizer.nebulae$optimizerAction(new OptimizerRequest(action, index, operation, factor,
                optimizer.nebulae$optimizerSnapshot().revision()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        var snapshot = optimizer.nebulae$optimizerSnapshot();
        for (int i = 0; i < selections.size(); i++) {
            boolean visible = detailRow < 0 && i < snapshot.rows().size();
            selections.get(i).visible = visible;
            details.get(i).visible = visible;
            protections.get(i).visible = visible;
            if (visible) {
                var row = snapshot.rows().get(i);
                selections.get(i).setMessage(Component.literal(row.selected() ? "x" : ""));
                selections.get(i).active = row.allowed();
                selections.get(i).setTooltip(Tooltip.create(text("select_location")));
                protections.get(i).setMessage(text(row.allowed() ? "enabled_short" : "disabled_short"));
                protections.get(i).setTooltip(Tooltip.create(text("toggle_tooltip")));
                details.get(i).setTooltip(Tooltip.create(Component.literal(row.location())
                        .append(" ").append(Component.translatable("gui.nebulaeae2.optimizer.slot", row.slot() + 1))));
            }
        }
        apply.active = snapshot.previewReady() && snapshot.operation().equals(operation)
                && (operation.equals("restore") || multiplier.getValue().equals(Long.toString(snapshot.factor())));
        apply.setTooltip(Tooltip.create(text("impact_tooltip").copy().append(" ")
                .append(Component.translatable("gui.nebulaeae2.optimizer.impact", snapshot.beforeExecutions(),
                        snapshot.afterExecutions(), snapshot.excess()))));
        multiplier.setEditable(!operation.equals("restore"));
    }

    @Override
    public void drawFG(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        var snapshot = optimizer.nebulae$optimizerSnapshot();
        int color = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        int muted = style.getColor(PaletteColor.MUTED_TEXT_COLOR).toARGB();
        if (detailRow >= 0 && detailRow < snapshot.rows().size()) {
            drawDetails(graphics, snapshot.rows().get(detailRow), color, muted);
        } else {
            for (int i = 0; i < snapshot.rows().size(); i++) {
                var row = snapshot.rows().get(i);
                int y = 25 + i * 35;
                drawClipped(graphics, row.name(), 31, y, 174, color);
                drawClipped(graphics, Component.literal(row.location()), 31, y + 10, 174, muted);
                var encoded = row.before().get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (encoded != null && !encoded.sparseOutputs().isEmpty() && encoded.sparseOutputs().getFirst() != null) {
                    var output = encoded.sparseOutputs().getFirst();
                    drawClipped(graphics, output.what().getDisplayName(), 31, y + 20, 174, color);
                }
            }
            drawClipped(graphics, Component.translatable("gui.nebulaeae2.optimizer.page",
                    snapshot.page() + 1, Math.max(1, (snapshot.total() + 2) / 3), snapshot.selected()), 36, 136, 208, color);
        }
        var status = text(snapshot.status());
        drawClipped(graphics, status, 8, 179, 264, color);
        if (snapshot.previewReady() && snapshot.beforeExecutions() > 0) {
            var impact = Component.translatable("gui.nebulaeae2.optimizer.impact", snapshot.beforeExecutions(),
                    snapshot.afterExecutions(), snapshot.excess());
            drawClipped(graphics, impact, 8, 191, 264, muted);
        }
    }

    private void drawDetails(GuiGraphics graphics, OptimizerSnapshot.Row row, int color, int muted) {
        var before = row.before().get(AEComponents.ENCODED_PROCESSING_PATTERN);
        var after = row.after().get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (before == null) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        appendQuantities(lines, "input", before.sparseInputs(), after == null ? null : after.sparseInputs());
        appendQuantities(lines, "output", before.sparseOutputs(), after == null ? null : after.sparseOutputs());
        int pages = Math.max(1, (lines.size() + 4) / 5);
        detailPage = Math.clamp(detailPage, 0, pages - 1);
        drawClipped(graphics, row.name(), 8, 25, 264, color);
        drawClipped(graphics, Component.literal(row.location()), 8, 36, 264, muted);
        var beforeProvenance = row.before().get(PatternOptimization.PROVENANCE);
        var afterProvenance = row.after().get(PatternOptimization.PROVENANCE);
        var factor = Component.translatable("gui.nebulaeae2.optimizer.multiplier",
                beforeProvenance == null ? 1 : beforeProvenance.multiplier(),
                row.after().isEmpty() ? "?" : afterProvenance == null ? 1 : afterProvenance.multiplier());
        drawClipped(graphics, factor, 8, 47, 264, muted);
        for (int i = 0; i < 5 && detailPage * 5 + i < lines.size(); i++) {
            var line = lines.get(detailPage * 5 + i);
            int y = 62 + i * 12;
            drawClipped(graphics, line, 8, y, 264, color);
        }
        drawClipped(graphics, Component.translatable("gui.nebulaeae2.optimizer.detail_page", detailPage + 1, pages),
                36, 136, 208, muted);
        if (!row.error().isEmpty()) {
            drawClipped(graphics, text(row.error()), 8, 122, 264, style.getColor(PaletteColor.ERROR).toARGB());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        hoveredText = null;
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hoveredText != null) {
            graphics.renderTooltip(font, hoveredText, mouseX, mouseY);
        }
    }

    private void drawClipped(GuiGraphics graphics, Component value, int x, int y, int width, int color) {
        graphics.drawString(font, font.substrByWidth(value, width).getString(), x, y, color, false);
        if (font.width(value) > width && lastMouseX >= leftPos + x && lastMouseX < leftPos + x + width
                && lastMouseY >= topPos + y && lastMouseY < topPos + y + font.lineHeight) {
            hoveredText = value;
        }
    }

    private static void appendQuantities(List<Component> lines, String kind, List<GenericStack> before,
            List<GenericStack> after) {
        for (int i = 0; i < before.size(); i++) {
            var stack = before.get(i);
            if (stack != null) {
                String next = after == null ? "?" : Long.toString(after.get(i).amount());
                lines.add(Component.translatable("gui.nebulaeae2.optimizer.quantity", text(kind),
                        stack.what().getDisplayName(), stack.amount(), next));
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (detailRow >= 0) {
                detailRow = -1;
            } else {
                returnToParent();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static Component text(String key) {
        return Component.translatable("gui.nebulaeae2.optimizer." + key);
    }
}
