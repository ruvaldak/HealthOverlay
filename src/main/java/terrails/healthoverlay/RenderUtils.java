package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;

public class RenderUtils {

    public static void drawColoredTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, alpha != 0 ? VertexFormats.POSITION_COLOR_TEXTURE : VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).texture(u0, v0).next();
        bufferBuilder.end();
        //noinspection deprecation
        RenderSystem.enableAlphaTest();
        BufferRenderer.draw(bufferBuilder);
    }
}
