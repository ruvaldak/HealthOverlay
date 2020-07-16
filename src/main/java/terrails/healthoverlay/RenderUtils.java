package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderUtils {

    public static void drawColoredTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, alpha != 0 ? DefaultVertexFormats.POSITION_COLOR_TEX : DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(matrices, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).tex(u0, v1).endVertex();
        bufferBuilder.pos(matrices, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).tex(u1, v1).endVertex();
        bufferBuilder.pos(matrices, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).tex(u1, v0).endVertex();
        bufferBuilder.pos(matrices, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).tex(u0, v0).endVertex();
        bufferBuilder.finishDrawing();
        //noinspection deprecation
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferBuilder);
    }
}
