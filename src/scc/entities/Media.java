package scc.entities;

import org.bson.Document;

public class Media {
    private String mediaId;
    private int references;

    public static final String ID = "_id";
    public static final String REFERENCES = "references";


    public Media(String mediaId, int references) {
        this.mediaId = mediaId;
        this.references = references;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public int getReferences() {
        return references;
    }

    public void setReferences(int references) {
        this.references = references;
    }

    public Document toDocument() {
        return new Document()
                .append(ID, mediaId)
                .append(REFERENCES, references);

    }

}
