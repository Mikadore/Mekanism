package mekanism.common.lib.multiblock;

import net.minecraft.tileentity.TileEntity;

public interface IStructuralMultiblock extends IMultiblockBase {

    boolean canInterface(TileEntity controller);

    void setMultiblock(MultiblockData multiblock);
}