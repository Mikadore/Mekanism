package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.annotations.BracketDumper;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_DUMPERS)
public class CrTBracketDumpers {

    @BracketDumper(value = CrTConstants.BRACKET_GAS, subCommandName = "gases")
    public static Collection<String> getGasStackDump() {
        return getChemicalStackDump(MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
    }

    @BracketDumper(value = CrTConstants.BRACKET_INFUSE_TYPE, subCommandName = "infuseTypes")
    public static Collection<String> getInfusionStackDump() {
        return getChemicalStackDump(MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
    }

    @BracketDumper(value = CrTConstants.BRACKET_PIGMENT, subCommandName = "pigments")
    public static Collection<String> getPigmentStackDump() {
        return getChemicalStackDump(MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
    }

    @BracketDumper(value = CrTConstants.BRACKET_SLURRY, subCommandName = "slurries")
    public static Collection<String> getSlurryStackDump() {
        return getChemicalStackDump(MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, ?, CRT_STACK>> Collection<String>
    getChemicalStackDump(IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter) {
        return registry.getValues()
              .stream()
              .map(chemical -> getter.apply(chemical).getCommandString())
              .collect(Collectors.toList());
    }
}