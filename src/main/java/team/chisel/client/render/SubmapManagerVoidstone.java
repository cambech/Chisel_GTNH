package team.chisel.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.cricketcraft.chisel.api.carving.CarvingUtils;
import com.cricketcraft.chisel.api.rendering.TextureType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import team.chisel.ctmlib.ISubmapManager;
import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.ctmlib.TextureSubmap;

public class SubmapManagerVoidstone extends SubmapManagerBase {

    private static ThreadLocal<RenderBlocksCTM> renderBlocksThreadLocal;

    private static void initStatics() {
        if (renderBlocksThreadLocal == null) {
            renderBlocksThreadLocal = ThreadLocal.withInitial(RenderBlocksCTM::new);
        }
    }

    private ISubmapManager overlay;
    private TextureSubmap base;

    private String texture;
    private int meta;

    public SubmapManagerVoidstone(String texture, int meta) {
        this.texture = texture;
        this.meta = meta;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return overlay.getIcon(side, meta);
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return overlay.getIcon(world, x, y, z, side);
    }

    @Override
    public void registerIcons(String modName, Block block, IIconRegister register) {
        overlay = TextureType.getTypeFor(null, modName, texture)
            .createManagerFor(CarvingUtils.getDefaultVariationFor(block, meta, 0), texture);
        overlay.registerIcons(modName, block, register);
        base = new TextureSubmap(register.registerIcon(modName + ":" + "animations/hadesX32"), 2, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        initStatics();
        RenderBlocksCTM rb = renderBlocksThreadLocal.get();
        try {
            RenderBlocks ctx = overlay.createRenderContext(rendererOld, block, world);
            rb.setRenderBoundsFromBlock(block);
            if (ctx instanceof RenderBlocksCTM
                && (texture == "voidstone/animated/bevel" || texture == "voidstone/animated/metalborder")) {
                rb.submap = ((RenderBlocksCTM) ctx).submap;
                rb.submapSmall = ((RenderBlocksCTM) ctx).submapSmall;
            }
            return rb;
        } finally {
            renderBlocksThreadLocal.remove();
        }
    }

    private boolean hadOverride = false;

    @Override
    @SideOnly(Side.CLIENT)
    public void preRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        RenderBlocksCTM rbctm = (RenderBlocksCTM) renderer;
        if (!rbctm.hasOverrideBlockTexture()) {
            hadOverride = true;
            rbctm.setOverrideBlockTexture(
                TextureType.getVIcon(
                    TextureType.V4,
                    base,
                    MathHelper.floor_double(x),
                    MathHelper.floor_double(y),
                    MathHelper.floor_double(z),
                    side.ordinal()));
            if (side == ForgeDirection.NORTH) {
                rbctm.renderFaceZNeg(world.getBlock(x, y, z), x, y, z, null);
            } else if (side == ForgeDirection.SOUTH) {
                rbctm.renderFaceZPos(world.getBlock(x, y, z), x, y, z, null);
            } else if (side == ForgeDirection.WEST) {
                rbctm.renderFaceXNeg(world.getBlock(x, y, z), x, y, z, null);
            } else if (side == ForgeDirection.EAST) {
                rbctm.renderFaceXPos(world.getBlock(x, y, z), x, y, z, null);
            } else if (side == ForgeDirection.DOWN) {
                rbctm.renderFaceYNeg(world.getBlock(x, y, z), x, y, z, null);
            } else {
                rbctm.renderFaceYPos(world.getBlock(x, y, z), x, y, z, null);
            }
            hadOverride = false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        RenderBlocksCTM rbctm = (RenderBlocksCTM) renderer;
        rbctm.clearOverrideBlockTexture();
        if (texture == "voidstone/animated/bevel" || texture == "voidstone/animated/metalborder") {
            if (side == ForgeDirection.NORTH) {
                rbctm.renderMinZ -= 0.001;
            } else if (side == ForgeDirection.SOUTH) {
                rbctm.renderMaxZ += 0.001;
            } else if (side == ForgeDirection.WEST) {
                rbctm.renderMinX -= 0.001;
            } else if (side == ForgeDirection.EAST) {
                rbctm.renderMaxX += 0.001;
            } else if (side == ForgeDirection.DOWN) {
                rbctm.renderMinY -= 0.001;
            } else {
                rbctm.renderMaxY += 0.001;
            }
        }
    }
}
