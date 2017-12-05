package com.wilddog.conversation.bean;

import com.wilddog.video.base.core.Stream;

public class StreamHolder {
    private Long timeStamp;
    private Stream stream;
    private boolean isLocal =false;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StreamHolder(boolean isLocal, long timeStamp, Stream stream) {
        this.isLocal = isLocal;
        this.timeStamp = timeStamp;
        this.stream = stream;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
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
        if (timeStamp != null ? !timeStamp.equals(that.timeStamp) : that.timeStamp != null)
            return false;
        if (stream != null ? !stream.equals(that.stream) : that.stream != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = timeStamp != null ? timeStamp.hashCode() : 0;
        result = 31 * result + (stream != null ? stream.hashCode() : 0);
        result = 31 * result + (isLocal ? 1 : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
