package net.mine_diver.smoothbeta.client.render;

import net.mine_diver.smoothbeta.client.render.gl.GlStateManager;
import net.minecraft.client.util.GlAllocationUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class VboPool implements AutoCloseable {
    private static final int DEFAULT_PAGE_BYTES = 2 * 1024 * 1024;
    private static final int INITIAL_DRAW_CAPACITY = 64;

    private final List<Page> pages = new ArrayList<>();
    private final List<DrawRange> drawRanges = new ArrayList<>();
    private final int vertexBytes;
    private final int defaultPageVertices;
    private VertexFormat.DrawMode drawMode = VertexFormat.DrawMode.QUADS;
    private int drawRangeCount;
    private Page drawPage;
    private boolean pageBatching;
    private boolean vertexAttributesEnabled;
    private boolean closed;

    public VboPool(VertexFormat format) {
        this.vertexBytes = format.getVertexSizeByte();
        if (this.vertexBytes <= 0)
            throw new IllegalArgumentException("Vertex format must not be empty");
        this.defaultPageVertices = Math.max(1, DEFAULT_PAGE_BYTES / this.vertexBytes);
    }

    @Override
    public void close() {
        this.deleteGlBuffers();
    }

    public void bufferData(ByteBuffer data, Pos poolPos) {
        if (this.closed)
            throw new IllegalStateException("VBO pool is closed");

        int byteCount = data.remaining();
        if (byteCount % this.vertexBytes != 0)
            throw new IllegalArgumentException("Vertex data is not aligned to the " + this.vertexBytes + "-byte format");

        int vertexCount = byteCount / this.vertexBytes;
        if (vertexCount == 0) {
            this.release(poolPos);
            return;
        }

        if (poolPos.page == null || vertexCount > poolPos.allocationSize) {
            this.release(poolPos);
            this.allocate(poolPos, vertexCount);
        } else if (vertexCount <= poolPos.allocationSize / 4) {
            poolPos.page.free(poolPos.position + vertexCount, poolPos.allocationSize - vertexCount);
            poolPos.allocationSize = vertexCount;
        }

        poolPos.size = vertexCount;
        poolPos.page.bind();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, this.toBytes(poolPos.position), data);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void allocate(Pos poolPos, int vertexCount) {
        for (Page page : this.pages) {
            int position = page.allocate(vertexCount);
            if (position >= 0) {
                poolPos.setAllocation(page, position, vertexCount);
                return;
            }
        }

        Page page = new Page(Math.max(this.defaultPageVertices, vertexCount));
        this.pages.add(page);
        int position = page.allocate(vertexCount);
        if (position < 0)
            throw new IllegalStateException("New VBO page could not satisfy an allocation");
        poolPos.setAllocation(page, position, vertexCount);
    }

    private void release(Pos poolPos) {
        Page page = poolPos.page;
        if (page != null) {
            page.free(poolPos.position, poolPos.allocationSize);
            if (page.used == 0 && this.hasAnotherEmptyPage(page)) {
                this.pages.remove(page);
                page.close();
            }
        }
        poolPos.clear();
    }

    private boolean hasAnotherEmptyPage(Page releasedPage) {
        for (Page page : this.pages)
            if (page != releasedPage && page.used == 0)
                return true;
        return false;
    }

    private long toBytes(int vertexCount) {
        return (long) vertexCount * this.vertexBytes;
    }

    public void upload(VertexFormat.DrawMode drawMode, Pos range) {
        if (range.page == null || range.size == 0)
            return;

        if (this.drawRangeCount > 0 && this.drawMode != drawMode)
            throw new IllegalArgumentException("Mixed region draw modes: " + this.drawMode + " != " + drawMode);

        this.drawMode = drawMode;
        if (this.pageBatching) {
            range.page.queue(range.position, range.size);
            ++this.drawRangeCount;
            return;
        }

        DrawRange drawRange;
        if (this.drawRangeCount == this.drawRanges.size()) {
            drawRange = new DrawRange();
            this.drawRanges.add(drawRange);
        } else
            drawRange = this.drawRanges.get(this.drawRangeCount);
        drawRange.set(range.page, range.position, range.size);
        ++this.drawRangeCount;
    }

    public void drawAll() {
        if (this.drawRangeCount == 0)
            return;

        if (this.pageBatching) {
            for (Page page : this.pages) {
                if (page.firsts.position() == 0)
                    continue;
                this.bindForDraw(page);
                page.draw(this.drawMode);
            }
            this.drawRangeCount = 0;
            return;
        }

        int drawIndex = 0;
        while (drawIndex < this.drawRangeCount) {
            Page page = this.drawRanges.get(drawIndex).page;
            this.bindForDraw(page);

            do {
                DrawRange range = this.drawRanges.get(drawIndex);
                page.queue(range.first, range.count);
                range.page = null;
                ++drawIndex;
            } while (drawIndex < this.drawRangeCount && this.drawRanges.get(drawIndex).page == page);
            page.draw(this.drawMode);
        }

        this.drawRangeCount = 0;
    }

    public void setPageBatching(boolean pageBatching) {
        this.pageBatching = pageBatching;
    }

    private void bindForDraw(Page page) {
        if (!this.vertexAttributesEnabled) {
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
            this.vertexAttributesEnabled = true;
        }

        if (page == this.drawPage)
            return;

        page.bind();
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, this.vertexBytes, 0L);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, this.vertexBytes, 12L);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_UNSIGNED_BYTE, true, this.vertexBytes, 20L);
        this.drawPage = page;
    }

    public void finishDrawing() {
        if (this.vertexAttributesEnabled) {
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            this.vertexAttributesEnabled = false;
        }

        if (this.drawPage != null) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            this.drawPage = null;
        }
    }

    public void deleteGlBuffers() {
        if (this.closed)
            return;

        this.finishDrawing();
        for (Page page : this.pages)
            page.close();
        this.pages.clear();
        this.drawRanges.clear();
        this.drawRangeCount = 0;
        this.closed = true;
    }

    private final class Page implements AutoCloseable {
        private int vertexBufferId;
        private final int capacity;
        private int used;
        private final List<FreeRange> freeRanges = new ArrayList<>();
        private IntBuffer firsts = GlAllocationUtils.allocateIntBuffer(INITIAL_DRAW_CAPACITY);
        private IntBuffer counts = GlAllocationUtils.allocateIntBuffer(INITIAL_DRAW_CAPACITY);

        private Page(int capacity) {
            this.capacity = capacity;
            this.freeRanges.add(new FreeRange(0, capacity));
            this.vertexBufferId = GL15.glGenBuffers();
            this.bind();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VboPool.this.toBytes(capacity), GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        private void bind() {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBufferId);
        }

        private int allocate(int requestedSize) {
            for (int i = 0; i < this.freeRanges.size(); ++i) {
                FreeRange range = this.freeRanges.get(i);
                if (range.size < requestedSize)
                    continue;

                int position = range.position;
                if (range.size == requestedSize)
                    this.freeRanges.remove(i);
                else {
                    range.position += requestedSize;
                    range.size -= requestedSize;
                }
                this.used += requestedSize;
                return position;
            }
            return -1;
        }

        private void free(int position, int size) {
            if (size <= 0)
                return;

            int insertionIndex = 0;
            while (insertionIndex < this.freeRanges.size()
                    && this.freeRanges.get(insertionIndex).position < position)
                ++insertionIndex;

            FreeRange previous = insertionIndex > 0 ? this.freeRanges.get(insertionIndex - 1) : null;
            FreeRange next = insertionIndex < this.freeRanges.size() ? this.freeRanges.get(insertionIndex) : null;
            int end = position + size;
            if (position < 0 || end > this.capacity
                    || previous != null && previous.end() > position
                    || next != null && end > next.position)
                throw new IllegalStateException("Invalid or overlapping VBO page release");

            FreeRange released = new FreeRange(position, size);
            this.freeRanges.add(insertionIndex, released);

            if (previous != null && previous.end() == released.position) {
                previous.size += released.size;
                this.freeRanges.remove(insertionIndex);
                released = previous;
                --insertionIndex;
            }

            if (insertionIndex + 1 < this.freeRanges.size()) {
                next = this.freeRanges.get(insertionIndex + 1);
                if (released.end() == next.position) {
                    released.size += next.size;
                    this.freeRanges.remove(insertionIndex + 1);
                }
            }

            this.used -= size;
            if (this.used < 0)
                throw new IllegalStateException("VBO page usage became negative");
        }

        private void queue(int first, int count) {
            int last = this.firsts.position() - 1;
            if (last >= 0 && this.firsts.get(last) + this.counts.get(last) == first) {
                this.counts.put(last, this.counts.get(last) + count);
                return;
            }

            if (!this.firsts.hasRemaining())
                this.growDrawBuffers();
            this.firsts.put(first);
            this.counts.put(count);
        }

        private void growDrawBuffers() {
            int newCapacity = this.firsts.capacity() * 2;
            IntBuffer newFirsts = GlAllocationUtils.allocateIntBuffer(newCapacity);
            IntBuffer newCounts = GlAllocationUtils.allocateIntBuffer(newCapacity);
            this.firsts.flip();
            this.counts.flip();
            newFirsts.put(this.firsts);
            newCounts.put(this.counts);
            this.firsts = newFirsts;
            this.counts = newCounts;
        }

        private void draw(VertexFormat.DrawMode drawMode) {
            this.firsts.flip();
            this.counts.flip();
            GL14.glMultiDrawArrays(drawMode.glMode, this.firsts, this.counts);
            this.firsts.clear();
            this.counts.clear();
        }

        @Override
        public void close() {
            if (this.vertexBufferId > 0) {
                GlStateManager._glDeleteBuffers(this.vertexBufferId);
                this.vertexBufferId = 0;
            }
        }
    }

    private final class DrawRange {
        private Page page;
        private int first;
        private int count;

        private void set(Page page, int first, int count) {
            this.page = page;
            this.first = first;
            this.count = count;
        }
    }

    private static final class FreeRange {
        private int position;
        private int size;

        private FreeRange(int position, int size) {
            this.position = position;
            this.size = size;
        }

        private int end() {
            return this.position + this.size;
        }
    }

    public static final class Pos {
        private Page page;
        private int position = -1;
        private int size;
        private int allocationSize;

        public int getPosition() {
            return this.position;
        }

        public int getSize() {
            return this.size;
        }

        private void setAllocation(Page page, int position, int allocationSize) {
            this.page = page;
            this.position = position;
            this.size = allocationSize;
            this.allocationSize = allocationSize;
        }

        private void clear() {
            this.page = null;
            this.position = -1;
            this.size = 0;
            this.allocationSize = 0;
        }

        @Override
        public String toString() {
            return this.position + "/" + this.size + "/" + (this.position + this.allocationSize);
        }
    }
}
