package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.integration.computer.IComputerIntegration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class TileEntityReactorLogicAdapter extends TileEntityReactorBlock implements IComputerIntegration {

    private static final String[] methods = new String[]{"isIgnited", "canIgnite", "getPlasmaHeat", "getMaxPlasmaHeat", "getCaseHeat", "getMaxCaseHeat",
                                                         "getInjectionRate", "setInjectionRate", "hasFuel", "getProducing", "getIgnitionTemp", "getEnergy",
                                                         "getMaxEnergy", "getWater", "getSteam", "getFuel", "getDeuterium", "getTritium"};
    public ReactorLogic logicType = ReactorLogic.DISABLED;
    public boolean activeCooled;
    public boolean prevOutputting;

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            boolean outputting = checkMode();
            if (outputting != prevOutputting) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
            prevOutputting = outputting;
        }
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    public boolean checkMode() {
        if (world.isRemote) {
            return prevOutputting;
        }
        if (getReactor() == null || !getReactor().isFormed()) {
            return false;
        }
        switch (logicType) {
            case DISABLED:
                return false;
            case READY:
                return getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled);
            case CAPACITY:
                return getReactor().getPlasmaTemp() >= getReactor().getMaxPlasmaTemperature(activeCooled);
            case DEPLETED:
                return (getReactor().getDeuteriumTank().getStored() < getReactor().getInjectionRate() / 2) ||
                       (getReactor().getTritiumTank().getStored() < getReactor().getInjectionRate() / 2);
            default:
                return false;
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        logicType = ReactorLogic.values()[nbtTags.getInt("logicType")];
        activeCooled = nbtTags.getBoolean("activeCooled");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("logicType", logicType.ordinal());
        nbtTags.putBoolean("activeCooled", activeCooled);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!world.isRemote) {
            int type = dataStream.readInt();
            if (type == 0) {
                activeCooled = !activeCooled;
            } else if (type == 1) {
                logicType = dataStream.readEnumValue(ReactorLogic.class);
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (world.isRemote) {
            //logicType = dataStream.readEnumValue(ReactorLogic.class);
            activeCooled = dataStream.readBoolean();
            prevOutputting = dataStream.readBoolean();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        //data.add(logicType);
        data.add(activeCooled);
        data.add(prevOutputting);
        return data;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (getReactor() == null || !getReactor().isFormed()) {
            return new Object[]{"Unformed."};
        }
        switch (method) {
            case 0:
                return new Object[]{getReactor().isBurning()};
            case 1:
                return new Object[]{getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled)};
            case 2:
                return new Object[]{getReactor().getPlasmaTemp()};
            case 3:
                return new Object[]{getReactor().getMaxPlasmaTemperature(activeCooled)};
            case 4:
                return new Object[]{getReactor().getCaseTemp()};
            case 5:
                return new Object[]{getReactor().getMaxCasingTemperature(activeCooled)};
            case 6:
                return new Object[]{getReactor().getInjectionRate()};
            case 7:
                if (arguments[0] instanceof Double) {
                    getReactor().setInjectionRate(((Double) arguments[0]).intValue());
                    return new Object[]{"Injection rate set."};
                }
                return new Object[]{"Invalid parameters."};
            case 8:
                return new Object[]{(getReactor().getDeuteriumTank().getStored() >= getReactor().getInjectionRate() / 2) &&
                                    (getReactor().getTritiumTank().getStored() >= getReactor().getInjectionRate() / 2)};
            case 9:
                return new Object[]{getReactor().getPassiveGeneration(false, true)};
            case 10:
                return new Object[]{getReactor().getIgnitionTemperature(activeCooled)};
            case 11:
                return new Object[]{getReactor().getBufferedEnergy()};
            case 12:
                return new Object[]{getReactor().getBufferSize()};
            case 13:
                return new Object[]{getReactor().getWaterTank().getFluidAmount()};
            case 14:
                return new Object[]{getReactor().getSteamTank().getFluidAmount()};
            case 15:
                return new Object[]{getReactor().getFuelTank().getStored()};
            case 16:
                return new Object[]{getReactor().getDeuteriumTank().getStored()};
            case 17:
                return new Object[]{getReactor().getTritiumTank().getStored()};
            default:
                throw new NoSuchMethodException();
        }
    }

    public enum ReactorLogic implements IHasTranslationKey {
        DISABLED("disabled", new ItemStack(Items.GUNPOWDER)),
        READY("ready", new ItemStack(Items.REDSTONE)),
        CAPACITY("capacity", new ItemStack(Items.REDSTONE)),
        DEPLETED("depleted", new ItemStack(Items.REDSTONE));

        private String name;
        private ItemStack renderStack;

        ReactorLogic(String s, ItemStack stack) {
            name = s;
            renderStack = stack;
        }

        public ItemStack getRenderStack() {
            return renderStack;
        }

        @Override
        public String getTranslationKey() {
            return "mekanism.reactor." + name;
        }

        public String getDescription() {
            return "mekanism.reactor." + name + ".desc";
        }
    }
}