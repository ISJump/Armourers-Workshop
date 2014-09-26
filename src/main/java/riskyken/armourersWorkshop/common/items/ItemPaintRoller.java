package riskyken.armourersWorkshop.common.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import riskyken.armourersWorkshop.common.blocks.ModBlocks;
import riskyken.armourersWorkshop.common.lib.LibCommonTags;
import riskyken.armourersWorkshop.common.lib.LibItemNames;
import riskyken.armourersWorkshop.common.lib.LibModInfo;
import riskyken.armourersWorkshop.common.lib.LibSounds;
import riskyken.armourersWorkshop.common.tileentities.IColourable;
import riskyken.armourersWorkshop.common.tileentities.IWorldColourable;
import riskyken.armourersWorkshop.common.undo.UndoManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPaintRoller extends AbstractModItem implements IColourTool {
    
    public ItemPaintRoller() {
        super(LibItemNames.PAINT_ROLLER);
    }
    
    @SideOnly(Side.CLIENT)
    private IIcon tipIcon;
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon(LibModInfo.ID + ":" + "paintRoller");
        tipIcon = register.registerIcon(LibModInfo.ID + ":" + "paintRollerTip");
    }
    
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
            int side, float hitX, float hitY, float hitZ) {
        
        if (!getToolHasColour(stack)) {
            return false;
        }
        
        Block block = world.getBlock(x, y, z);
        
        if (player.isSneaking() & block == ModBlocks.colourMixer) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null && te instanceof IColourable) {
                if (!world.isRemote) {
                    int colour = ((IColourable)te).getColour();
                    setToolColour(stack, colour);
                }
            }
            return true;
        }
        
        if (!player.isSneaking() & block instanceof IWorldColourable) {
            world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, LibSounds.PAINT, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
            for (int i = -1; i < 2; i++ ) {
                for (int j = -1; j < 2; j++ ) {
                    switch (side) {
                        case 0:
                            paintBlock(world, player, stack, x + j, y, z + i);
                            break;
                        case 1:
                            paintBlock(world, player, stack, x + j , y, z + i);
                            break;
                        case 2:
                            paintBlock(world, player, stack, x + i, y  + j, z);
                            break;
                        case 3:
                            paintBlock(world, player, stack, x + i, y + j, z);
                            break;
                        case 4:
                            paintBlock(world, player, stack, x, y + i, z + j);
                            break;
                        case 5:
                            paintBlock(world, player, stack, x, y + i, z + j);
                            break;
                    }
                }
            }
            return true;
        }
        
        return false;
    }
    
    private void paintBlock(World world, EntityPlayer player, ItemStack stack, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof IWorldColourable) {
            if (!world.isRemote) {
                IWorldColourable worldColourable = (IWorldColourable) block;
                int oldColour = worldColourable.getColour(world, x, y, z);
                int newColour = getToolColour(stack);
                UndoManager.playerPaintedBlock(player, world, x, y, z, oldColour);
                ((IWorldColourable)block).setColour(world, x, y, z, newColour);
            }
        }
    }
    
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        if (getToolHasColour(stack)) {
            list.add("Colour " + getToolColour(stack));
        } else {
            list.add("No paint");
        }
        super.addInformation(stack, player, list, p_77624_4_);
    }
    
    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }
    
    @Override
    public int getRenderPasses(int metadata) {
        return 2;
    }
    
    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        if (pass == 0) {
            return super.getColorFromItemStack(stack, pass);
        }
        return getToolColour(stack);
    }
    
    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        if (pass == 0) {
            return itemIcon;
        }
        return tipIcon;
    }
    
    private NBTTagCompound getCompound(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    @Override
    public boolean getToolHasColour(ItemStack stack) {
        NBTTagCompound compound = getCompound(stack);
        if (compound.hasKey(LibCommonTags.TAG_COLOUR)) {
            return true;
        }
        return false;
    }

    @Override
    public int getToolColour(ItemStack stack) {
        NBTTagCompound compound = getCompound(stack);
        if (compound.hasKey(LibCommonTags.TAG_COLOUR)) {
            return compound.getInteger(LibCommonTags.TAG_COLOUR);
        }
        return 16777215;
    }

    @Override
    public void setToolColour(ItemStack stack, int colour) {
        NBTTagCompound compound = getCompound(stack);
        compound.setInteger(LibCommonTags.TAG_COLOUR, colour);
    }
}
