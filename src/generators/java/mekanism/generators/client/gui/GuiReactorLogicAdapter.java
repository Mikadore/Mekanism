package mekanism.generators.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.button.ReactorLogicButton;
import mekanism.generators.common.inventory.container.reactor.ReactorLogicAdapterContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter, ReactorLogicAdapterContainer> {

    public GuiReactorLogicAdapter(ReactorLogicAdapterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new DisableableImageButton(guiLeft + 23, guiTop + 19, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0))),
              getOnHover("mekanism.gui.toggleCooling")));
        for (ReactorLogic type : ReactorLogic.values()) {
            int typeShift = 22 * type.ordinal();
            addButton(new ReactorLogicButton(guiLeft + 24, guiTop + 32 + typeShift, type, tileEntity, getGuiLocation(),
                  onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1, type))), getOnHover()));
        }
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof ReactorLogicButton) {
                ReactorLogic type = ((ReactorLogicButton) onHover).getType();
                int typeOffset = 22 * type.ordinal();
                renderItem(type.getRenderStack(), 27, 35 + typeOffset);
                drawString(TextComponentUtil.build(EnumColor.WHITE, type), 46, 34 + typeOffset, 0x404040);
                displayTooltip(TextComponentUtil.translate(type.getDescription()), xAxis, yAxis);
            }
        };
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        renderScaledText(TextComponentUtil.build(Translation.of("mekanism.gui.coolingMeasurements"), ": ", EnumColor.RED, OnOff.of(tileEntity.activeCooled)),
              36, 20, 0x404040, 117);
        renderScaledText(TextComponentUtil.build(Translation.of("mekanism.gui.redstoneOutputMode"), ": ", EnumColor.RED, tileEntity.logicType),
              23, 123, 0x404040, 130);
        drawCenteredText(TextComponentUtil.build(Translation.of("mekanism.gui.status"), ": ", EnumColor.RED,
              Translation.of("mekanism.gui." + (tileEntity.checkMode() ? "outputting" : "idle"))), 0, xSize, 136, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "reactor_logic_adapter.png");
    }
}