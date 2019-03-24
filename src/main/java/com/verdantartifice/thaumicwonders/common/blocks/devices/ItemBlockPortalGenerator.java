package com.verdantartifice.thaumicwonders.common.blocks.devices;

import java.util.List;

import com.verdantartifice.thaumicwonders.common.blocks.BlocksTW;
import com.verdantartifice.thaumicwonders.common.tiles.devices.TilePortalAnchor;
import com.verdantartifice.thaumicwonders.common.tiles.devices.TilePortalGenerator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.lib.SoundsTC;

public class ItemBlockPortalGenerator extends ItemBlock {
    public ItemBlockPortalGenerator(Block block) {
        super(block);
    }
    
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.getBlockState(pos).getBlock() == BlocksTW.PORTAL_ANCHOR) {
            if (world.isRemote) {
                player.swingArm(hand);
                return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
            } else {
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null && tile instanceof TilePortalAnchor) {
                    ItemStack stack = player.getHeldItem(hand).copy();
                    stack.setCount(1);
                    stack.setTagInfo("linkX", new NBTTagInt(tile.getPos().getX()));
                    stack.setTagInfo("linkY", new NBTTagInt(tile.getPos().getY()));
                    stack.setTagInfo("linkZ", new NBTTagInt(tile.getPos().getZ()));
                    stack.setTagInfo("linkDim", new NBTTagInt(world.provider.getDimension()));
                    world.playSound(null, pos, SoundsTC.jar, SoundCategory.BLOCKS, 1.0F, 2.0F);
                    player.getHeldItem(hand).shrink(1);
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, stack));
                    }
                }
            }
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
    
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean retval = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (retval && !world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile instanceof TilePortalGenerator && stack.hasTagCompound()) {
                TilePortalGenerator generatorTile = (TilePortalGenerator)tile;
                generatorTile.setLink(
                        stack.getTagCompound().getInteger("linkX"), 
                        stack.getTagCompound().getInteger("linkY"), 
                        stack.getTagCompound().getInteger("linkZ"), 
                        stack.getTagCompound().getInteger("linkDim")
                );
            }
        }
        return retval;
    }
    
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            int linkX = stack.getTagCompound().getInteger("linkX");
            int linkY = stack.getTagCompound().getInteger("linkY");
            int linkZ = stack.getTagCompound().getInteger("linkZ");
            int linkDim = stack.getTagCompound().getInteger("linkDim");
            String desc = "" + linkDim;
            World linkWorld = DimensionManager.getWorld(linkDim);
            if (linkWorld != null) {
                desc = linkWorld.provider.getDimensionType().getName();
            }
            tooltip.add(I18n.format("tile.thaumicwonders.portal_generator.tooltip.linkedto", linkX, linkY, linkZ, desc));
        }
    }
}
