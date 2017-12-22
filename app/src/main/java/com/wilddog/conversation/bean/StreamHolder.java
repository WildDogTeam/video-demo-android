package com.wilddog.conversation.bean;

import com.wilddog.video.base.core.Stream;

public class StreamHolder {
    private Long timestamp;
    private Stream stream;
    private boolean isLocal =false;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StreamHolder(boolean isLocal, long timestamp, Stream stream) {
        this.isLocal = isLocal;
        this.timestamp = timestamp;
        this.stream = stream;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreamHolder that = (StreamHolder) o;

        if (isLocal != that.isLocal) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        if (stream != null ? !stream.equals(that.stream) : that.stream != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (stream != null ? stream.hashCode() : 0);
        result = 31 * result + (isLocal ? 1 : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
