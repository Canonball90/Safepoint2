package safepoint.two.utils.render;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import safepoint.two.Safepoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import safepoint.two.utils.math.Interpolation;

import java.awt.*;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static safepoint.two.Safepoint.mc;

public class RenderUtil {
    private static final RenderUtil INSTANCE = new RenderUtil();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static ICamera camera = new Frustum();
    public static Tessellator tessellator = Tessellator.getInstance();
    public static BufferBuilder bufferBuilder = tessellator.getBuffer();

    public static void drawBoxESPFlat(BlockPos pos, boolean box, boolean outline, Color color, Color outlineColor, float lineWidth) {
        if (box)
            drawBoxFlat(pos, color);
        if (outline)
            drawBlockOutlineFlat(pos, outlineColor, lineWidth);
    }

    public static void drawBlockOutlineFlat(final BlockPos pos, final Color color, final float lineWidth) {
        final IBlockState iblockstate = mc.world.getBlockState(pos);
        if (mc.world.getWorldBorder().contains(pos)) {
            final Vec3d interp = interpolateEntity(mc.player, mc.getRenderPartialTicks());
            drawBlockOutlineFlat(iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z), color, lineWidth);
        }
    }

    public static Vec3d interpolateEntity(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) time);
    }

    public static void drawBoxFlat(final BlockPos pos, final Color color) {
        final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ, pos.getX() + 1 - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            glEnable(2848);
            glHint(3154, 4354);
            RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static void drawBlockOutlineFlat(final AxisAlignedBB bb, final Color color, final float lineWidth) {
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        glEnable(2848);
        glHint(3154, 4354);
        GL11.glLineWidth(lineWidth);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    public static void drawOutlineRect(double left, double top, double right, double bottom, Color color, float lineWidth) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float) (color.getRGB() >> 24 & 255) / 255.0F;
        float f = (float) (color.getRGB() >> 16 & 255) / 255.0F;
        float f1 = (float) (color.getRGB() >> 8 & 255) / 255.0F;
        float f2 = (float) (color.getRGB() & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(lineWidth);
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public static void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static boolean isHovered(int X, int Y, int W, int H, int mX, int mY) {
        return mX >= X && mX <= X + W && mY >= Y && mY <= Y + H;
    }

    public static void drawBorderedRect(float x, float y, float x2, float y2, float lineSize, int color, int borderColor) {
        drawRect(x, y, x2, y2, color);
        drawRect(x, y, x + lineSize, y2, borderColor);
        drawRect(x2 - lineSize, y, x2, y2, borderColor);
        drawRect(x, y2 - lineSize, x2, y2, borderColor);
        drawRect(x, y, x2, y + lineSize, borderColor);
    }

    public static void rect(double x, double y, double width, double height, Color color) {
        GL11.glEnable((int) 3042);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDisable((int) 3553);
        GL11.glDisable((int) 2884);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        if (color != null) {
            color(color);
        }
        GL11.glBegin((int) 6);
        GL11.glVertex2d((double) x, (double) y);
        GL11.glVertex2d((double) (x + width), (double) y);
        GL11.glVertex2d((double) (x + width), (double) (y + height));
        GL11.glVertex2d((double) x, (double) (y + height));
        GL11.glEnd();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GL11.glEnable((int) 2884);
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        color(Color.white);
    }


    public static int getStringWidth(final String str) {
        return mc.fontRenderer.getStringWidth(str);
    }

    public static void color(Color color) {
        if (color == null) {
            color = Color.white;
        }
        GL11.glColor4d((double) ((float) color.getRed() / 255.0f), (double) ((float) color.getGreen() / 255.0f), (double) ((float) color.getBlue() / 255.0f), (double) ((float) color.getAlpha() / 255.0f));
    }

    public static void roundedRect(double x, double y, double width, double height, double edgeRadius, Color color) {
        double angle;
        double i;
        double halfRadius = edgeRadius / 2.0;
        width -= halfRadius;
        height -= halfRadius;
        float sideLength = (float) edgeRadius;
        sideLength /= 2.0f;
        GL11.glEnable((int) 3042);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDisable((int) 3553);
        GL11.glDisable((int) 2884);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        if (color != null) {
            color(color);
        }
        GL11.glBegin((int) 6);
        for (i = 180.0; i <= 270.0; i += 1.0) {
            angle = i * (Math.PI * 2) / 360.0;
            GL11.glVertex2d((double) (x + (double) sideLength * Math.cos(angle) + (double) sideLength), (double) (y + (double) sideLength * Math.sin(angle) + (double) sideLength));
        }
        GL11.glVertex2d((double) (x + (double) sideLength), (double) (y + (double) sideLength));
        GL11.glEnd();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GL11.glEnable((int) 2884);
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        color(Color.white);
        sideLength = (float) edgeRadius;
        sideLength /= 2.0f;
        GL11.glEnable((int) 3042);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDisable((int) 3553);
        GL11.glDisable((int) 2884);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        if (color != null) {
            color(color);
        }
        GL11.glEnable((int) 2848);
        GL11.glBegin((int) 6);
        for (i = 0.0; i <= 90.0; i += 1.0) {
            angle = i * (Math.PI * 2) / 360.0;
            GL11.glVertex2d((double) (x + width + (double) sideLength * Math.cos(angle)), (double) (y + height + (double) sideLength * Math.sin(angle)));
        }
        GL11.glVertex2d((double) (x + width), (double) (y + height));
        GL11.glEnd();
        GL11.glDisable((int) 2848);
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GL11.glEnable((int) 2884);
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        color(Color.white);
        sideLength = (float) edgeRadius;
        sideLength /= 2.0f;
        GL11.glEnable((int) 3042);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDisable((int) 3553);
        GL11.glDisable((int) 2884);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        if (color != null) {
            color(color);
        }
        GL11.glEnable((int) 2848);
        GL11.glBegin((int) 6);
        for (i = 270.0; i <= 360.0; i += 1.0) {
            angle = i * (Math.PI * 2) / 360.0;
            GL11.glVertex2d((double) (x + width + (double) sideLength * Math.cos(angle)), (double) (y + (double) sideLength * Math.sin(angle) + (double) sideLength));
        }
        GL11.glVertex2d((double) (x + width), (double) (y + (double) sideLength));
        GL11.glEnd();
        GL11.glDisable((int) 2848);
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GL11.glEnable((int) 2884);
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        color(Color.white);
        sideLength = (float) edgeRadius;
        sideLength /= 2.0f;
        GL11.glEnable((int) 3042);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDisable((int) 3553);
        GL11.glDisable((int) 2884);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        if (color != null) {
            color(color);
        }
        GL11.glEnable((int) 2848);
        GL11.glBegin((int) 6);
        for (i = 90.0; i <= 180.0; i += 1.0) {
            angle = i * (Math.PI * 2) / 360.0;
            GL11.glVertex2d((double) (x + (double) sideLength * Math.cos(angle) + (double) sideLength), (double) (y + height + (double) sideLength * Math.sin(angle)));
        }
        GL11.glVertex2d((double) (x + (double) sideLength), (double) (y + height));
        GL11.glEnd();
        GL11.glDisable((int) 2848);
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GL11.glEnable((int) 2884);
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        color(Color.white);
        rect(x + halfRadius, y + halfRadius, width - halfRadius, height - halfRadius, color);
        rect(x, y + halfRadius, edgeRadius / 2.0, height - halfRadius, color);
        rect(x + width, y + halfRadius, edgeRadius / 2.0, height - halfRadius, color);
        rect(x + halfRadius, y, width - halfRadius, halfRadius, color);
        rect(x + halfRadius, y + height, width - halfRadius, halfRadius, color);
    }

    public static void prepare(int mode) {
        prepareGL();
        begin(mode);
    }

    public static void release() {
        render();
        releaseGL();
    }

    public static void prepare() {
        GL11.glBlendFunc((int)770, (int)771);
        GlStateManager.tryBlendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth((float)1.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask((boolean)false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f);
    }


    public static void render() {
        tessellator.draw();
    }

    public static void releaseGL() {
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void begin(int mode) {
        bufferBuilder.begin(mode, DefaultVertexFormats.POSITION_COLOR);
    }

    public static void drawBlockBox(BlockPos pos, Color color, boolean outline, float lineWidth) {
        drawBBBox(new AxisAlignedBB(pos), color, color.getAlpha(), lineWidth, outline);
    }

    public static void drawBoxOutline(BlockPos pos,Color color,float lineWidth){
        drawBoundingBox(new AxisAlignedBB(pos), lineWidth, color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public static void drawBBBox(AxisAlignedBB BB, Color colour, int alpha, float lineWidth, boolean outline) {
        AxisAlignedBB bb = new AxisAlignedBB(BB.minX - mc.getRenderManager().viewerPosX, BB.minY - mc.getRenderManager().viewerPosY, BB.minZ - mc.getRenderManager().viewerPosZ, BB.maxX - mc.getRenderManager().viewerPosX, BB.maxY - mc.getRenderManager().viewerPosY, BB.maxZ - mc.getRenderManager().viewerPosZ);
        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
            prepare(GL_QUADS);
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glHint(3154, 4354);
            GL11.glShadeModel(GL_SMOOTH);
            if (outline) {
                drawBoundingBox(bb, lineWidth, colour.getRed(), colour.getGreen(), colour.getBlue(), 255);
            }
            RenderGlobal.renderFilledBox(bb, (float) colour.getRed() / 255.0f, (float) colour.getGreen() / 255.0f, (float) colour.getBlue() / 255.0f, alpha / 255.0f);
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            release();
        }
    }

    public static void drawBoundingBox(AxisAlignedBB bb, float width, int red, int green, int blue, int alpha) {
        GL11.glLineWidth(width);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        drawBoundingBox(bb);
        GlStateManager.color(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        vertexBuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        vertexBuffer.begin(1, DefaultVertexFormats.POSITION);
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void prepareGL() {
        GL11.glBlendFunc(770, 771);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    public static void renderBox(BlockPos pos, Color color, float height) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        AxisAlignedBB bb = Interpolation.interpolatePos(pos, height);
        startRender();
        drawOutline(bb, 1.5f, color);
        endRender();
        Color boxColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 76);
        startRender();
        drawBox(bb, boxColor);
        endRender();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void startRender() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        glEnable(GL11.GL_CULL_FACE);
        glEnable(GL11.GL_LINE_SMOOTH);
        glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    public static void endRender() {
        glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void drawOutline(AxisAlignedBB bb, float lineWidth) {
        GL11.glPushMatrix();
        glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        fillOutline(bb);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LIGHTING);
        glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawOutline(AxisAlignedBB bb, float lineWidth, Color color) {
        GL11.glPushMatrix();
        glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        color(color);
        fillOutline(bb);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LIGHTING);
        glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void fillOutline(AxisAlignedBB bb) {
        if (bb != null) {
            GL11.glBegin(GL11.GL_LINES);
            {
                GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
                GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);

                GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
                GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);

                GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);

                GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
                GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);

                GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
                GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

                GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
                GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

                GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

                GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
                GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);

                GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
                GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

                GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
                GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

                GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
                GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);

                GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
                GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
            }
            GL11.glEnd();
        }
    }

    public static void drawBox(AxisAlignedBB bb, Color color) {
        GL11.glPushMatrix();
        glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        color(color);
        fillBox(bb);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LIGHTING);
        glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void fillBox(AxisAlignedBB boundingBox) {
        if (boundingBox != null) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ);
            GL11.glVertex3d((float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glVertex3d((float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ);
            GL11.glEnd();
        }
    }

    public static void releaseq() {
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask((boolean)true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void color(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    public static void drawBlock(AxisAlignedBB bb, Color color) {
        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
            prepareGL();
            RenderGlobal.renderFilledBox((AxisAlignedBB)bb, (float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
            releaseq();
        }
    }

    public static void drawBlockOutline(final AxisAlignedBB bb, final Color color, final float linewidth) {
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        glEnable(2848);
        glHint(3154, 4354);
        GL11.glLineWidth(linewidth);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air) {
        if (box) {
            RenderUtil.drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha));
        }
        if (outline) {
            RenderUtil.drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air);
        }
    }

    public static void drawBlockOutline(final BlockPos pos, final Color color, final float linewidth, final boolean air) {
        final IBlockState iblockstate = RenderUtil.mc.world.getBlockState(pos);
        if ((air || iblockstate.getMaterial() != Material.AIR) && RenderUtil.mc.world.getWorldBorder().contains(pos)) {
            final Vec3d interp = interpolateEntity(RenderUtil.mc.player, RenderUtil.mc.getRenderPartialTicks());
            drawBlockOutline(iblockstate.getSelectedBoundingBox(RenderUtil.mc.world, pos).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z), color, linewidth);
        }
    }


    public static void drawBox(final BlockPos pos, final Color color) {
        final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - RenderUtil.mc.getRenderManager().viewerPosX, pos.getY() - RenderUtil.mc.getRenderManager().viewerPosY, pos.getZ() - RenderUtil.mc.getRenderManager().viewerPosZ, pos.getX() + 1 - RenderUtil.mc.getRenderManager().viewerPosX, pos.getY() + 1 - RenderUtil.mc.getRenderManager().viewerPosY, pos.getZ() + 1 - RenderUtil.mc.getRenderManager().viewerPosZ);
        RenderUtil.camera.setPosition(Objects.requireNonNull(RenderUtil.mc.getRenderViewEntity()).posX, RenderUtil.mc.getRenderViewEntity().posY, RenderUtil.mc.getRenderViewEntity().posZ);
        if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + RenderUtil.mc.getRenderManager().viewerPosX, bb.minY + RenderUtil.mc.getRenderManager().viewerPosY, bb.minZ + RenderUtil.mc.getRenderManager().viewerPosZ, bb.maxX + RenderUtil.mc.getRenderManager().viewerPosX, bb.maxY + RenderUtil.mc.getRenderManager().viewerPosY, bb.maxZ + RenderUtil.mc.getRenderManager().viewerPosZ))) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            glEnable(2848);
            glHint(3154, 4354);
            RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
}
